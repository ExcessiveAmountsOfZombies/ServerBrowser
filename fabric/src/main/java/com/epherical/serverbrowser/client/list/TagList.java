package com.epherical.serverbrowser.client.list;

import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.ServerBrowserFabClient;
import com.epherical.serverbrowser.client.screen.FilterServerScreen;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class TagList extends ContainerObjectSelectionList<TagList.Entry> {


    public TagList(FilterServerScreen screen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);

        Iterable<List<Filter>> partition = Iterables.partition(ServerBrowserFabClient.filters.stream()
                .sorted(Comparator.comparing(Filter::isActive).reversed().thenComparing(Filter::getTagName)).collect(Collectors.toList()), 2);
        partition.forEach(filters -> {
            TagEntry entry = new TagEntry(filters.toArray(new Filter[0]));
            addEntry(entry);
        });
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }


    @Environment(EnvType.CLIENT)
    public class TagEntry extends Entry {

        private final List<Checkbox> checkboxes;

        public TagEntry(Filter... checkboxes) {
            this.checkboxes = new ArrayList<>();
            for (Filter checkbox : checkboxes) {
                int width = minecraft.font.width(checkbox.getTagName()) + 20;
                this.checkboxes.add(new Checkbox(0, 0, width + 5, 20, Component.literal(checkbox.getTagName()), checkbox.isActive()));
            }
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int itemWidth = 0;
            for (Checkbox checkbox : checkboxes) {
                checkbox.x = (left + itemWidth);
                checkbox.y = (top);
                itemWidth += checkbox.getWidth();
                checkbox.render(poseStack, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return checkboxes;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return checkboxes;
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

    }

}
