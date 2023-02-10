package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.Config;
import com.epherical.serverbrowser.ConfigSettings;
import com.epherical.serverbrowser.FabricPlatform;
import net.fabricmc.api.ClientModInitializer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ServerBrowserFabClient implements ClientModInitializer {


    public static Set<Filter> filters;
    public static ConfigSettings settings;

    @Override
    public void onInitializeClient() {
        FabricPlatform.create(new FabricPlatform());
        Config config = new Config();
        settings = config.loadConfig();
        filters = new LinkedHashSet<>();
        if (settings != null && settings.modPackFilter.length() > 0)  {
            filters.add(new Filter(settings.modPackFilter, true));
        }
    }

    public static void mergeFilters(List<Filter> filter) {
        filter.removeAll(filters);
        filters.addAll(filter);
    }

    public static void clearAndReset(List<Filter> filter) {
        filters.clear();
        filters.addAll(filter);
    }
}
