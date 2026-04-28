package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.util.Window;
import org.agmas.holo.client.render.PhoneHolder;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CubeMapRenderer.class)
public class IncorrectPanoramaMixin {

    @WrapOperation(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/Window;getFramebufferHeight()I"
            )
    )
    public int changeWidth(Window instance, Operation<Integer> original) {
        if (PhoneHolder.renderingPanorama) {
            return 1280;
        }
        return original.call(instance);
    }
    @WrapOperation(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/Window;getFramebufferWidth()I"
            )
    )
    public int changeHeight(Window instance, Operation<Integer> original) {
        if (PhoneHolder.renderingPanorama) {
            return 720;
        }
        return original.call(instance);
    }
}
