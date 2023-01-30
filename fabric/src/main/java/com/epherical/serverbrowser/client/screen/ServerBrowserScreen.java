package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.ServerBrowserList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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

    public ServerBrowserScreen(Screen previousScreen) {
        super(Component.nullToEmpty(""));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        list = new ServerBrowserList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
        list.queryServers();

        this.joinButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), (button) -> {
            /*this.joinSelectedServer();*/
        }).bounds(this.width / 2 - 154, this.height - 52, 100, 20).build());
        this.joinButton.active = false;
        this.favoriteButton = this.addRenderableWidget(Button.builder(Component.translatable("Favorite"), (button) -> {
           /* ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
            if (entry instanceof ServerSelectionList.OnlineServerEntry) {
                ServerData serverData = ((ServerSelectionList.OnlineServerEntry)entry).getServerData();
                this.editingServer = new ServerData(serverData.name, serverData.ip, false);
                this.editingServer.copyFrom(serverData);
                this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
            }*/
        }).bounds(this.width / 2 - 154, this.height - 28, 70, 20).build());
        this.favoriteButton.active = false;
        this.addRenderableWidget(Button.builder(Component.translatable("selectServer.refresh"), (button) -> {
            this.refreshServerList();
        }).bounds(this.width / 2 - 80, this.height - 28, 156, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(this.previousScreen);
        }).bounds(this.width / 2 + 4 + 76, this.height - 28, 75, 20).build());

        this.addWidget(list);
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
