package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(LivingEntityRenderer.class)
public abstract class HoloSkinMixin {

    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"), index = 2)
    public boolean translucentMixin(boolean translucent, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(livingEntity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(livingEntity.getUuid())) {
                return true;
            }
        }
        return translucent;
    }
    @Inject(method = "getRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;", shift = At.Shift.AFTER), cancellable = true)
    public void a(LivingEntity entity, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<RenderLayer> cir) {
        if (HoloClient.playersInHolo.containsKey(entity.getUuid())) {
            cir.setReturnValue(VeilRenderType.get(Identifier.of("holo","scanline"),((LivingEntityRenderer)(Object)this).getTexture(entity)));
            cir.cancel();
        }
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
    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"), index = 4)
    public int visibilityMixin(int par3, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(livingEntity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(livingEntity.getUuid())) {
                return new Color(1.0f,1.0f,1.0f, MathHelper.clamp((HoloClient.shownEntities.get(livingEntity.getUuid()) / 140f), 0f,1.0f)).getRGB();
            }
        }
        if (HoloClient.playersInHolo.containsKey(livingEntity.getUuid())) {
            return HoloClient.HOLO_COLOR;
        }
        return par3;
    }
}
