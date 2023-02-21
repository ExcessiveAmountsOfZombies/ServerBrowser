package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerBrowserForgeClient {

    private static CommonClient commonClient;

    public static void initClient() {
        Config config = new Config();
        CommonClient commonClient = new CommonClient(config);
        commonClient.setSettings(config.loadConfig());
    }

}
