package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.render.PhoneHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class LessViewBobbingMixin {


    @WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    public void disableWhenWatchingCams(MatrixStack instance, float x, float y, float z, Operation<Void> original) {
        if (MinecraftClient.getInstance().player != null) {
            if (MinecraftClient.getInstance().player.getActiveItem().isOf(ModItems.PHONE)) {
                original.call(instance,x*0.4f,y*0.4f,z*0.4f);
                return;
            }
        }
        original.call(instance,x,y,z);
    }
}
