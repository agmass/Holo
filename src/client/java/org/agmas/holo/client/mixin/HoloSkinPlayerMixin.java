package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.client.duck.PlayerModelMixinDuck;
import org.agmas.holo.client.models.WardensHorns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(PlayerEntityRenderer.class)
public class HoloSkinPlayerMixin {

    @Inject(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/PlayerEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER), cancellable = true)
    public void visibilityMixin2(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, @Local(argsOnly = true) AbstractClientPlayerEntity livingEntity) {
        if (HoloClient.playersInHolo.containsKey(livingEntity.getUuid())) {
            ci.cancel();
            arm.pitch = 0.0F;
            arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(player.getSkinTextures().texture())), light, OverlayTexture.DEFAULT_UV, HoloClient.HOLO_COLOR);
            sleeve.pitch = 0.0F;
            sleeve.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTextures().texture())), light, OverlayTexture.DEFAULT_UV, HoloClient.HOLO_COLOR);
        }
    }
}
