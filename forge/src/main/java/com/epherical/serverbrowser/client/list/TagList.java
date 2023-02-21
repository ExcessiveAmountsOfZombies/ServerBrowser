package com.epherical.serverbrowser.client.list;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.screen.FilterServerScreen;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TagList extends ExtendedList<TagList.Entry> {


    public TagList(FilterServerScreen screen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);

        Iterable<List<Filter>> partition = Iterables.partition(CommonClient.getInstance().getFilters().stream()
                .sorted(Comparator.comparing(Filter::isActive).reversed().thenComparing(Filter::getTagName)).collect(Collectors.toList()), 2);
        partition.forEach(filters -> {
            TagEntry entry = new TagEntry(filters.toArray(new Filter[0]));
            addEntry(entry);
        });
    }

    protected int getScrollbarPosition() {
        return this.width - 10;
    }

    public int getRowWidth() {
        return this.width - 10;
    }


    public class TagEntry extends Entry {

        private static int widest = 0;

        private final List<CheckboxButton> checkboxes;

        public TagEntry(Filter... checkboxes) {
            this.checkboxes = new ArrayList<>();
            for (Filter checkbox : checkboxes) {
                int width = minecraft.font.width(checkbox.getTagName()) + 20;
                this.checkboxes.add(new CheckboxButton(0, 0, width + 5, 20, new StringTextComponent(checkbox.getTagName()), checkbox.isActive()));
            }
        }

        @Override
        public void render(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int itemWidth = 0;
            int increment = 0;
            for (CheckboxButton checkbox : checkboxes) {
                checkbox.x = (left + itemWidth);
                checkbox.y = (top);
                itemWidth += checkbox.getWidth();
                if (increment == 0 && itemWidth >= widest) {
                    widest = itemWidth;
                }
                if (increment == 1) {
                    checkbox.x = ((left + widest));
                }
                checkbox.render(poseStack, mouseX, mouseY, partialTick);
                increment++;
            }
            increment = 0;
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return checkboxes;
        }
    }

    public abstract static class Entry extends AbstractOptionList.Entry<Entry> {

    }

}
