package org.agmas.holo.client.mixin.veilWatermediaCompat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferManager;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DynamicBufferManager.class)
public class DisableVeilDynamicBufferManagerWhenRenderingWaterMediaVideoMixin {
    @WrapMethod(method = "isEnabled", remap = false)
    public boolean disableBuffers(Operation<Boolean> original) {
        Log.info(LogCategory.GENERAL, "Kill me");
        return false;
    }
}
