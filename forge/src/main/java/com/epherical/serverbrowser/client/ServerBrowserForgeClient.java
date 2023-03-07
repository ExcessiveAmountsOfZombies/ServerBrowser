package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.Config;
import com.epherical.serverbrowser.client.fm.FancyMenuEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

@OnlyIn(Dist.CLIENT)
public class ServerBrowserForgeClient {

    private static CommonClient commonClient;

    public static void initClient() {
        Config config = new Config();
        CommonClient commonClient = new CommonClient(config);
        commonClient.setSettings(config.loadConfig());

        if (ModList.get().isLoaded("fancymenu")) {
            MinecraftForge.EVENT_BUS.register(new FancyMenuEvents());
        }
    }

}
