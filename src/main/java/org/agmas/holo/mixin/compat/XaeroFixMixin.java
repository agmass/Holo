package org.agmas.holo.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.holo.util.FakestPlayer;
import org.spongepowered.asm.mixin.Mixin;
import xaero.lib.common.event.CommonEvents;

@Mixin(CommonEvents.class)
public class XaeroFixMixin {

    @WrapMethod(method = "clonePlayer")
    void xaeroCompat(PlayerEntity oldPlayer, PlayerEntity newPlayer, Operation<Void> original) {
        if (oldPlayer instanceof FakestPlayer || newPlayer instanceof FakestPlayer) return;
        original.call(oldPlayer,newPlayer);
    }

}
