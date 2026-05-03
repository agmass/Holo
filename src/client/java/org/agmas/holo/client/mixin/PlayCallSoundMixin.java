package org.agmas.holo.client.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.client.sound.CallSoundLoop;
import org.agmas.holo.state.HoloPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayCallSoundMixin extends PlayerEntity {
    public PlayCallSoundMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void playCallSound(CallbackInfo ci) {
        if (HoloPlayerComponent.KEY.get(this).callSound != 0) {
            if (!HoloPlayerComponent.KEY.get(this).playingCallSound) {
                Log.info(LogCategory.GENERAL, "ring ring ring");
                CallSoundLoop callSoundLoop = new CallSoundLoop(Holo.ringtones.get(HoloPlayerComponent.KEY.get(this).callSound-1), this);
                MinecraftClient.getInstance().getSoundManager().play(callSoundLoop);
                HoloPlayerComponent.KEY.get(this).playingCallSound = true;
            }
        } else {
            HoloPlayerComponent.KEY.get(this).playingCallSound = false;
        }
    }
}
