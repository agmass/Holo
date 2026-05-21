package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import org.agmas.holo.state.HoloPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentHelper.class)
public class DisableEnchantsBattleMixin {
    @WrapMethod(method = "getEquipmentLevel")
    private static int stopItemUse(RegistryEntry<Enchantment> enchantment, LivingEntity entity, Operation<Integer> original) {
        if (entity instanceof PlayerEntity player) {
            if (HoloPlayerComponent.KEY.get(player).noEnchantsAllowed) {
                return 0;
            }
        }
        return original.call(enchantment,entity);
    }
}
