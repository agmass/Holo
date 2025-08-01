package org.agmas.holo.util;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.impl.event.interaction.FakePlayerNetworkHandler;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FakestPlayer extends ServerPlayerEntity {

    public boolean isHologram = true;
    public HologramType type = HologramType.NORMAL;
    public float pitch;
    public float yaw;
    public String ownerName;
    public String holoName = "testName";
    public UUID ownerUUID;
    public RegistryKey<World> worldName;

    public static FakestPlayer get(ServerWorld world, GameProfile profile, String ownerName, UUID ownerUUID) {
        Objects.requireNonNull(world, "World may not be null.");
        Objects.requireNonNull(profile, "Game profile may not be null.");
        return new FakestPlayer(world, profile,ownerName,ownerUUID);
    }

    @Override
    public void tick() {
        Log.info(LogCategory.GENERAL, "I am " + holoName);
        tickMovement();
        super.tick();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!isHologram) {
            ServerPlayerEntity p = getServer().getPlayerManager().getPlayer(ownerUUID);
            if (p != null) {
                if (HoloNbtManager.getPlayerState(p).clones.contains(this)) {
                    Holo.swapBody(p,this,true);
                    Holo.updateAttributesAndUpdateMode(p);
                    p.kill();
                }
            }
        }
        super.onDeath(damageSource);
    }

    protected FakestPlayer(ServerWorld world, GameProfile profile, String ownerName, UUID ownerUUID) {
        super(world.getServer(), world, profile, SyncedClientOptions.createDefault());
        this.networkHandler = new FakePlayerNetworkHandler(this);
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return super.writeNbt(nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
    }

    public void actuallyWrite(NbtCompound nbtCompound) {
        super.writeCustomDataToNbt(nbtCompound);
        nbtCompound.putBoolean("isHologram", isHologram);
        nbtCompound.putInt("type", type.ordinal());
        nbtCompound.putDouble("X", getX());
        nbtCompound.putDouble("Y", getY());
        nbtCompound.putDouble("Z", getZ());
        nbtCompound.putDouble("Pitch", getPitch());
        nbtCompound.putDouble("Yaw", getYaw());
        nbtCompound.putString("HoloName", holoName);
    }

    @Override
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive) {
        if (oldPlayer instanceof FakestPlayer fp) {
            isHologram = fp.isHologram;
            type = fp.type;
            holoName = fp.holoName;
            ownerName = fp.ownerName;
            ownerUUID = fp.ownerUUID;
            worldName = fp.getWorld().getRegistryKey();
        } else {
            //should never happen but wtv
            isHologram = false;
            holoName = oldPlayer.getNameForScoreboard();
            type = HologramType.NORMAL;
        }
        super.copyFrom(oldPlayer, alive);
    }

    private static record FakePlayerKey(ServerWorld world, GameProfile profile) {
        private FakePlayerKey(ServerWorld world, GameProfile profile) {
            this.world = world;
            this.profile = profile;
        }

        public ServerWorld world() {
            return this.world;
        }

        public GameProfile profile() {
            return this.profile;
        }
    }
}
