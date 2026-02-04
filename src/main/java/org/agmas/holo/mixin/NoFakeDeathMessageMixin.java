package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.GameRules;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ServerPlayerEntity.class)
public class NoFakeDeathMessageMixin {
    @WrapOperation(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z", ordinal = 0))
    public boolean sendShellUpdate(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule, Operation<Boolean> original) {
        if (((ServerPlayerEntity)(Object)this) instanceof FakestPlayer fp) {
            if (fp.isHologram) {
                fp.getWorld().playSound(fp, fp.getBlockPos(), Holo.holo_death, SoundCategory.PLAYERS, 1, new Random().nextFloat(0.9f, 1.1f));
            }
            if (fp.type != HologramType.BATTLE_DUEL) {
                return !fp.isHologram;
            }
        } else {
            ServerPlayerEntity spe = ((ServerPlayerEntity)(Object)this);
            if (HoloPlayerComponent.KEY.get(spe).inHoloMode)
                spe.getWorld().playSound(spe, spe.getBlockPos(),  Holo.holo_death, SoundCategory.PLAYERS,1,new Random().nextFloat(0.9f,1.1f));
        }
        return original.call(instance,rule);
    }
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    void aa(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (HoloPlayerComponent.KEY.get((ServerPlayerEntity)(Object)this).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    void aa(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (HoloPlayerComponent.KEY.get((ServerPlayerEntity)(Object)this).hologramType.equals(HologramType.BATTLE_DUEL)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
