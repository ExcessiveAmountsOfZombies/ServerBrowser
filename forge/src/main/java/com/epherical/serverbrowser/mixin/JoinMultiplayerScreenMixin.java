package com.epherical.serverbrowser.mixin;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.OfficialServerListing;
import com.epherical.serverbrowser.client.ScreenButtonGrabber;
import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;
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
    public void serverBrowser$modifyRender(MatrixStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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

            serverBrowser$time++;
        }
    }

    @Override
    public Button grabbedButton() {
        return serverBrowser$button;
    }
}
