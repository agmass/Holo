package org.agmas.holo.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
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
import foundry.veil.api.client.util.Easing;
import foundry.veil.impl.client.render.pipeline.VeilFirstPersonRenderer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import org.agmas.holo.Holo;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class PhoneHolder {
    public static Matrix4f renderProj = new Matrix4f();
    public static PhoneState phoneState = PhoneState.CAMERA;
    public static int battery = 100;
    public static PhoneScreenTexture phoneScreenTexture = new PhoneScreenTexture();
    public static Vec2f cursorPos = new Vec2f(0,0);
    private static final Identifier CROSSHAIR_TEXTURE = Identifier.ofVanilla("hud/crosshair");

    public static boolean renderingPanorama = false;
    public static int panoramas = 4;
    public static float appTransition = 1;
    public static float rotationTransition = 0;
    public static CubeMapRenderer PANORAMA = new CubeMapRenderer(Identifier.of("holo", "textures/gui/title/background/" + new Random().nextInt(panoramas) + "/panorama"));
    public static RotatingCubeMapRenderer ROTATING_PANORAMA;

    public static void transform(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0,-0.5,0);
        matrices.translate(0, Math.clamp(MathHelper.lerp((player.getItemUseTime()+tickDelta)/10f, 0f, 0.5f),0f,0.5f),0);
    }
    public static void renderPhoneBuffer() {
        AdvancedFbo phoneBuffer = VeilRenderSystem.renderer()
                .getFramebufferManager()
                .getFramebuffer(Identifier.of("holo:phone_buffer_vertical"));
        boolean vertical = !phoneState.equals(PhoneState.CAMERA);
        if (!vertical) {
            phoneBuffer = VeilRenderSystem.renderer()
                    .getFramebufferManager()
                    .getFramebuffer(Identifier.of("holo:phone_buffer"));
        }

        if (vertical) {
            rotationTransition -= MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration()*0.1f;
            if (rotationTransition < 0) rotationTransition = 0;
        } else {
            rotationTransition += MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration()*0.1f;
            if (rotationTransition > 1) rotationTransition = 1;
        }

        if (cursorPos.x > 620) cursorPos = new Vec2f(620, cursorPos.y);
        if (cursorPos.y > 1000) cursorPos = new Vec2f(cursorPos.x,1000);
        if (cursorPos.x < -32) cursorPos = new Vec2f(-32, cursorPos.y);
        if (cursorPos.y < 0) cursorPos = new Vec2f(cursorPos.x,0);

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
                            vertical ? 720 : 1280,
                            vertical ? 1280 : 720,
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

            if (phoneState.equals(PhoneState.HOME)) {
                renderingPanorama = true;
                ROTATING_PANORAMA.render(drawContext,1280,720,1,MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)/2.0f);
                renderingPanorama = false;

                drawContext.getMatrices().push();
                drawContext.getMatrices().scale(3,3,1);

                renderAppIcon(drawContext,32,48,"call",Text.literal("Calls"),PhoneState.CALL);
                renderAppIcon(drawContext,128,48,"tiktok",Text.literal("Minetok"),PhoneState.DOOMSCROLLING);
                renderAppIcon(drawContext,32,128,"camera",Text.literal("Holo Feed").formatted(Formatting.BLUE),PhoneState.CAMERA);

                drawContext.getMatrices().pop();
            }
            if (phoneState.equals(PhoneState.CAMERA)) {
                renderCameraUI(drawContext);
            }
            if (phoneState.equals(PhoneState.CALL)) {
                renderCallsMenuUI(drawContext);
            }

            drawContext.getMatrices().push();
            if (vertical) {
                drawContext.getMatrices().scale(6,6,1);
            }
            drawContext.drawText(MinecraftClient.getInstance().textRenderer, Text.of(battery + "%"), 6,12,-1,true);
            drawContext.getMatrices().pop();

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

            if (!phoneState.equals(PhoneState.CAMERA)) {
                drawContext.drawGuiTexture(CROSSHAIR_TEXTURE, (int) cursorPos.x, (int) cursorPos.y, 128, 128);
            }

            RenderSystem.defaultBlendFunc();

            appTransition -= MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration()*0.1f;
            if (appTransition < 0) appTransition = 0;
            int alpha = MathHelper.lerp(Easing.EASE_OUT_SINE.ease(appTransition), 0, 255);
            if (alpha > 255) alpha = 255;
            if (alpha < 0) alpha = 0;
            drawContext.fill(-2000,-2000,2000,2000,new Color(0,0,0,alpha).getRGB());

            bufferSource.draw();
            matrix4fStack.popMatrix();
            phoneScreenTexture.copy(phoneBuffer);
            AdvancedFbo.unbind();
        }
    }

    public static void renderAppIcon(DrawContext context, int x, int y, String textureName, Text text, PhoneState endState) {
        context.getMatrices().push();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int lengthOfText = textRenderer.getWidth(text);
        context.getMatrices().translate(x,y,0);

        if ((x*3 < cursorPos.x+28 || x*3 < cursorPos.x+100) && ((x+48)*3 > cursorPos.x+28 || (x+48)*3 > cursorPos.x+100)) {
            if ((y*3 < cursorPos.y+28 || y*3 < cursorPos.y+100) && ((y+48)*3 > cursorPos.y+28 || (y+48)*3> cursorPos.y+100)) {
                context.fill(-8,-8,56,64, new Color(160,160,255,200).getRGB());
                if (MinecraftClient.getInstance().mouse.wasLeftButtonClicked()) {
                    phoneState = endState;
                    appTransition = 1;

                    AdvancedFbo phoneBuffer = VeilRenderSystem.renderer()
                            .getFramebufferManager()
                            .getFramebuffer(Identifier.of("holo:phone_buffer"));
                    AdvancedFbo phoneBufferVertical = VeilRenderSystem.renderer()
                            .getFramebufferManager()
                            .getFramebuffer(Identifier.of("holo:phone_buffer_vertical"));
                    phoneBuffer.clear();
                    phoneBufferVertical.clear();
                }
            }
        }
        context.drawTexture(Identifier.of("holo", "textures/gui/sprites/phone/" + textureName + ".png"),0,0,0,0,48,48,48,48);

        context.drawText(textRenderer,text,-(int)(lengthOfText/2f) + 24,50,-1,true);

        context.getMatrices().pop();
    }

    public static void render(ItemRenderer itemRenderer, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        float swing = 0;
        if (swingProgress < 0.5) {
            swing = MathHelper.lerp(swingProgress*2, 0f, 1f);
        } else if (swingProgress > 0.5) {
            swing = MathHelper.lerp((swingProgress-0.5f)*2, 1f, 0f);
        }
        matrices.translate(0,0,-(swing/8));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swing*-5));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swing*10));
        if (player.getActiveHand().equals(Hand.OFF_HAND)) {
            PhoneHolder.phoneState = PhoneState.CAMERA;
        }
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90*rotationTransition));
        matrices.translate(0,0,-0.7);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);

        itemRenderer.renderItem(ModItems.SEETHROUGH_PHONE.getDefaultStack(), ModelTransformationMode.NONE,false,matrices,vertexConsumers,light,OverlayTexture.DEFAULT_UV,
                itemRenderer.getModel(ModItems.SEETHROUGH_PHONE.getDefaultStack(),player.getWorld(),player,0));

        matrices.translate(0,-0.009,0.03);
        float width = 0.18f;
        float height = 0.32f;
        width *= 1.1f;
        height *= 1.1f;


        if (!PhoneHolder.phoneState.equals(PhoneHolder.PhoneState.CAMERA)) {
            builder.vertex(matrices.peek().getPositionMatrix(), -width, -height, 0).texture(0, 0).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), width, -height, 0).texture(1, 0).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), width, height, 0).texture(1, 1).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), -width, height, 0).texture(0, 1).light(light).color(-1);
        } else {
            builder.vertex(matrices.peek().getPositionMatrix(), -width, -height, 0).texture(1, 0).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), width, -height, 0).texture(1, 1).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), width, height, 0).texture(0, 1).light(light).color(-1);
            builder.vertex(matrices.peek().getPositionMatrix(), -width, height, 0).texture(0, 0).light(light).color(-1);
        }

        RenderSystem.setShaderTexture(0, phoneScreenTexture.getGlId());
        VeilRenderType.get(Identifier.of(Holo.MOD_ID, "phone")).draw(builder.end());
        matrices.pop();
    }

    public static void renderCameraUI(DrawContext drawContext) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().scale(8,8,1);
        if (availableHolos.isEmpty()) {
            drawContext.drawText(MinecraftClient.getInstance().textRenderer,Text.literal(":: ").formatted(Formatting.RED).append(Text.literal("NO HOLOS AVAILABLE")), 8,6,-1,true);
        } else {
            if (HoloPlayerComponent.KEY.get(availableHolos.get(selectedHolo)).hologramType.equals(HologramType.BATTLE_DUEL)) {
                drawContext.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(":: ").formatted(Formatting.DARK_RED).append(availableHolos.get(selectedHolo).getName()), 8, 6, -1, true);
            } else {
                drawContext.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(":: ").formatted(Formatting.AQUA).append(availableHolos.get(selectedHolo).getName()), 8, 6, -1, true);
            }
        }
        drawContext.getMatrices().pop();
    }
    public static void renderCallsMenuUI(DrawContext drawContext) {
        drawContext.getMatrices().push();
        drawContext.drawTexture(Identifier.of("minecraft", "textures/block/end_stone.png"),0,0,0,0,720,1280,64,64);

        drawContext.getMatrices().translate(32f,128f,0);
        for (PlayerListEntry playerListEntry : MinecraftClient.getInstance().getNetworkHandler().getListedPlayerListEntries()) {
            PlayerSkinDrawer.draw(drawContext,playerListEntry.getSkinTextures(),0,0,48);
            Text name = playerListEntry.getDisplayName();
            if (name == null) name = Text.of(playerListEntry.getProfile().getName());
            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(4,4,1);
            drawContext.drawText(MinecraftClient.getInstance().textRenderer, name,16,2,-1,true);
            drawContext.getMatrices().pop();
            drawContext.getMatrices().translate(0,64f,0);
        }

        drawContext.getMatrices().pop();
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

        if (MinecraftClient.getInstance().player.getActiveItem().isOf(ModItems.PHONE) && PhoneHolder.phoneState.equals(PhoneHolder.PhoneState.CAMERA) && !availableHolos.isEmpty()) {
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
