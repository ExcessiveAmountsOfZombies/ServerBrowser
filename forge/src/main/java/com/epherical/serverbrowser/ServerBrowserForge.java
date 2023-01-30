package com.epherical.serverbrowser;

import com.epherical.serverbrowser.client.ServerBrowserForgeClient;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("serverbrowser")
public class ServerBrowserForge {

    private static ServerBrowserForge mod;


    public ServerBrowserForge() {
        mod = this;
        CommonPlatform.create(new ForgePlatform());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonInit);

        //MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientInit(FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ServerBrowserForgeClient::initClient);
        MinecraftForge.EVENT_BUS.register(new ServerBrowserForgeClient());
    }

    private void commonInit(FMLCommonSetupEvent event) {

    }



}
