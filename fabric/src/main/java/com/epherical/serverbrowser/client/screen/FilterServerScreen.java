package com.epherical.serverbrowser.client.screen;

import com.epherical.serverbrowser.Config;
import com.epherical.serverbrowser.client.Filter;
import com.epherical.serverbrowser.client.ServerBrowserFabClient;
import com.epherical.serverbrowser.client.list.TagList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
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

    protected TagList list;

    protected FilterServerScreen(Screen previousScreen) {
        super(Component.literal("Filter Servers"));
        this.previousScreen = previousScreen;
    }


    @Override
    protected void init() {
        queryTags();
        list = new TagList(this, this.minecraft, this.width, this.height, 32, this.height - 64, 25);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            List<Filter> filters = new ArrayList<>();
            for (TagList.Entry child : list.children()) {
                for (GuiEventListener listener : child.children()) {
                    Checkbox checkbox = (Checkbox) listener;
                    String string = checkbox.getMessage().getString();
                    boolean active = checkbox.selected();
                    filters.add(new Filter(string, active));
                }
            }
            ServerBrowserFabClient.getInstance().clearAndReset(filters);
            this.minecraft.setScreen(previousScreen);
        }).bounds(this.width / 2 + 4 + 76, this.height - 28, 75, 20).build());

        this.addWidget(list);
    }


    public void queryTags() {
        try {
            URIBuilder builder = new URIBuilder("http://localhost:8080/api/v1/tags");
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
                    if (ServerBrowserFabClient.getInstance().getSettings().modPackFilter.length() > 0 && category.equals("Modpack")) {
                        continue;
                    }
                    List<Filter> filterList = new ArrayList<>();
                    filterList.add(new Filter(tagName));
                    ServerBrowserFabClient.getInstance().mergeFilters(filterList);
                }
            }
            connection.disconnect();
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}
