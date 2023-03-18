package com.epherical.serverbrowser.client.fm;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.ScreenButtonGrabber;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.keksuccino.fancymenu.events.RenderWidgetEvent;
import de.keksuccino.konkrete.events.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;

public class FancyMenuEvents {

    private int color = 0xFFFF0000;
    private int time;

    @SubscribeEvent
    public void onRender(RenderWidgetEvent.Post event) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ScreenButtonGrabber title) {
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

                        bufferBuilder.vertex(centerX + (innerRad * cos), centerY + (innerRad * sin), screen.getBlitOffset()).color(color).endVertex();
                        bufferBuilder.vertex(centerX + (outerRad * cos), centerY + (outerRad * sin), screen.getBlitOffset()).color(color).endVertex();
                    }

                    bufferBuilder.end();
                    BufferUploader.end(bufferBuilder);

                    time++;
                }
            }
        }
    }
}
