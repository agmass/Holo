package org.agmas.holo.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.holo.state.StyleMeterComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class StyleMeterShieldMixin {

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"))
    public void criticalDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity)(Object)this) instanceof PlayerEntity player)
            StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.BLOCK_DAMAGE);
    }
}
