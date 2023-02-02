package com.epherical.serverbrowser.client.list;

import com.epherical.serverbrowser.client.screen.FilterServerScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TagList extends ObjectSelectionList<ServerBrowserList.Entry> {


    public TagList(FilterServerScreen screen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }


    @Environment(EnvType.CLIENT)
    public class TagEntry extends Entry {

        private final Checkbox[] checkboxes;

        public TagEntry(Checkbox... checkboxes) {
            this.checkboxes = checkboxes;
        }




        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {

        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return null;
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

    }

}
