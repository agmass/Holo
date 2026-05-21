package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.agmas.holo.state.HoloPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentHelper.class)
public class ClientDisableEnchantsBattleMixin {
    @WrapMethod(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V")
    private static void stopItemUse(ItemStack stack, EnchantmentHelper.Consumer consumer, Operation<Void> original) {
        if ((MinecraftClient.getInstance().player != null && HoloPlayerComponent.KEY.get(MinecraftClient.getInstance().player).noEnchantsAllowed)) {
            return;
        }
        original.call(stack,consumer);
    }
}
