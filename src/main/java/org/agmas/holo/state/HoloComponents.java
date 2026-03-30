package org.agmas.holo.state;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class HoloComponents implements EntityComponentInitializer {
    public HoloComponents() {
    }

    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, ClonePlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(ClonePlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, HoloPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(HoloPlayerComponent::new);
        registry.beginRegistration(PersistentProjectileEntity.class, HoloLoyaltyComponent.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(HoloLoyaltyComponent::new);
    }

}