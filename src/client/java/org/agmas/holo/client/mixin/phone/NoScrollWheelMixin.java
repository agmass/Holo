package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerInventory;
import org.agmas.holo.client.render.PhoneHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerInventory.class)
public abstract class NoScrollWheelMixin {
    @WrapMethod(method = "scrollInHotbar")
    public void disableSwapping(double scrollAmount, Operation<Void> original) {
        if (PhoneHolder.usingPhone) {
            return;
        }
        original.call(scrollAmount);
    }
}
