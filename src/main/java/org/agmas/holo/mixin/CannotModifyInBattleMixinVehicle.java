package org.agmas.holo.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleInventory.class)
public interface CannotModifyInBattleMixinVehicle {
    @Inject(method = "canPlayerAccess", at = @At("HEAD"), cancellable = true)
    private void cannotInteract(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
