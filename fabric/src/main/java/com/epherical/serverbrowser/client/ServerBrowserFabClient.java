package com.epherical.serverbrowser.client;

import net.fabricmc.api.ClientModInitializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerBrowserFabClient implements ClientModInitializer {


    public static Set<Filter> filters;

    @Override
    public void onInitializeClient() {
        filters = new HashSet<>();
        // TODO; pull from config
        filters.add(new Filter("ATM6", true));
    }

    public static void mergeFilters(List<Filter> filter) {
        filter.removeAll(filters);
        filters.addAll(filter);
    }
}
