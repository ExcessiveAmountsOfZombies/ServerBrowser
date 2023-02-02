package com.epherical.serverbrowser.client;

import net.fabricmc.api.ClientModInitializer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ServerBrowserFabClient implements ClientModInitializer {


    public static Set<Filter> filters;

    @Override
    public void onInitializeClient() {
        filters = new LinkedHashSet<>();
        // TODO; pull from config
        filters.add(new Filter("ATM6", true));
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
