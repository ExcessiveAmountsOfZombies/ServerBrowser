package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.OfficialServerListing;
import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen {


    @Shadow
    protected ServerSelectionList serverSelectionList;

    @Shadow
    private Button editButton;

    @Shadow
    private Button deleteButton;

    protected JoinMultiplayerScreenMixin(ITextComponent pTitle) {
        super(pTitle);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void serverBrowserAddBrowserButton(CallbackInfo ci) {
        this.addButton(new Button(3, 3, 120, 20, new TranslationTextComponent("Server Browser"), button -> {
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
