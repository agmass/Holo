package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(HungerManager.class)
public abstract class BattleHologramOnlyHealFromSaturationMixin {

    @Shadow private float saturationLevel;

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 1))
    public float sendShellUpdate(float par1, @Local(argsOnly = true)PlayerEntity player) {
        if (StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE) || StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            if (saturationLevel <= 0)
                return 0;
        }
        return par1;
    }
    @ModifyConstant(method = "update", constant = @Constant(intValue = 0, ordinal = 2))
    public int sendShellUpdate(int constant, @Local(argsOnly = true)PlayerEntity player) {
        if (StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE) || StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            return 80-20;
        }
        return 0;
    }
}
