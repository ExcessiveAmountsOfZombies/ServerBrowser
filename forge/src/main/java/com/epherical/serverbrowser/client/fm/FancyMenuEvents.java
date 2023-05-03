package com.epherical.serverbrowser.client.fm;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.ScreenButtonGrabber;
import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.fancymenu.events.RenderWidgetEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class FancyMenuEvents {

    private int color = 0xFFFF0000;
    private int time;

    @SubscribeEvent
    public void onRender(RenderWidgetEvent.Post event) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ScreenButtonGrabber) {
            ScreenButtonGrabber title = (ScreenButtonGrabber) screen;
            Button button = title.grabbedButton();
            if (button.equals(event.getWidget())) {
                int yPos = button.y;
                int xPos = button.x;

                if (CommonClient.displayCircle()) {
                    if (time < 20) {
                        color -= 0x00050000;
                    }

                    if (time > 20) {
                        color += 0x00050000;
                    }

                    if (time == 40) {
                        color = 0xFFFF0000;
                        time = 0;
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
                        int red = (color >> 24) & 0xFF;
                        int green = (color >> 16) & 0xFF;
                        int blue = (color >> 8) & 0xFF;

                        bufferBuilder.vertex(centerX + (innerRad * cos), centerY + (innerRad * sin), screen.getBlitOffset()).color(red, green, blue, 255).endVertex();
                        bufferBuilder.vertex(centerX + (outerRad * cos), centerY + (outerRad * sin), screen.getBlitOffset()).color(red, green, blue, 255).endVertex();
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

                    time++;
                }
            }
        }
    }
}
