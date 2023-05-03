package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.list.TagList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FilterServerScreen extends Screen {

    private final Screen previousScreen;

    private ITextComponent websiteStatus;

    protected TagList list;

    protected FilterServerScreen(Screen previousScreen) {
        super(new StringTextComponent("Filter Servers"));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        queryTags();
        list = new TagList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 25);
        this.addButton(new Button(this.width / 2 + 4 + 76, this.height - 28, 75, 20, DialogTexts.GUI_CANCEL, (button) -> {
            List<Filter> filters = new ArrayList<>();
            for (TagList.Entry child : list.children()) {
                for (IGuiEventListener listener : child.children()) {
                    CheckboxButton checkbox = (CheckboxButton) listener;
                    String string = checkbox.getMessage().getString();
                    boolean active = checkbox.selected();
                    filters.add(new Filter(string, active));
                }
            }
            CommonClient.getInstance().clearAndReset(filters);
            this.minecraft.setScreen(previousScreen);
        }));

        this.addWidget(list);
    }


    public void queryTags() {
        websiteStatus = null;
        try {
            URIBuilder builder = new URIBuilder(CommonClient.URL + "/api/v1/tags");
            URL url = builder.build().toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Server Browser Mod");
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                byte[] bytes = IOUtils.toByteArray((connection.getInputStream()));
                String string = new String(bytes);
                JsonArray element = new JsonParser().parse(string).getAsJsonArray();
                for (JsonElement jsonElement : element) {
                    JsonObject object = jsonElement.getAsJsonObject();
                    String tagName = object.get("tagName").getAsString();
                    String category = object.get("category").getAsString();
                    if (CommonClient.getInstance().getSettings().modPackFilter.length() > 0 && category.equals("Modpack")) {
                        continue;
                    }
                    List<Filter> filterList = new ArrayList<>();
                    filterList.add(new Filter(tagName));
                    CommonClient.getInstance().mergeFilters(filterList);
                }
            }
            connection.disconnect();
        } catch (URISyntaxException | IOException e) {
            websiteStatus = new StringTextComponent("Website could not be reached at the moment");
        }
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (websiteStatus != null) {
            drawCenteredString(poseStack, minecraft.font, websiteStatus, width / 2, 40, 0xFFFFFF);
        }
    }
}
