package org.agmas.holo.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.impl.client.render.pipeline.VeilFirstPersonRenderer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holo.Holo;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class PhoneHolder {
    public static Matrix4f renderProj = new Matrix4f();
    public static PhoneState phoneState = PhoneState.CAMERA;
    public static PhoneScreenTexture phoneScreenTexture = new PhoneScreenTexture();
    public static void transform(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0,-0.5,0);
        matrices.translate(0, Math.clamp(MathHelper.lerp((player.getItemUseTime()+tickDelta)/10f, 0f, 0.5f),0f,0.5f),0);
    }
    public static void renderPhoneBuffer() {
        AdvancedFbo phoneBuffer = VeilRenderSystem.renderer()
                .getFramebufferManager()
                .getFramebuffer(Identifier.of("holo:phone_buffer"));

        if (phoneBuffer != null) {
            updateCameraBuffer(phoneBuffer);

            AdvancedFbo drawFbo = VeilRenderSystem.renderer().getDynamicBufferManger().getDynamicFbo(phoneBuffer);

            VertexConsumerProvider.Immediate bufferSource = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
            bufferSource.draw();

            drawFbo.bind(true);
            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            Matrix4f matrix4f = new Matrix4f()
                    .setOrtho(
                            0.0F,
                            1280,
                            720,
                            0.0F,
                            1000.0F,
                            21000.0F
                    );
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.translation(0.0F, 0.0F, -11000.0F);
            RenderSystem.applyModelViewMatrix();
            DrawContext drawContext = new DrawContext(MinecraftClient.getInstance(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());

            phoneState = PhoneState.CAMERA;
            if (phoneState.equals(PhoneState.CAMERA)) {
                renderCameraUI(drawContext);
            }
            bufferSource.draw();
            matrix4fStack.popMatrix();
            phoneScreenTexture.copy(phoneBuffer);
            AdvancedFbo.unbind();
        }
    }

    public static void render(ItemRenderer itemRenderer, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        HoloClient.phoneCameraMode = player.getActiveHand().equals(Hand.OFF_HAND);
        matrices.push();
        if (HoloClient.phoneCameraMode) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
        }
        matrices.translate(0,0,-0.7);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);

        itemRenderer.renderItem(item, ModelTransformationMode.NONE,false,matrices,vertexConsumers,light,OverlayTexture.DEFAULT_UV,
                itemRenderer.getModel(item,player.getWorld(),player,0));

        matrices.translate(0,0,0.035);
        if (!HoloClient.phoneCameraMode) {
            builder.vertex(matrices.peek().getPositionMatrix(), -0.186f, -0.25f, 0).texture(0, 0).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), 0.186f, -0.25f, 0).texture(1, 0).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), 0.186f, 0.25f, 0).texture(1, 1).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), -0.186f, 0.25f, 0).texture(0, 1).light(light).color(-1);
        } else {
            builder.vertex(matrices.peek().getPositionMatrix(), -0.186f, -0.25f, 0).texture(1, 0).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), 0.186f, -0.25f, 0).texture(1, 1).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), 0.186f, 0.25f, 0).texture(0, 1).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), -0.186f, 0.25f, 0).texture(0, 0).light(light).color(-1);
        }

        RenderSystem.setShaderTexture(0, phoneScreenTexture.getGlId());
        VeilRenderType.get(Identifier.of(Holo.MOD_ID, "phone")).draw(builder.end());
        matrices.pop();
    }

    public static void renderCameraUI(DrawContext drawContext) {
        drawContext.getMatrices().scale(8,8,8);
        if (availableHolos.isEmpty()) {
            drawContext.drawText(MinecraftClient.getInstance().textRenderer,Text.literal(":: ").formatted(Formatting.RED).append(Text.literal("NO HOLOS AVAILABLE")), 2,2,-1,true);
        } else {
            if (HoloPlayerComponent.KEY.get(availableHolos.get(selectedHolo)).hologramType.equals(HologramType.BATTLE_DUEL)) {
                drawContext.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(":: ").formatted(Formatting.DARK_RED).append(availableHolos.get(selectedHolo).getName()), 2, 2, -1, true);
            } else {
                drawContext.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(":: ").formatted(Formatting.AQUA).append(availableHolos.get(selectedHolo).getName()), 2, 2, -1, true);
            }
        }
    }
    public static int selectedHolo = 0;
    public static List<PlayerEntity> availableHolos;
    public static void updateCameraBuffer(AdvancedFbo phoneBuffer) {

        availableHolos = MinecraftClient.getInstance().world.getEntitiesByClass(PlayerEntity.class, MinecraftClient.getInstance().player.getBoundingBox().expand(1000), (fakestPlayer -> {
            return
                    (HoloPlayerComponent.KEY.get(fakestPlayer).ownerUUID != null && HoloPlayerComponent.KEY.get(fakestPlayer).ownerUUID.equals(MinecraftClient.getInstance().player.getUuid())) ||
                    HoloPlayerComponent.KEY.get(fakestPlayer).hologramType.equals(HologramType.BATTLE_DUEL);
        }));
        if (selectedHolo >= availableHolos.size()) {
            selectedHolo = 0;
        }

        if (HoloClient.phoneCameraMode && !availableHolos.isEmpty()) {
            renderProj.setPerspective(49.5f, 1280f / 720f, 0.6F, 16 * 4);
            Vector3d pos = new Vector3d(0,0,0);
            float yaw = 0;
            float pitch = 0;
            if (availableHolos.get(selectedHolo) != null) {
                pos = new Vector3d(availableHolos.get(selectedHolo).getEyePos().x, availableHolos.get(selectedHolo).getEyePos().y, availableHolos.get(selectedHolo).getEyePos().z);
                yaw = availableHolos.get(selectedHolo).headYaw;
                pitch = availableHolos.get(selectedHolo).getPitch();
            }
            Quaternionf quart = new Quaternionf();
            quart.mul(RotationAxis.NEGATIVE_X.rotationDegrees(180+pitch));
            quart.mul(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
            VeilLevelPerspectiveRenderer.render(phoneBuffer, new Matrix4f(), renderProj, pos, quart, 16, MinecraftClient.getInstance().getRenderTickCounter(), false);
        }
    }

    public enum PhoneState {
        HOME,
        CAMERA,
        CALL,
        INCOMING_CALL,
        OUTGOING_CALL,
        DOOMSCROLLING
    }

}
