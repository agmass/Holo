package org.agmas.holo.state;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ClonePlayerComponent implements ServerTickingComponent {
    public static final ComponentKey<ClonePlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Holo.MOD_ID, "clone"), ClonePlayerComponent.class);
    private final PlayerEntity player;
    public ArrayList<FakestPlayer> clones = new ArrayList<>();

    public ClonePlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void clientTick() {
    }

    public void serverTick() {
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound cloneCompound = new NbtCompound();
        clones.forEach((fakestPlayer -> {
            NbtCompound singleCloneNbt = new NbtCompound();
            fakestPlayer.actuallyWrite(singleCloneNbt);
            cloneCompound.put(fakestPlayer.getUuid().toString(), singleCloneNbt);
        }));
        tag.put("clones", cloneCompound);

    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        AtomicInteger i = new AtomicInteger();
        NbtCompound cloneCompound = tag.getCompound("clones");
        cloneCompound.getKeys().forEach((k -> {
            try {
                String ids = cloneCompound.getCompound(k).getString("Dimension");
                Identifier id = Identifier.of(ids);
                GameProfile profile = new GameProfile(Holo.getFreeUUID(), "");
                profile.getProperties().putAll(player.getGameProfile().getProperties());
                FakestPlayer fakePlayer = FakestPlayer.get(Holo.server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id)), profile, cloneCompound.getCompound(k).getString("ownerName"), player.getUuid());

                fakePlayer.ownerUUID = player.getUuid();
                fakePlayer.ownerName = player.getNameForScoreboard();
                fakePlayer.worldName = RegistryKey.of(RegistryKeys.WORLD, id);
                fakePlayer.readCustomDataFromNbt(cloneCompound.getCompound(k));
                fakePlayer.isHologram = cloneCompound.getCompound(k).getBoolean("isHologram");
                if (cloneCompound.getCompound(k).contains("type", NbtElement.INT_TYPE)) {
                    fakePlayer.type = HologramType.values()[cloneCompound.getCompound(k).getInt("type")];
                } else {
                    fakePlayer.type = HologramType.NORMAL;
                }
                if (cloneCompound.getCompound(k).contains("HoloName", NbtElement.STRING_TYPE)) {
                    fakePlayer.holoName = cloneCompound.getCompound(k).getString("HoloName");
                } else {
                    fakePlayer.holoName = "h" + i.get();
                }
                i.getAndIncrement();
                clones.add(fakePlayer);
            } catch (Exception e) {

                Log.error(LogCategory.GENERAL, "Failed to spawn holo: " + cloneCompound.getCompound(k).getString("HoloName") + " by " + player.getNameForScoreboard());
                Log.error(LogCategory.GENERAL, e.getMessage());
            }



        }));
    }
}




























