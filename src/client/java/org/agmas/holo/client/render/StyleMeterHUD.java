package org.agmas.holo.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holo.state.StyleMeterComponent;

import java.awt.*;

public class StyleMeterHUD {

    public static float lastStylePoints = 0;
    public static Color colorWithOpacity(Color color, PlayerEntity player) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), StyleMeterComponent.KEY.get(player).c_totalStyleMeter > 25 ? 255 : 50);
    }
    public static void render(ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter) {
        context.getMatrices().push();
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(10));
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-2));

        float tickDelta = tickCounter.getTickDelta(false);

        float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees((player.getPitch(tickDelta) - h) * 0.1F));
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees((player.getYaw(tickDelta) - i) * 0.1F));

        context.getMatrices().translate(context.getScaledWindowWidth()-120,context.getScaledWindowHeight()-70,0);

        context.getMatrices().push();
        if (StyleMeterComponent.KEY.get(player).c_totalStyleMeter <= 25) {
            context.setShaderColor(1f, 1f, 1f, 0.1f);
        } else {
            context.setShaderColor(1f, 1f, 1f, 0.25f);
        }
        context.getMatrices().translate(0,0,-0.2f);
        context.fill(-10,-110,110,10,Color.GRAY.getRGB());
        context.setShaderColor(1f,1f,1f,1f);
        context.getMatrices().pop();

        context.getMatrices().push();
        StyleMeterComponent.KEY.get(player).stylePoints.forEach(((reason, points) -> {
            context.getMatrices().translate(0,-16f,0f);
            Color color = colorWithOpacity(reason.pointsPerTick > 10 ? Color.YELLOW : Color.CYAN, player);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(reason.toString() + " x" + Math.round(points.combo)),0,-10, color.getRGB(),true);
        }));
        context.getMatrices().pop();

        renderBar(player,context,tickCounter);
        renderComboText(player,context,tickCounter);
        context.getMatrices().pop();
    }

    public static Color getColorFromPoints(float points) {
        return new Color(255, Math.clamp(MathHelper.lerp(points, 255, 0), 0, 255), Math.clamp(MathHelper.lerp(points, 64, 0),0, 255));
    }
    public static void renderComboText(PlayerEntity player, DrawContext context, RenderTickCounter tickCounter) {
        context.getMatrices().push();
        context.getMatrices().scale(1.5f,1.5f,1.5f);
        Color color = colorWithOpacity(Color.WHITE, player);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("COMBO: x" + StyleMeterComponent.KEY.get(player).consecutiveHits),0,-10,color.getRGB(),true);
        context.getMatrices().pop();
    }
    public static void renderBar(PlayerEntity player, DrawContext context, RenderTickCounter tickCounter) {
        context.getMatrices().push();
        int m = Math.round(StyleMeterComponent.KEY.get(player).c_totalStyleMeter/6f);

        context.getMatrices().push();
        context.getMatrices().translate(0,0,-0.1f);
        context.fill(0,0,100,8, colorWithOpacity(Color.GRAY,player).getRGB());
        context.getMatrices().pop();
        context.fill(0,0, (int) MathHelper.lerp(tickCounter.getTickDelta(false), lastStylePoints, m),8, colorWithOpacity(getColorFromPoints(StyleMeterComponent.KEY.get(player).c_totalStyleMeter/600f),player).getRGB());
        context.getMatrices().pop();
    }
}
