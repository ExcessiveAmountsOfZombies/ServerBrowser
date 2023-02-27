package com.epherical.serverbrowser;

import com.epherical.serverbrowser.client.OfficialServer;

import java.util.ArrayList;
import java.util.List;

public class ConfigSettings {

    public boolean serverBrowserNotification = true;

    public String modPackFilter = "";
    public List<OfficialServer> officialServers = new ArrayList<>();
    public List<String> blacklistedServers = new ArrayList<>();


    public void setModPackFilter(String modPackFilter) {
        this.modPackFilter = modPackFilter;
    }

    public String getModPackFilter() {
        return modPackFilter;
    }

    public List<OfficialServer> getOfficialServers() {
        return officialServers;
    }

    public void setOfficialServers(List<OfficialServer> officialServers) {
        this.officialServers = officialServers;
    }

    public void setBlacklistedServers(List<String> blacklistedServers) {
        this.blacklistedServers = blacklistedServers;
    }

    public List<String> getBlacklistedServers() {
        return blacklistedServers;
    }

    public void addOfficialServer(OfficialServer officialServer) {
        this.officialServers.add(officialServer);
    }
}
