package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.agmas.holo.client.HoloClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntityRenderer.class)
public abstract class HoloSkinMixin {

    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 4)
    public float visibilityMixin3(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.playersInHolo.contains(livingEntity.getUuid())) {
            return 0.75f;
        }
        return par5;
    }
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 5)
    public float visibilityMixin2(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.playersInHolo.contains(livingEntity.getUuid())) {
            return 0.75f;
        }
        return par5;
    }
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index = 7)
    public float visibilityMixin(float par5, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.playersInHolo.contains(livingEntity.getUuid())) {
            return 0.5f;
        }
        return par5;
    }
}
