
package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.agmas.holo.state.HoloPlayerComponent;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public abstract class AlarmMakesWorldRedMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;", ordinal = 6))
    Vector3f b(Vector3f instance, Vector3fc other, float t, Operation<Vector3f> original) {
        if (HoloPlayerComponent.KEY.get(MinecraftClient.getInstance().player).phoneExplosion > 0) {
            Vector3f orig = original.call(instance,other,t);
            float intensity = 1-((((HoloPlayerComponent.KEY.get(MinecraftClient.getInstance().player).phoneExplosion+MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)) % 22)+1) /22f);
            return orig.lerp(new Vector3f(10f,1f,1f), intensity);
        }
        return original.call(instance,other,t);
    }

}
