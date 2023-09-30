package com.epherical.serverbrowser.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerBrowserForgeClient {

    private static CommonClient commonClient;

    public static void initClient() {
        CommonClient commonClient = new CommonClient();
    }

}
