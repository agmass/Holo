package org.agmas.holo.state;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class HoloNbtManager {

    public HashMap<UUID, PlayerData> players = new HashMap<>();

    public static final AttachmentType<NbtCompound> holoData = AttachmentRegistry.createPersistent(Identifier.of(Holo.MOD_ID, "holodata"), NbtCompound.CODEC);

    public static PlayerData getPlayerState(LivingEntity player) {
        HoloNbtManager serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    public static HoloNbtManager createFromNbt(NbtCompound tag) {
        HoloNbtManager state = new HoloNbtManager();

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.inHoloMode = playersNbt.getCompound(key).getBoolean("inHoloMode");
            playerData.loreAccurate = playersNbt.getCompound(key).getBoolean("loreMode");
            playerData.cloneCompound = playersNbt.getCompound(key).getCompound("clones");
            if (playersNbt.getCompound(key).contains("type", NbtElement.INT_TYPE))
                playerData.hologramType = HologramType.values()[playersNbt.getCompound(key).getInt("type")];
            if (playersNbt.getCompound(key).contains("totalHolosCreated", NbtElement.INT_TYPE))
                playerData.totalHolosCreated = playersNbt.getCompound(key).getInt("totalHolosCreated");

            if (playerData.cloneCompound != null) {
                AtomicInteger i = new AtomicInteger();
                playerData.cloneCompound.getKeys().forEach((k -> {
                    FakestPlayer fakePlayer = FakestPlayer.get(Holo.server.getOverworld(), new GameProfile(Holo.getFreeUUID(), ""), playerData.cloneCompound.getCompound(k).getString("ownerName"), UUID.fromString(key));
                    fakePlayer.readCustomDataFromNbt(playerData.cloneCompound.getCompound(k));
                    fakePlayer.setPosition(new Vec3d(playerData.cloneCompound.getCompound(k).getDouble("X"),playerData.cloneCompound.getCompound(k).getDouble("Y"),playerData.cloneCompound.getCompound(k).getDouble("Z")));
                    fakePlayer.setPitch((float) playerData.cloneCompound.getCompound(k).getDouble("Pitch"));
                    fakePlayer.setYaw((float) playerData.cloneCompound.getCompound(k).getDouble("Yaw"));
                    fakePlayer.isHologram = playerData.cloneCompound.getCompound(k).getBoolean("isHologram");
                    fakePlayer.worldName = (RegistryKey<World>) World.CODEC.parse(new Dynamic(NbtOps.INSTANCE, playerData.cloneCompound.getCompound(k).get("Dimension"))).result().get();
                    if (playerData.cloneCompound.getCompound(k).contains("type", NbtElement.INT_TYPE)) {
                        fakePlayer.type = HologramType.values()[playerData.cloneCompound.getCompound(k).getInt("type")];
                    } else {
                        fakePlayer.type = HologramType.NORMAL;
                    }
                    if (playerData.cloneCompound.getCompound(k).contains("HoloName", NbtElement.STRING_TYPE)) {
                        fakePlayer.holoName = playerData.cloneCompound.getCompound(k).getString("HoloName");
                    } else {
                        fakePlayer.holoName = "h" + i.get();
                    }
                    i.getAndIncrement();
                    playerData.clones.add(fakePlayer);

                }));
            }


            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }
    public static HoloNbtManager INSTANCE;
    public static HoloNbtManager getServerState(MinecraftServer server) {
        return INSTANCE;
    }




    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound holoNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            NbtCompound cloneNbt = new NbtCompound();

            playerNbt.putBoolean("inHoloMode", playerData.inHoloMode);
            playerNbt.putBoolean("loreMode", playerData.loreAccurate);
            playerNbt.putString("playerName", playerData.playerName);
            playerNbt.putInt("type", playerData.hologramType.ordinal());
            playerNbt.putInt("totalHolosCreated", playerData.totalHolosCreated);

            playerData.clones.forEach((fakestPlayer -> {
                NbtCompound singleCloneNbt = new NbtCompound();
                fakestPlayer.actuallyWrite(singleCloneNbt);
                cloneNbt.put(fakestPlayer.getUuid().toString(), singleCloneNbt);
            }));

            playerNbt.put("clones", cloneNbt);
            holoNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", holoNbt);

        return nbt;
    }


    public static class PlayerData {
        public boolean inHoloMode = false;
        public boolean loreAccurate = false;
        public HologramType hologramType = HologramType.NORMAL;
        public int totalHolosCreated = 0;
        public String playerName = "";
        public String holoName = "";
        public ArrayList<FakestPlayer> clones = new ArrayList<>();
        public NbtCompound cloneCompound;
    }

}