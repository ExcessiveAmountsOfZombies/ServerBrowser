package com.epherical.serverbrowser.client;

import com.epherical.serverbrowser.FabricPlatform;
import com.epherical.serverbrowser.client.fm.FancyMenuEvents;
import de.keksuccino.konkrete.Konkrete;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ServerBrowserFabClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricPlatform.create(new FabricPlatform());
        CommonClient commonClient = new CommonClient();

        if (FabricLoader.getInstance().isModLoaded("fancymenu")) {
            Konkrete.getEventHandler().registerEventsFrom(new FancyMenuEvents());
        }
    }
}
