package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.agmas.holo.ModItems;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.statusEffects.ModStatusEffects;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class StopItemDropMixin {


    @WrapMethod(
            method = "dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"
    )
    public ItemEntity noPhoneSlowdown(ItemStack stack, boolean retainOwnership, Operation<ItemEntity> original) {
        if (HoloPlayerComponent.KEY.get((PlayerEntity)(Object)this).hologramType.equals(HologramType.BATTLE_DUEL)) {
            return null;
        }
        return original.call(stack,retainOwnership);
    }
    @WrapMethod(
            method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"
    )
    public ItemEntity noPhoneSlowdown(ItemStack stack, boolean throwRandomly, boolean retainOwnership, Operation<ItemEntity> original) {
        if (HoloPlayerComponent.KEY.get((PlayerEntity)(Object)this).hologramType.equals(HologramType.BATTLE_DUEL)) {
            return null;
        }
        return original.call(stack,throwRandomly,retainOwnership);
    }
}
