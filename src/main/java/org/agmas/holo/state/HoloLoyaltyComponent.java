package org.agmas.holo.state;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class HoloLoyaltyComponent implements ServerTickingComponent {
    public static final ComponentKey<HoloLoyaltyComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Holo.MOD_ID, "hololoyalty"), HoloLoyaltyComponent.class);
    private final PersistentProjectileEntity projectile;
    STATE state = STATE.NOT_ASSIGNED;

    public HoloLoyaltyComponent(PersistentProjectileEntity projectile) {
        this.projectile = projectile;
    }

    public void serverTick() {
        if (projectile.getOwner() != null && projectile.getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
            if (state.equals(STATE.NOT_ASSIGNED)) {
                if (HoloPlayerComponent.KEY.get(serverPlayerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    state = STATE.SHOT_FROM_HOLO;
                } else {
                    state = STATE.SHOT_FROM_REAL_PERSON;
                }
            }
            if (state.equals(STATE.SHOT_FROM_HOLO)) {
                if (!HoloPlayerComponent.KEY.get(serverPlayerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    projectile.discard();
                }
            }
            if (state.equals(STATE.SHOT_FROM_REAL_PERSON)) {
                if (HoloPlayerComponent.KEY.get(serverPlayerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    projectile.discard();
                }
            }
        }
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {}

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {}

    enum STATE {
        NOT_ASSIGNED,
        SHOT_FROM_HOLO,
        SHOT_FROM_REAL_PERSON
    }
}




























