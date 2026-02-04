package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import me.shedaniel.autoconfig.AutoConfig;
import net.irisshaders.iris.api.v0.IrisApi;
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

@Mixin(value = LivingEntityRenderer.class, priority = 1900)
public abstract class HoloSkinMixin {

    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"), index = 4)
    public int visibilityMixin(int par3, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(livingEntity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(livingEntity.getUuid())) {
                return new Color(1.0f,1.0f,1.0f, MathHelper.clamp((HoloClient.shownEntities.get(livingEntity.getUuid()) / 140f), 0f,1.0f)).getRGB();
            }
        }
        return par3;
    }
}
