package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.agmas.holo.client.render.PhoneHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyBinding.class)
public abstract class NoSwappingHotbarMixin {
    @WrapMethod(method = "wasPressed")
    public boolean disableSwapping(Operation<Boolean> original) {
        boolean a =original.call();
        if (a) {
            if (PhoneHolder.usingPhone) {
                for (int i = 0; i < 9; i++) {
                    if (MinecraftClient.getInstance().options.hotbarKeys[i].equals(((KeyBinding) (Object)this))) {
                        Log.info(LogCategory.GENERAL, "stopped hotbar key");
                        return false;
                    }
                }
            }
        }
        return a;
    }
}
