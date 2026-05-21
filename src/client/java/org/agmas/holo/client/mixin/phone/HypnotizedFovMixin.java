package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.HoloClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class HypnotizedFovMixin {

    @WrapMethod(
            method = {"getFov"}
    )
    public double noPhoneSlowdown(Camera camera, float tickDelta, boolean changingFov, Operation<Double> original) {
        if (HoloClient.hypnotization > 0) {
            return 60+(Math.sin((HoloClient.hypnotization-tickDelta)*0.25f)*10);
        }
        return original.call(camera,tickDelta,changingFov);
    }
}
