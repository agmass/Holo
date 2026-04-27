package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayerEntity;
import org.agmas.holo.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class PhoneNoSlowdownMixin {

    @WrapOperation(
            method = {"tickMovement()V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
            )}
    )
    public boolean noPhoneSlowdown(ClientPlayerEntity instance, Operation<Boolean> original) {
        boolean f = original.call(instance);
        if (f && ((ClientPlayerEntity)(Object)this).getActiveItem().isOf(ModItems.PHONE)) {
            return false;
        }
        return f;
    }
}
