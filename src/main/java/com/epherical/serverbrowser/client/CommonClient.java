package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.ConfigSettings;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CommonClient {

    private static CommonClient client;

    public static String URL = "https://minecraft.multiplayerservers.net";

    private Set<Filter> filters;
    private ConfigSettings settings;

    public CommonClient() {
        client = this;
        filters = new LinkedHashSet<>();
        if (settings != null && settings.modPackFilter.length() > 0)  {
            filters.add(new Filter(settings.modPackFilter, true));
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

    public ConfigSettings getSettings() {
        return settings;
    }

}
