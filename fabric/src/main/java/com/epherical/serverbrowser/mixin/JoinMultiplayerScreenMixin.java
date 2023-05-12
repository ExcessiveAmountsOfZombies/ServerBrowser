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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen implements ScreenButtonGrabber {


    @Shadow protected ServerSelectionList serverSelectionList;

    @Shadow private Button editButton;

    @Shadow private Button deleteButton;

    private Button serverBrowser$button;

    private int serverBrowser$color = 0xFFFF0000;
    private int serverBrowser$time;

    protected JoinMultiplayerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void serverBrowserAddBrowserButton(CallbackInfo ci) {
        serverBrowser$button = this.addRenderableWidget(Button.builder(Component.translatable("Server Browser"), button -> {
            minecraft.setScreen(new ServerBrowserScreen(this));
            CommonClient.getInstance().getSettings().serverBrowserNotification = false;
            CommonClient.getInstance().saveConfig();
        }).bounds(3, 3, 120, 20).build());
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
        int yPos = serverBrowser$button.getY();
        int xPos = serverBrowser$button.getX();

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

                /*if (j >= (progress + 90)) {
                    bufferBuilder.vertex(centerX + (innerRad * cos), centerY + (innerRad * sin), getBlitOffset()).color(56, 56, 56, 255).endVertex();
                    bufferBuilder.vertex(centerX + (outerRad * cos), centerY + (outerRad * sin), getBlitOffset()).color(56, 56, 56, 255).endVertex();
                } else {

                }*/

                bufferBuilder.vertex(centerX + (innerRad * cos), centerY + (innerRad * sin), 0).color(serverBrowser$color).endVertex();
                bufferBuilder.vertex(centerX + (outerRad * cos), centerY + (outerRad * sin), 0).color(serverBrowser$color).endVertex();
            }

            BufferUploader.drawWithShader(bufferBuilder.end());

            serverBrowser$time++;
        }
    }

    @Override
    public Button grabbedButton() {
        return serverBrowser$button;
    }
}
