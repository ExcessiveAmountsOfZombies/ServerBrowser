package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen {


    protected JoinMultiplayerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void serverBrowserAddBrowserButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.translatable("Server Browser"), button -> {
            minecraft.setScreen(new ServerBrowserScreen(this));
        }).bounds(3, 3, 120, 20).build());
    }
}
