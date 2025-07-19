package org.agmas.holo.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class CannotModifyMixin {
    @Inject(method = "canPlayerModifyAt", at = @At("HEAD"), cancellable = true)
    public void sendShellUpdate(PlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
        if (HoloNbtManager.getPlayerState(player).inHoloMode && !HoloNbtManager.getPlayerState(player).activeModifiers.contains(HoloModifiers.CONSTRUCTION) && !HoloNbtManager.getPlayerState(player).loreAccurate) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
