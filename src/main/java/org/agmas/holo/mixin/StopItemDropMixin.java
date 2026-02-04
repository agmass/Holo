package org.agmas.holo.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class StopItemDropMixin {
    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    void aa(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<Boolean> cir) {
        if (HoloPlayerComponent.KEY.get((PlayerEntity)(Object)this).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
