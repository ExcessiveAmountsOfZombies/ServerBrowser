package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.ServerBrowserFabClient;
import com.epherical.serverbrowser.client.list.ServerBrowserList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ServerBrowserScreen extends Screen {


    private final ServerStatusPinger pinger = new ServerStatusPinger();
    protected ServerBrowserList list;

    private Button joinButton;
    private Button favoriteButton;

    @Nullable
    private List<Component> toolTip;

    private Screen previousScreen;

    public static JsonElement servers;

    public ServerBrowserScreen(Screen previousScreen) {
        super(Component.nullToEmpty(""));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        queryServers();
        list = new ServerBrowserList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
        list.queryServers();

        this.joinButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), (button) -> {
            this.joinSelectedServer();
        }).bounds(this.width / 2 - 154, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("Filter Servers"), (button) -> {
            this.minecraft.setScreen(new FilterServerScreen(this));
        }).bounds(this.width / 2 - 50, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("History"), (button) -> {
            // todo; store 20 servers last joined from this screen.
        }).bounds(this.width / 2 + 54, this.height - 52, 100, 20).build());
        this.favoriteButton = this.addRenderableWidget(Button.builder(Component.translatable("Favorite"), (button) -> {
            ServerList serverList = new ServerList(this.minecraft);
            ServerBrowserList.Entry entry = this.list.getSelected();
            if (entry instanceof ServerBrowserList.BrowsedEntry browsedEntry) {
                ServerData serverData = browsedEntry.getServerData();
                serverList.add(serverData, false);
                serverList.save();
            }
        }).bounds(this.width / 2 - 154, this.height - 28, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectServer.refresh"), (button) -> {
            this.refreshServerList();
        }).bounds(this.width / 2 - 80, this.height - 28, 156, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(null);
        }).bounds(this.width / 2 + 4 + 76, this.height - 28, 75, 20).build());

        this.addWidget(list);
        onSelectedChange();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        toolTip = null;
        this.renderBackground(poseStack);

        this.list.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (this.toolTip != null) {
            this.renderComponentTooltip(poseStack, this.toolTip, mouseX, mouseY);
        }
    }

    public void queryServers() {
        try {
            URIBuilder builder = new URIBuilder("http://localhost:8080/api/v1/servers");
            for (Filter filter : ServerBrowserFabClient.filters) {
                builder.addParameter("type", filter.getTagName());
            }
            //builder.addParameter("type", "AOF5");
            URL url = builder.build().toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] bytes = connection.getInputStream().readAllBytes();
                String string = new String(bytes);
                servers = JsonParser.parseString(string);
            }
            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setSelected(ServerBrowserList.Entry selected) {
        this.list.setSelected(selected);
        this.onSelectedChange();
    }

    protected void onSelectedChange() {
        this.joinButton.active = false;
        this.favoriteButton.active = false;
        ServerBrowserList.Entry entry = this.list.getSelected();
        if (entry instanceof ServerBrowserList.BrowsedEntry) {
            this.joinButton.active = true;
            this.favoriteButton.active = true;
        }
    }

    public void joinSelectedServer() {
        ServerBrowserList.Entry entry = this.list.getSelected();
        if (entry instanceof ServerBrowserList.BrowsedEntry) {
            this.join(((ServerBrowserList.BrowsedEntry) entry).getServerData());
        }
    }

    private void join(ServerData server) {
        ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(server.ip), server);
    }

    private void refreshServerList() {
        this.minecraft.setScreen(new ServerBrowserScreen(this.previousScreen));
    }

    public ServerStatusPinger getPinger() {
        return pinger;
    }

    public void setToolTip(List<Component> toolTip) {
        this.toolTip = toolTip;
    }
}
