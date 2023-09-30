package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.list.TagList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FilterServerScreen extends Screen {

    private final Screen previousScreen;

    private Component websiteStatus;

    protected TagList list;

    protected FilterServerScreen(Screen previousScreen) {
        super(Component.translatable("serverbrowser.button.filter"));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        queryTags();
        list = new TagList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 25);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            List<Filter> filters = new ArrayList<>();
            for (TagList.Entry child : list.children()) {
                for (GuiEventListener listener : child.children()) {
                    Checkbox checkbox = (Checkbox) listener;
                    String string = checkbox.getMessage().getString();
                    boolean active = checkbox.selected();
                    filters.add(new Filter(string, active));
                }
            }
            CommonClient.getInstance().clearAndReset(filters);
            this.minecraft.setScreen(previousScreen);
        }).bounds(this.width / 2 + 4 + 76, this.height - 28, 75, 20).build());

        this.addWidget(list);
    }


    public void queryTags() {
        websiteStatus = null;
        try {
            URIBuilder builder = new URIBuilder(CommonClient.URL + "/api/v1/tags");
            URL url = builder.build().toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] bytes = connection.getInputStream().readAllBytes();
                String string = new String(bytes);
                JsonArray element = JsonParser.parseString(string).getAsJsonArray();
                for (JsonElement jsonElement : element) {
                    JsonObject object = jsonElement.getAsJsonObject();
                    String tagName = object.get("tagName").getAsString();
                    String category = object.get("category").getAsString();
                    if (!CommonClient.getInstance().getConfig().modPackFilter.isEmpty() && category.equals("Modpack")) {
                        continue;
                    }
                    List<Filter> filterList = new ArrayList<>();
                    filterList.add(new Filter(tagName));
                    CommonClient.getInstance().mergeFilters(filterList);
                }
            }
            connection.disconnect();
        } catch (URISyntaxException | IOException e) {
            websiteStatus = Component.translatable("serverbrowser.error.unreachable_website");
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (websiteStatus != null) {
            graphics.drawCenteredString(minecraft.font, websiteStatus, width / 2, 40, 0xFFFFFF);
        }
    }
}
