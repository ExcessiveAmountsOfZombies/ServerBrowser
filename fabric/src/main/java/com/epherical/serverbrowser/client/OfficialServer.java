package com.epherical.serverbrowser.client;


public class OfficialServer {

    private String name;
    private String ipAddress;

    public OfficialServer(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
