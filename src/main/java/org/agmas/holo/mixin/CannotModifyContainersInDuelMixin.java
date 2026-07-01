package org.agmas.holo.mixin;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.screen.ScreenHandler;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootableContainerBlockEntity.class)
public class CannotModifyContainersInDuelMixin {
    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    private void cannotInteract(int i, PlayerInventory playerInventory, PlayerEntity playerEntity, CallbackInfoReturnable<ScreenHandler> cir) {
        if (HoloPlayerComponent.KEY.get(playerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
