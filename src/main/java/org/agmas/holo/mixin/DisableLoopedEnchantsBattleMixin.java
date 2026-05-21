package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.agmas.holo.state.HoloPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentHelper.class)
public class DisableLoopedEnchantsBattleMixin {
    @WrapMethod(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V")
    private static void stopItemUse(ItemStack stack, EquipmentSlot slot, LivingEntity entity, EnchantmentHelper.ContextAwareConsumer contextAwareConsumer, Operation<Void> original) {
        if (entity instanceof PlayerEntity player) {
            if (HoloPlayerComponent.KEY.get(player).noEnchantsAllowed) {
                return;
            }
        }
        original.call(stack,slot,entity,contextAwareConsumer);
    }
    @WrapMethod(method = "forEachEnchantment(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V")
    private static void stopItemUse(LivingEntity entity, EnchantmentHelper.ContextAwareConsumer contextAwareConsumer, Operation<Void> original) {
        if (entity instanceof PlayerEntity player) {
            if (HoloPlayerComponent.KEY.get(player).noEnchantsAllowed) {
                return;
            }
        }
        original.call(entity,contextAwareConsumer);
    }
}
