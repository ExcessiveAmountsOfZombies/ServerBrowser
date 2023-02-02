package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.list.TagList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FilterServerScreen extends Screen {

    private final Screen screen;

    protected TagList list;

    protected FilterServerScreen(Screen previousScreen) {
        super(Component.literal("Filter Servers"));
        this.screen = previousScreen;
    }


    @Override
    protected void init() {
        list = new TagList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 36);

        this.addWidget(list);
    }


    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}
