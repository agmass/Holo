package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.agmas.holo.client.render.PhoneHolder;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class CameraHoloNoBodyMixin {
    @WrapOperation(method = "setModelPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isSpectator()Z"))
    private static boolean startyt(AbstractClientPlayerEntity instance, Operation<Boolean> original) {
        if (HoloPlayerComponent.KEY.get(instance).hologramType.equals(HologramType.CAMERA)) {
            return true;
        }
        return original.call(instance);
    }
}
