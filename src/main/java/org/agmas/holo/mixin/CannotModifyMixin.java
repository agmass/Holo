package org.agmas.holo.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.agmas.holo.state.HoloPlayerComponent;
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
        if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
        if (HoloPlayerComponent.KEY.get(player).inHoloMode && !HoloPlayerComponent.KEY.get(player).activeModifiers.contains(HoloModifiers.CONSTRUCTION) && !HoloPlayerComponent.KEY.get(player).loreAccurate) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
