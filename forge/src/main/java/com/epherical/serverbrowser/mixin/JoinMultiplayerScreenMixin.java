package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.OfficialServerListing;
import com.epherical.serverbrowser.client.ScreenButtonGrabber;
import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen implements ScreenButtonGrabber {


    @Shadow
    protected ServerSelectionList serverSelectionList;

    @Shadow
    private Button editButton;

    @Shadow
    private Button deleteButton;


    private Button serverBrowser$button;

    private int serverBrowser$color = 0xFFFF0000;
    private int serverBrowser$time;

    protected JoinMultiplayerScreenMixin(ITextComponent pTitle) {
        super(pTitle);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void serverBrowserAddBrowserButton(CallbackInfo ci) {
        serverBrowser$button = this.addButton(new Button(3, 3, 120, 20, new TranslationTextComponent("Server Browser"), button -> {
            minecraft.setScreen(new ServerBrowserScreen(this));
            CommonClient.getInstance().getSettings().serverBrowserNotification = false;
            CommonClient.getInstance().saveConfig();
        }));
    }

    @Inject(method = "onSelectedChange", at = @At("TAIL"))
    public void serverBrowserPreventChange(CallbackInfo ci) {
        if (this.serverSelectionList.getSelected() instanceof OfficialServerListing.OfficialEntry) {
            this.editButton.active = false;
            this.deleteButton.active = false;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void serverBrowser$modifyRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int yPos = serverBrowser$button.y;
        int xPos = serverBrowser$button.x;

        if (CommonClient.displayCircle()) {
            if (serverBrowser$time < 20) {
                serverBrowser$color -= 0x00050000;
            }

            if (serverBrowser$time > 20) {
                serverBrowser$color += 0x00050000;
            }

            if (serverBrowser$time == 40) {
                serverBrowser$color = 0xFFFF0000;
                serverBrowser$time = 0;
            }

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            int segments = 360 + 90;
            double twoPI = Math.PI * 2;

            double innerRad = 0;
            double outerRad = 5;

            double centerX = xPos + 2;
            double centerY = yPos + 2;

            for (int j = 90; j < segments; j++) {
                double sin = Math.sin(-(j * twoPI / 360));
                double cos = Math.cos(-(j * twoPI / 360));

                bufferBuilder.vertex(centerX + (innerRad * cos), centerY + (innerRad * sin), getBlitOffset()).color(serverBrowser$color).endVertex();
                bufferBuilder.vertex(centerX + (outerRad * cos), centerY + (outerRad * sin), getBlitOffset()).color(serverBrowser$color).endVertex();
            }

            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);

            serverBrowser$time++;
        }
    }

    @Override
    public Button grabbedButton() {
        return serverBrowser$button;
    }
}
