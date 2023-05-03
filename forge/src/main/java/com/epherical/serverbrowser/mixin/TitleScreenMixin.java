package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.ScreenButtonGrabber;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainMenuScreen.class)
public abstract class TitleScreenMixin extends Screen implements ScreenButtonGrabber {


    private int serverBrowser$color = 0xFFFF0000;
    private int serverBrowser$time;

    private Button serverBrowser$grabbedMultiplayerButton;

    protected TitleScreenMixin(ITextComponent component) {
        super(component);
    }

    @Redirect(method = "createNormalMenuOptions", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/MainMenuScreen;addButton(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 1))
    public Widget serverBrowser$findMultiplayerButton(MainMenuScreen instance, Widget widget) {
        serverBrowser$grabbedMultiplayerButton = (Button) widget;
        addButton(serverBrowser$grabbedMultiplayerButton);
        return widget;
    }


    @Inject(method = "render", at = @At("TAIL"))
    public void serverBrowser$renderOutline(MatrixStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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

            //RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

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

                int red = (serverBrowser$color >> 24) & 0xFF;
                int green = (serverBrowser$color >> 16) & 0xFF;
                int blue = (serverBrowser$color >> 8) & 0xFF;

                bufferBuilder.vertex(centerX + (innerRad * cos), centerY + (innerRad * sin), getBlitOffset()).color(red, green, blue, 255).endVertex();
                bufferBuilder.vertex(centerX + (outerRad * cos), centerY + (outerRad * sin), getBlitOffset()).color(red, green, blue, 255).endVertex();
            }
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            bufferBuilder.end();
            WorldVertexBufferUploader.end(bufferBuilder);
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            //BufferUploader.end(bufferBuilder);

            serverBrowser$time++;
        }
    }

    @Override
    public Button grabbedButton() {
        return serverBrowser$grabbedMultiplayerButton;
    }
}
