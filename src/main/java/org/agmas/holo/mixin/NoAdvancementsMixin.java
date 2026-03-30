package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.GameRules;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(PlayerAdvancementTracker.class)
public class NoAdvancementsMixin {
    @Shadow private ServerPlayerEntity owner;

    @WrapMethod(method = "grantCriterion")
    public boolean sendShellUpdate(AdvancementEntry advancement, String criterionName, Operation<Boolean> original) {
        if (owner instanceof FakestPlayer fp) {
            if (fp.isHologram) {
                return false;
            }
        }
        return original.call(advancement,criterionName);
    }
}
