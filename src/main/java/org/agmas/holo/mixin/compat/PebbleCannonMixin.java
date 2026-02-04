package org.agmas.holo.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.agmas.infernum_effugium.item.PebbleCannonItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PebbleCannonItem.class)
public class PebbleCannonMixin {
    @WrapOperation(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/item/ItemStack;)I", ordinal = 3))
    int a(RegistryEntry<Enchantment> enchantment, ItemStack stack, Operation<Integer> original, @Local(argsOnly = true)Entity entity) {
        if (entity instanceof PlayerEntity player) {
            if (HoloPlayerComponent.KEY.get(player).inHoloMode && HoloPlayerComponent.KEY.get(player).hologramType == HologramType.BATTLE_DUEL) {
                return 0;
            }
        }
        return original.call(enchantment,stack);
    }
}
