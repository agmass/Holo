package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntityRenderer.class)
public abstract class HoloSkinMixin {

    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 4)
    public float visibilityMixin3(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (HoloClient.playersInHolo.containsKey(livingEntity.getUuid())) {
            if (HoloClient.playersInHolo.get(livingEntity.getUuid()).equals(HologramType.SILENT)) {
                return 0.4f;
            }
            return 0.75f;
        }
        return par5;
    }
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 5)
    public float visibilityMixin2(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.playersInHolo.containsKey(livingEntity.getUuid())) {
            if (HoloClient.playersInHolo.get(livingEntity.getUuid()).equals(HologramType.SILENT)) {
                return 0.4f;
            }
            return 0.75f;
        }
        return par5;
    }
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 6)
    public float visibilityMixin4(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.playersInHolo.containsKey(livingEntity.getUuid())) {
            if (HoloClient.playersInHolo.get(livingEntity.getUuid()).equals(HologramType.BATTLE)) {
                return 0.75f;
            }
        }
        return par5;
    }

    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"), index = 2)
    public boolean translucentMixin(boolean translucent, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(livingEntity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(livingEntity.getUuid())) {
                return true;
            }
        }
        return translucent;
    }
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"), index = 3)
    public boolean outlineMixin(boolean outline, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(livingEntity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(livingEntity.getUuid())) {
                return true;
            }
        }
        return outline;
    }
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 7)
    public float visibilityMixin(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(livingEntity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(livingEntity.getUuid())) {
                return (float) HoloClient.shownEntities.get(livingEntity.getUuid()) / 140f;
            }
        }
        if (HoloClient.playersInHolo.containsKey(livingEntity.getUuid())) {
            return 0.5f;
        }
        return par5;
    }
}
