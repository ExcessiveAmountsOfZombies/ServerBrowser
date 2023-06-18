package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.ServerQuery;
import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.ServerPinger;
import com.epherical.serverbrowser.client.list.ServerBrowserList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public class ServerBrowserScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();


    private final ServerPinger pinger = new ServerPinger();
    protected ServerBrowserList list;

    private Button joinButton;
    private Button favoriteButton;

    private Button next;
    private Button prev;

    @Nullable
    private List<Component> toolTip;
    private Component websiteStatus;

    private Screen previousScreen;

    private int page = 1;

    public ServerBrowserScreen(Screen previousScreen) {
        super(Component.nullToEmpty(""));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        list = new ServerBrowserList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
        queryServers();
        list.queryServers();

        next = this.addRenderableWidget(Button.builder(Component.translatable("serverbrowser.button.next_page", (page + 1)), button -> {
            if (list.getEntries().size() >= 20) {
                page++;
                queryServers();
                list.queryServers();
                button.setMessage(Component.translatable("serverbrowser.button.next_page", (page + 1)));
            }
        }).bounds(this.width - 110, 12, 60, 20).build());
        prev = this.addRenderableWidget(Button.builder(Component.translatable("serverbrowser.button.prev_page"), button -> {
            if (page <= 1) {
                page = 1;
            } else {
                page--;
                queryServers();
                list.queryServers();
            }
            next.setMessage(Component.translatable("serverbrowser.button.next_page", (page + 1)));
        }).bounds(this.width - 140, 12, 30, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("serverbrowser.button.register"), button -> {
            this.minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
                if (bl) {
                    Util.getPlatform().openUri("https://minecraft.multiplayerservers.net");
                }
                this.minecraft.setScreen(this);
            }, "https://minecraft.multiplayerservers.net", true));
        }).bounds(this.width / 2 - 50, 3, 100, 20).build());
        this.joinButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), (button) -> {
            this.joinSelectedServer();
        }).bounds(this.width / 2 - 154, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("serverbrowser.button.filter"), (button) -> {
            this.minecraft.setScreen(new FilterServerScreen(this));
        }).bounds(this.width / 2 - 50, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder( Component.translatable("serverbrowser.button.history"), (button) -> {
            // todo; store 20 servers last joined from this screen.
        }).bounds(this.width / 2 + 54, this.height - 52, 100, 20).build());
        this.favoriteButton = this.addRenderableWidget(Button.builder(Component.translatable("serverbrowser.button.favorite"), (button) -> {
            ServerList serverList = new ServerList(this.minecraft);
            ServerBrowserList.Entry entry = this.list.getSelected();
            if (entry instanceof ServerBrowserList.BrowsedEntry browsedEntry) {
                ServerData serverData = browsedEntry.getServerData();
                serverList.add(serverData, false);
                serverList.save();
                this.minecraft.setScreen(new JoinMultiplayerScreen(this));
            }
        }).bounds(this.width / 2 - 154, this.height - 28, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectServer.refresh"), (button) -> {
            this.refreshServerList();
        }).bounds(this.width / 2 - 80, this.height - 28, 156, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }).bounds(this.width / 2 + 4 + 76, this.height - 28, 75, 20).build());

        this.addWidget(list);
        onSelectedChange();
    }

    @Override
    public void render(GuiGraphics poseStack, int mouseX, int mouseY, float partialTick) {
        toolTip = null;
        this.renderBackground(poseStack);

        this.list.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (this.toolTip != null) {
            poseStack.renderComponentTooltip(minecraft.font, this.toolTip, mouseX, mouseY);
        }
        next.active = false;
        prev.active = false;
        if (list.getEntries().size() >= 20) {
            next.active = true;
            //prev.active = true;
        }
        if (page > 1) {
            prev.active = true;
        }


        if (websiteStatus != null) {
            poseStack.drawCenteredString(minecraft.font, websiteStatus, width / 2, 40, 0xFFFFFF);
        }
    }

    public void queryServers() {
        websiteStatus = null;
        ServerQuery main = new ServerQuery(CommonClient.URL + "/api/v1/servers", builder -> {
            if (page > 1) {
                builder.addParameter("page", String.valueOf(page));
            }
            for (Filter filter : CommonClient.getInstance().getFilters()) {
                if (filter.isActive()) {
                    builder.addParameter("type", filter.getTagName());
                }
            }
        }, throwable -> {
            websiteStatus = Component.translatable("serverbrowser.error.unreachable_website");
            return "";
        }, (s, throwable) -> {
            websiteStatus = null;
        });
        main.buildList(main.runQuery(), list, false);

        String packID = CommonClient.getInstance().getSettings().bisectPackID;
        if (packID.length() > 0 && !(page > 1)) {
            // b-ruh
            ServerQuery bisect = new ServerQuery("https://www.bisecthosting.com/api/v1/public_servers", builder -> {
                builder.addParameter("id", packID);
            }, throwable -> {
                LOGGER.warn("Could not ping bisect's servers", throwable);
                return "";
            }, (s, throwable) -> {

            });
            bisect.buildList(bisect.runQuery(), list, true);
        }
        websiteStatus = Component.translatable("serverbrowser.error.unreachable_website");
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
        ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(server.ip), server, false);
    }

    private void refreshServerList() {
        list.refreshServers();
    }

    public ServerPinger getPinger() {
        return pinger;
    }

    public void setToolTip(List<Component> toolTip) {
        this.toolTip = toolTip;
    }
}
