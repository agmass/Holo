package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class AlwaysOutlineSilentMixin {

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    public void outlineMixin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType.equals(HologramType.SILENT) && !(entity instanceof ClientPlayerEntity) && HoloClient.shownEntities.containsKey(entity.getUuid())) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
