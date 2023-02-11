package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.ServerBrowserFabClient;
import com.epherical.serverbrowser.client.list.ServerBrowserList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
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
    private Component websiteStatus;

    private Screen previousScreen;

    public static JsonElement servers;

    private int page = 1;

    public ServerBrowserScreen(Screen previousScreen) {
        super(Component.nullToEmpty(""));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        queryServers();
        list = new ServerBrowserList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
        list.queryServers();

        if (list.getEntries().size() >= 20) {
            Button nextButton = this.addRenderableWidget(Button.builder(Component.translatable("Next: " + (page + 1)), button -> {
                if (list.getEntries().size() >= 20) {
                    page++;
                    queryServers();
                    list.queryServers();
                    button.setMessage(Component.translatable("Next: " + (page + 1)));
                }
            }).bounds(this.width - 110, 12, 60, 20).build());
            this.addRenderableWidget(Button.builder(Component.translatable("Prev"), button -> {
                if (page <= 1) {
                    page = 1;
                } else {
                    page--;
                    queryServers();
                    list.queryServers();
                }
                nextButton.setMessage(Component.translatable("Next: " + (page + 1)));
            }).bounds(this.width - 140, 12, 30, 20).build());
        }

        this.addRenderableWidget(new Button(this.width / 2 - 50, 3, 100, 20, Component.literal("Register Server"), button -> {
            this.minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
                if (bl) {
                    Util.getPlatform().openUri("https://minecraft.multiplayerservers.net");
                }
                this.minecraft.setScreen(this);
            }, "https://minecraft.multiplayerservers.net", true));
        }));
        this.joinButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), (button) -> {
            this.joinSelectedServer();
        }).bounds(this.width / 2 - 154, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("Filter Servers"), (button) -> {
            this.minecraft.setScreen(new FilterServerScreen(this));
        }).bounds(this.width / 2 - 50, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("History (WIP)"), (button) -> {
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

        if (websiteStatus != null) {
            drawCenteredString(poseStack, minecraft.font, websiteStatus, width / 2, 40, 0xFFFFFF);
        }
    }

    public void queryServers() {
        websiteStatus = null;
        try {
            URIBuilder builder = new URIBuilder("http://localhost:8080/api/v1/servers");
            if (page > 1) {
                builder.addParameter("page", String.valueOf(page));
            }
            for (Filter filter : ServerBrowserFabClient.getInstance().getFilters()) {
                if (filter.isActive()) {
                    builder.addParameter("type", filter.getTagName());
                }
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
        } catch (IOException | URISyntaxException ignored) {
            websiteStatus = Component.literal("Website could not be reached at the moment");
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
        list.refreshServers();
    }

    public ServerStatusPinger getPinger() {
        return pinger;
    }

    public void setToolTip(List<Component> toolTip) {
        this.toolTip = toolTip;
    }
}
