package org.agmas.holo.state;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID, PlayerData> players = new HashMap<>();


    public static PlayerData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.inHoloMode = playersNbt.getCompound(key).getBoolean("inHoloMode");
            playerData.loreAccurate = playersNbt.getCompound(key).getBoolean("loreMode");
            playerData.cloneCompound = playersNbt.getCompound(key).getCompound("clones");
            playerData.hologramType = HologramType.valueOf(playersNbt.getCompound(key).getString("type"));

            if (playerData.cloneCompound != null) {
                playerData.cloneCompound.getKeys().forEach((k -> {
                    FakestPlayer fakePlayer = FakestPlayer.get(Holo.server.getOverworld(), new GameProfile(Holo.getFreeUUID(), ""), playerData.cloneCompound.getCompound(k).getString("ownerName"), UUID.fromString(key));
                    fakePlayer.readCustomDataFromNbt(playerData.cloneCompound.getCompound(k));
                    fakePlayer.setPosition(new Vec3d(playerData.cloneCompound.getCompound(k).getDouble("X"),playerData.cloneCompound.getCompound(k).getDouble("Y"),playerData.cloneCompound.getCompound(k).getDouble("Z")));
                    fakePlayer.setPitch((float) playerData.cloneCompound.getCompound(k).getDouble("Pitch"));
                    fakePlayer.setYaw((float) playerData.cloneCompound.getCompound(k).getDouble("Yaw"));
                    fakePlayer.isHologram = playerData.cloneCompound.getCompound(k).getBoolean("isHologram");
                    fakePlayer.worldName = (RegistryKey<World>) World.CODEC.parse(new Dynamic(NbtOps.INSTANCE, playerData.cloneCompound.getCompound(k).get("Dimension"))).result().get();
                    fakePlayer.type = HologramType.valueOf(playersNbt.getCompound(key).getString("type"));
                    playerData.clones.add(fakePlayer);

                }));
            }


            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }
    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverAndLoader state = persistentStateManager.getOrCreate(StateSaverAndLoader::createFromNbt, StateSaverAndLoader::new, Holo.MOD_ID);
        state.markDirty();

        return state;
    }



    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            NbtCompound cloneNbt = new NbtCompound();

            playerNbt.putBoolean("inHoloMode", playerData.inHoloMode);
            playerNbt.putBoolean("loreMode", playerData.loreAccurate);
            playerNbt.putString("playerName", playerData.playerName);
            playerNbt.putString("type", playerData.hologramType.name());

            playerData.clones.forEach((fakestPlayer -> {
                NbtCompound singleCloneNbt = new NbtCompound();
                fakestPlayer.actuallyWrite(singleCloneNbt);
                cloneNbt.put(fakestPlayer.getUuid().toString(), singleCloneNbt);
            }));

            playerNbt.put("clones", cloneNbt);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }


    public static class PlayerData {
        public boolean inHoloMode = false;
        public boolean loreAccurate = false;
        public HologramType hologramType = HologramType.NORMAL;
        public String playerName = "";
        public ArrayList<FakestPlayer> clones = new ArrayList<>();
        public NbtCompound cloneCompound;
    }

}