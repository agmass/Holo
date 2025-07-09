package org.agmas.holo.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class NoItemPickupMixin {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void sendShellUpdate(PlayerEntity player, CallbackInfo ci) {
        if (!((ItemEntity)(Object)this).getWorld().isClient) {
            if (HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                ci.cancel();
            }
        }
    }
}
