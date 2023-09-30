package com.epherical.serverbrowser.client;

import com.epherical.epherolib.libs.org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import com.epherical.serverbrowser.config.OfficialServer;
import com.epherical.serverbrowser.config.SBConfig;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CommonClient {

    private static CommonClient client;

    public static String URL = "https://minecraft.multiplayerservers.net";

    private boolean existingSave;

    private Set<Filter> filters;
    private SBConfig config;

    public CommonClient() {
        client = this;
        this.config = new SBConfig(HoconConfigurationLoader.builder(), "serverbrowser.conf");
        this.config.addSerializer(OfficialServer.class, OfficialServer.Serializer.INSTANCE);
        this.config.loadConfig("serverbrowser");
        filters = new LinkedHashSet<>();
        this.setSettings();

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

    public void setSettings() {
        if (!config.modPackFilter.isEmpty())  {
            filters.add(new Filter(config.modPackFilter, true));
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
        this.config.saveConfig();
    }

    public SBConfig getConfig() {
        return this.config;
    }


    public static boolean displayCircle() {
        return getInstance().existingSave && getInstance().config.serverBrowserNotification;
    }

}
