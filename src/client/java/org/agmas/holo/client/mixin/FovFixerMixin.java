package org.agmas.holo.client.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class FovFixerMixin {

    @Redirect(method = "getFovMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerAbilities;getWalkSpeed()F"))
    public float visibilityMixin(PlayerAbilities instance) {
        if (HoloClient.hologramType != null) {
            if (HoloClient.hologramType != HologramType.BATTLE_DUEL)
                return (float) ((LivingEntity)(Object)this).getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        }
        return instance.getWalkSpeed();
    }
}
