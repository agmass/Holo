package org.agmas.holo.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class LoreAccurateHolosCannotPickupitemsMixin {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void sendShellUpdate(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof FakestPlayer fp) {
            if (fp.getWorld().isClient()) return;
            ServerPlayerEntity p = fp.getServer().getPlayerManager().getPlayer(fp.ownerUUID);
            if (p != null) {
                if (HoloPlayerComponent.KEY.get(p).loreAccurate) {
                    ci.cancel();
                }
            }
        }
    }
}
