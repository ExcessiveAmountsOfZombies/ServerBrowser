package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.ScreenButtonGrabber;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen implements ScreenButtonGrabber {


    private int serverBrowser$color = 0xFFFF0000;
    private int serverBrowser$time;

    private Button serverBrowser$grabbedMultiplayerButton;

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Redirect(method = "createNormalMenuOptions", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 1))
    public GuiEventListener serverBrowser$findMultiplayerButton(TitleScreen instance, GuiEventListener guiEventListener) {
        serverBrowser$grabbedMultiplayerButton = (Button) guiEventListener;
        addRenderableWidget(serverBrowser$grabbedMultiplayerButton);
        return guiEventListener;
    }


    @Inject(method = "render", at = @At("TAIL"))
    public void serverBrowser$renderOutline(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int yPos = serverBrowser$grabbedMultiplayerButton.y;
        int xPos = serverBrowser$grabbedMultiplayerButton.x;

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
        return serverBrowser$grabbedMultiplayerButton;
    }
}
