package com.epherical.serverbrowser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * This class exists just so we can call createEntry, so that I can put these entries into the mixin
 * in ServerSelectionMixin
 */
public class OfficialServerListing extends ServerSelectionList {

    private static final Component OFFICIAL = Component.translatable("serverbrowser.server.official");

    public OfficialServerListing(JoinMultiplayerScreen joinMultiplayerScreen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(joinMultiplayerScreen, minecraft, i, j, k, l, m);
    }

    public OfficialEntry createEntry(JoinMultiplayerScreen screen, ServerData data) {
        return new OfficialEntry(screen, data);
    }

    public class OfficialEntry extends ServerSelectionList.OnlineServerEntry {

        public OfficialEntry(JoinMultiplayerScreen joinMultiplayerScreen, ServerData serverData) {
            super(joinMultiplayerScreen, serverData);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, isMouseOver, partialTick);
            guiGraphics.drawString(minecraft.font, OFFICIAL, left - 45, top + 11, 0xFFFF00);
        }
    }
}
