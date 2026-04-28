package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec2f;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.render.PhoneHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class SwingHandWhenMousedPresssedMixin {


    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void swingHand(long window, int button, int action, int mods, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            if (MinecraftClient.getInstance().player.getActiveItem().isOf(ModItems.PHONE)) {
                if (!PhoneHolder.phoneState.equals(PhoneHolder.PhoneState.CAMERA)) {
                    MinecraftClient.getInstance().player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }
}
