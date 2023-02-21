package com.epherical.serverbrowser.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;

/**
 * This class exists just so we can call createEntry, so that I can put these entries into the mixin
 * in ServerSelectionMixin
 */
public class OfficialServerListing extends ServerSelectionList {

    public OfficialServerListing(MultiplayerScreen joinMultiplayerScreen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(joinMultiplayerScreen, minecraft, i, j, k, l, m);
    }

    public OfficialEntry createEntry(MultiplayerScreen screen, ServerData data) {
        return new OfficialEntry(screen, data);
    }

    public class OfficialEntry extends ServerSelectionList.NormalEntry {

        public OfficialEntry(MultiplayerScreen joinMultiplayerScreen, ServerData serverData) {
            super(joinMultiplayerScreen, serverData);
        }

        @Override
        public void render(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(poseStack, index, top, left, width, height, mouseX, mouseY, isMouseOver, partialTick);
            minecraft.font.drawShadow(poseStack, "Official", left - 45, top + 11, 0xFFFF00);
        }
    }
}
