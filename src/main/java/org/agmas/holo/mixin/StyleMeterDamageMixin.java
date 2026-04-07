package org.agmas.holo.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.holo.Holo;
import org.agmas.holo.state.ClonePlayerComponent;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.state.StyleMeterComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModeUpdates;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class StyleMeterDamageMixin {

    @Inject(method = "takeShieldHit", at = @At("HEAD"))
    public void criticalDamage(LivingEntity attacker, CallbackInfo ci) {
        if (attacker.disablesShield()) {
            if (attacker instanceof PlayerEntity player) {
                StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.SHIELD_BREAK);
            }
        }
    }
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addCritParticles(Lnet/minecraft/entity/Entity;)V"))
    public void criticalDamage(Entity target, CallbackInfo ci) {
        StyleMeterComponent.KEY.get(((PlayerEntity)(Object)this)).addStylePoints(StyleMeterComponent.StyleReason.CRITICAL);
    }
}
