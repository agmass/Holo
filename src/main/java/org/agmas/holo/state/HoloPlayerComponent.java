package org.agmas.holo.state;

import de.maxhenkel.voicechat.api.Group;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.ModItems;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HoloPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<HoloPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Holo.MOD_ID, "holo"), HoloPlayerComponent.class);
    private final PlayerEntity player;

    public boolean inHoloMode = false;
    public boolean publicCamera = false;
    public boolean loreAccurate = false;
    public boolean battleUsesNormalSaturation = false;
    public HologramType hologramType = HologramType.NORMAL;
    public PlayerInventory loreModeBattleStoredInv;
    public ItemStack loreModeBattleStoredOffhand;
    public int totalHolosCreated = 0;
    public int callSound = 0;
    public int lastComputerMaxPower = 0;
    public BlockPos computerPos = new BlockPos(0,0,0);
    public RegistryKey<World> computerWorld = null;
    public int power = 0;
    public String holoName = "";
    public Group callGroup = null;
    public boolean playingCallSound = false;
    public UUID ownerUUID = null;
    public UUID caller = null;
    public ArrayList<HoloModifiers> activeModifiers = new ArrayList<>();
    public ArrayList<UUID> playersInFight = new ArrayList<>();

    public HoloPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        inHoloMode = false;
        loreAccurate = false;
        hologramType = HologramType.NORMAL;
        totalHolosCreated = 0;
        lastComputerMaxPower = 0;
        power = 0;
        holoName = "";
        computerWorld = player.getServer().getOverworld().getRegistryKey();
        computerPos = new BlockPos(0,0,0);
        activeModifiers = new ArrayList<>();
        playersInFight = new ArrayList<>();
        caller = null;
        sync();
    }

    public void serverTick() {
        if (!hologramType.equals(HologramType.BATTLE_DUEL)) {
            battleUsesNormalSaturation = false;
            playersInFight.clear();
        }
        if (player instanceof FakestPlayer fp) {
            ownerUUID = fp.ownerUUID;
            hologramType = fp.type;
        }
        if (caller != null && player.getServer().getPlayerManager().getPlayer(caller) == null) {
            caller = null;
            callSound = 0;
            playingCallSound = false;
        }
        if (callSound != 0) {
            sync();
        }
        sync();
    }

    public void stopCall(UUID caller) {
        if (player.getServer().getPlayerManager().getPlayer(caller) != null) {
            HoloPlayerComponent.KEY.get(caller).callSound = 0;
        }
        sync();
    }
    public void startCall(UUID caller, Group group, int ringtone) {
        this.caller = caller;
        callGroup = group;
        playingCallSound = false;
        sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("inHoloMode", inHoloMode);
        tag.putBoolean("loreMode", loreAccurate);
        tag.putBoolean("publicCamera", publicCamera);
        tag.putBoolean("battleUsesNormalSaturation", battleUsesNormalSaturation);
        tag.putString("holoName", holoName);
        tag.putInt("type", hologramType.ordinal());
        tag.putInt("totalHolosCreated",totalHolosCreated);
        tag.putInt("lastComputerMaxPower", lastComputerMaxPower);
        tag.putInt("computerPosX", computerPos.getX());
        tag.putInt("computerPosY", computerPos.getY());
        tag.putInt("computerPosZ", computerPos.getZ());
        if (caller != null)
            tag.putUuid("caller", caller);
        if (player.getInventory().contains(itemStack -> itemStack.isOf(ModItems.PHONE))) {
            tag.putInt("callSound", callSound);
        } else {
            tag.putInt("callSound", 0);
        }
        if (ownerUUID != null)
            tag.putUuid("ownerUUID", ownerUUID);
        if (computerWorld == null) computerWorld = player.getWorld().getRegistryKey();
        tag.putString("computerWorld", computerWorld.getValue().toString());
        List<Integer> intList = new ArrayList<>();
        for (HoloModifiers activeModifier : activeModifiers) {
            intList.add(activeModifier.ordinal());
        }

        NbtCompound compound = new NbtCompound();
        for (UUID uuid : playersInFight) {
            compound.putUuid(uuid.toString(), uuid);
        }
        tag.putIntArray("activeModifiers",intList);
        tag.put("playersInFight",compound);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        inHoloMode = tag.getBoolean("inHoloMode");
        loreAccurate = tag.getBoolean("loreMode");
        publicCamera = tag.getBoolean("publicCamera");
        battleUsesNormalSaturation = tag.getBoolean("battleUsesNormalSaturation");
        holoName = tag.getString("holoName");
        hologramType = HologramType.values()[tag.getInt("type")];
        totalHolosCreated = tag.getInt("totalHolosCreated");
        lastComputerMaxPower = tag.getInt("lastComputerMaxPower");
        callSound = tag.getInt("callSound");
        if (tag.contains("caller")) {
            caller = tag.getUuid("caller");
        } else {
            caller = null;
        }
        if (tag.contains("ownerUUID")) {
            ownerUUID = tag.getUuid("ownerUUID");
        }
        if (player instanceof ServerPlayerEntity spe && tag.contains("computerWorld")) {
            computerWorld = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(tag.getString("computerWorld")));
        }
        computerPos = new BlockPos(tag.getInt("computerPosX"),tag.getInt("computerPosY"),tag.getInt("computerPosZ"));
        for (int modifiers : tag.getIntArray("activeModifiers")) {
            activeModifiers.add(HoloModifiers.values()[modifiers]);
        }

        playersInFight.clear();
        for (String inFight : tag.getCompound("playersInFight").getKeys()) {
            playersInFight.add(UUID.fromString(inFight));
        }

    }
}




























