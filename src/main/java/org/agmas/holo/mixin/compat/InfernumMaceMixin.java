package org.agmas.holo.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.agmas.infernum_effugium.item.InfernumMaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InfernumMaceItem.class)
public class InfernumMaceMixin {
    @WrapOperation(method = "postHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    boolean a(World instance, Entity entity, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 1) LivingEntity attacker) {
        if (HoloPlayerComponent.KEY.get(attacker).inHoloMode && HoloPlayerComponent.KEY.get(attacker).hologramType == HologramType.BATTLE_DUEL) {
            return false;
        }
        return original.call(instance,entity);
    }
}
