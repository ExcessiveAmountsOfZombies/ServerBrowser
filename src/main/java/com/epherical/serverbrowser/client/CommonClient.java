package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.Config;
import com.epherical.serverbrowser.ConfigSettings;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CommonClient {

    private static CommonClient client;

    public static String URL = "https://minecraft.multiplayerservers.net";

    private boolean existingSave;

    private Set<Filter> filters;
    private ConfigSettings settings;
    private Config config;

    public CommonClient(Config config) {
        client = this;
        this.config = config;
        filters = new LinkedHashSet<>();
        File file = new File(Minecraft.getInstance().gameDirectory, "saves");
        File[] files = file.listFiles();
        if (files != null) {
            existingSave = files.length > 0;
        } else {
            existingSave = false;
        }

    }

    public void mergeFilters(List<Filter> filter) {
        filter.removeAll(filters);
        filters.addAll(filter);
    }

    public void clearAndReset(List<Filter> filter) {
        filters.clear();
        filters.addAll(filter);
    }

    public void setSettings(ConfigSettings settings) {
        this.settings = settings;
        if (settings.modPackFilter.length() > 0)  {
            filters.add(new Filter(settings.modPackFilter, true));
        }
    }

    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    public Set<Filter> getFilters() {
        return filters;
    }

    public static CommonClient getInstance() {
        return client;
    }

    public void saveConfig() {
        try {
            this.config.saveFile(settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public ConfigSettings getSettings() {
        return settings;
    }

    public static boolean displayCircle() {
        return getInstance().existingSave && getInstance().getSettings().serverBrowserNotification;
    }

}
