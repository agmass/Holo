package org.agmas.holo.state;

import net.minecraft.entity.player.PlayerEntity;
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

public class HoloPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<HoloPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Holo.MOD_ID, "holo"), HoloPlayerComponent.class);
    private final PlayerEntity player;

    public boolean inHoloMode = false;
    public boolean loreAccurate = false;
    public HologramType hologramType = HologramType.NORMAL;
    public int totalHolosCreated = 0;
    public int lastComputerMaxPower = 0;
    public BlockPos computerPos = new BlockPos(0,0,0);
    public World computerWorld = null;
    public int power = 0;
    public String holoName = "";
    public ArrayList<HoloModifiers> activeModifiers = new ArrayList<>();

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
        computerWorld = player.getServer().getOverworld();
        computerPos = new BlockPos(0,0,0);
        activeModifiers = new ArrayList<>();
        sync();
    }

    public void serverTick() {
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("inHoloMode", inHoloMode);
        tag.putBoolean("loreMode", loreAccurate);
        tag.putString("holoName", holoName);
        tag.putInt("type", hologramType.ordinal());
        tag.putInt("totalHolosCreated",totalHolosCreated);
        tag.putInt("lastComputerMaxPower", lastComputerMaxPower);
        tag.putInt("computerPosX", computerPos.getX());
        tag.putInt("computerPosY", computerPos.getY());
        tag.putInt("computerPosZ", computerPos.getZ());
        if (computerWorld == null) computerWorld = player.getWorld();
        tag.putString("computerWorld", computerWorld.getRegistryKey().getValue().toString());
        List<Integer> intList = new ArrayList<>();
        for (HoloModifiers activeModifier : activeModifiers) {
            intList.add(activeModifier.ordinal());
        }
        tag.putIntArray("activeModifiers",intList);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        inHoloMode = tag.getBoolean("inHoloMode");
        loreAccurate = tag.getBoolean("loreMode");
        holoName = tag.getString("holoName");
        hologramType = HologramType.values()[tag.getInt("type")];
        totalHolosCreated = tag.getInt("totalHolosCreated");
        lastComputerMaxPower = tag.getInt("lastComputerMaxPower");
        if (player instanceof ServerPlayerEntity spe && tag.contains("computerWorld")) {
            computerWorld = spe.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(tag.getString("computerWorld"))));
        }
        computerPos = new BlockPos(tag.getInt("computerPosX"),tag.getInt("computerPosY"),tag.getInt("computerPosZ"));
        for (int modifiers : tag.getIntArray("activeModifiers")) {
            activeModifiers.add(HoloModifiers.values()[modifiers]);
        }

    }
}




























