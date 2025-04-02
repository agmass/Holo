package org.agmas.holo.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class NoItemPickupMixin {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void sendShellUpdate(PlayerEntity player, CallbackInfo ci) {
        if (!((ItemEntity)(Object)this).getWorld().isClient) {
            if (StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                ci.cancel();
            }
        }
    }
}
