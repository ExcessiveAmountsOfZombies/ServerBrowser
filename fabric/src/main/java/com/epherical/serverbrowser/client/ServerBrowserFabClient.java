package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.Config;
import com.epherical.serverbrowser.FabricPlatform;
import net.fabricmc.api.ClientModInitializer;

public class ServerBrowserFabClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricPlatform.create(new FabricPlatform());
        Config config = new Config();
        CommonClient commonClient = new CommonClient();
        commonClient.setSettings(config.loadConfig());
    }
}
