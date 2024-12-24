package org.agmas.holo.mixin.compat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.infernum_effugium.Infernum_effugium;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Infernum_effugium.class)
public class InfernumEffugiumMixin {
    @Inject(method = "lambda$onInitialize$0", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), cancellable = true)
    private static void a(ServerPlayerEntity p, CallbackInfo ci) {
        if (StateSaverAndLoader.getPlayerState(p).inHoloMode) {
            ci.cancel();
        }
    }
}
