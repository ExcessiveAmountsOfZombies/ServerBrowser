package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.OfficialServerListing;
import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen {


    @Shadow protected ServerSelectionList serverSelectionList;

    @Shadow private Button editButton;

    @Shadow private Button deleteButton;

    protected JoinMultiplayerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void serverBrowserAddBrowserButton(CallbackInfo ci) {
        this.addRenderableWidget(new Button(3, 3, 120, 20, Component.translatable("Server Browser"), button -> {
            minecraft.setScreen(new ServerBrowserScreen(this));
        }));
    }

    @Inject(method = "onSelectedChange", at = @At("TAIL"))
    public void serverBrowserPreventChange(CallbackInfo ci) {
        if (this.serverSelectionList.getSelected() instanceof OfficialServerListing.OfficialEntry) {
            this.editButton.active = false;
            this.deleteButton.active = false;
        }
    }
}
