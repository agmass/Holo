package org.agmas.holo.client.sound;


import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.agmas.holo.state.HoloPlayerComponent;

public class AlarmSoundLoop extends MovingSoundInstance {

    public PlayerEntity calledPlayer;
    public AlarmSoundLoop(SoundEvent soundEvent, PlayerEntity calledPlayer) {
        super(soundEvent, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 1.0F;
        this.relative = false;
        this.calledPlayer = calledPlayer;

    }

    @Override
    public boolean canPlay() {
        return HoloPlayerComponent.KEY.get(calledPlayer).phoneExplosion > 0;
    }

    @Override
    public void tick() {
        if (HoloPlayerComponent.KEY.get(calledPlayer).phoneExplosion <= 0) {
            setDone();
        }

        x = calledPlayer.getX();
        y = calledPlayer.getY();
        z = calledPlayer.getZ();


    }
}