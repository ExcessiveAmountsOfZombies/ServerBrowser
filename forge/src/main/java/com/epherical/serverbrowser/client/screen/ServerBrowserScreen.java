package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.list.ServerBrowserList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class ServerBrowserScreen extends Screen {


    private final ServerPinger pinger = new ServerPinger();
    protected ServerBrowserList list;

    private Button joinButton;
    private Button favoriteButton;

    @Nullable
    private List<ITextComponent> toolTip;
    private ITextComponent websiteStatus;

    private Screen previousScreen;

    public static JsonElement servers;

    private int page = 1;

    public ServerBrowserScreen(Object previousScreen) {
        super(ITextComponent.nullToEmpty(""));
        // YIKES
        this.previousScreen = (Screen) previousScreen;
    }


    @Override
    protected void init() {
        queryServers();
        list = new ServerBrowserList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
        list.queryServers();

        if (list.getEntries().size() >= 20) {
            Button nextButton = this.addButton(new Button(this.width - 110, 12, 60, 20, new TranslationTextComponent("Next: " + (page + 1)), button -> {
                if (list.getEntries().size() >= 20) {
                    page++;
                    queryServers();
                    list.queryServers();
                    button.setMessage(new TranslationTextComponent("Next: " + (page + 1)));
                }
            }));
            this.addButton(new Button(this.width - 140, 12, 30, 20, new TranslationTextComponent("Prev"), button -> {
                if (page <= 1) {
                    page = 1;
                } else {
                    page--;
                    queryServers();
                    list.queryServers();
                }
                nextButton.setMessage(new TranslationTextComponent("Next: " + (page + 1)));
            }));
        }

        this.addButton(new Button(this.width / 2 - 50, 3, 100, 20, new TranslationTextComponent("Register Server"), button -> {
            this.minecraft.setScreen(new ConfirmOpenLinkScreen((bl) -> {
                if (bl) {
                    Util.getPlatform().openUri("https://minecraft.multiplayerservers.net");
                }
                this.minecraft.setScreen(this);
            }, "https://minecraft.multiplayerservers.net", true));
        }));
        this.joinButton = this.addButton(new Button(this.width / 2 - 154, this.height - 52, 100, 20, new TranslationTextComponent("selectServer.select"), (button) -> {
            this.joinSelectedServer();
        }));
        this.addButton(new Button(this.width / 2 - 50, this.height - 52, 100, 20, new TranslationTextComponent("Filter Servers"), (button) -> {
            this.minecraft.setScreen(new FilterServerScreen(this));
        }));
        this.addButton(new Button(this.width / 2 + 54, this.height - 52, 100, 20, new TranslationTextComponent("History (WIP)"), (button) -> {
            // todo; store 20 servers last joined from this screen.
        }));
        this.favoriteButton = this.addButton(new Button(this.width / 2 - 154, this.height - 28, 70, 20, new TranslationTextComponent("Favorite"), (button) -> {
            ServerList serverList = new ServerList(this.minecraft);
            ServerBrowserList.Entry entry = this.list.getSelected();
            if (entry instanceof ServerBrowserList.BrowsedEntry browsedEntry) {
                ServerData serverData = browsedEntry.getServerData();
                serverList.add(serverData);
                serverList.save();
            }
        }));
        this.addButton(new Button(this.width / 2 - 80, this.height - 28, 156, 20, new TranslationTextComponent("selectServer.refresh"), (button) -> {
            this.refreshServerList();
        }));
        this.addButton(new Button(this.width / 2 + 4 + 76, this.height - 28, 75, 20, DialogTexts.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(null);
        }));

        this.addWidget(list);
        onSelectedChange();
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
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
            URIBuilder builder = new URIBuilder(CommonClient.URL + "/api/v1/servers");
            if (page > 1) {
                builder.addParameter("page", String.valueOf(page));
            }
            for (Filter filter : CommonClient.getInstance().getFilters()) {
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
                servers = new JsonParser().parse(string);
            }
            connection.disconnect();
        } catch (IOException | URISyntaxException ignored) {
            websiteStatus = new StringTextComponent("Website could not be reached at the moment");
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
        this.minecraft.setScreen(new ConnectingScreen(this, this.minecraft, server));
    }

    private void refreshServerList() {
        list.refreshServers();
    }

    public ServerPinger getPinger() {
        return pinger;
    }

    public void setToolTip(List<ITextComponent> toolTip) {
        this.toolTip = toolTip;
    }
}