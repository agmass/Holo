package org.agmas.holo.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class NoFakeDeathMessageMixin {
    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean sendShellUpdate(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        if (((ServerPlayerEntity)(Object)this) instanceof FakestPlayer fp) {
            return !fp.isHologram;
        }
        return true;
    }
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    void aa(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (HoloNbtManager.getPlayerState((ServerPlayerEntity)(Object)this).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
