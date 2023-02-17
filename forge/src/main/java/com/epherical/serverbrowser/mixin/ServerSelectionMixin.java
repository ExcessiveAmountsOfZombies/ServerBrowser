package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.OfficialServer;
import com.epherical.serverbrowser.client.OfficialServerListing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.class)
public class ServerSelectionMixin extends ObjectSelectionList {


    @Shadow @Final private JoinMultiplayerScreen screen;

    public ServerSelectionMixin(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Inject(method = "refreshEntries", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0))
    public void serverBrowserAddOfficialServers(CallbackInfo ci) {
        OfficialServerListing listing = new OfficialServerListing(screen, minecraft, 0, 0, 0,0 ,0);
        for (OfficialServer officialServer : CommonClient.getInstance().getSettings().officialServers) {
            ServerData serverData = new ServerData(officialServer.getName(), officialServer.getIpAddress(), false);
            this.addEntry(listing.createEntry(screen, serverData));
        }
    }

}
