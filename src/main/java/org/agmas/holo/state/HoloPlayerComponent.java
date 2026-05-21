package org.agmas.holo.state;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.agmas.holo.Holo;
import org.agmas.holo.ModItems;
import org.agmas.holo.statusEffects.ModStatusEffects;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;
import org.agmas.holo.voicechat.HoloVoicechatPlugin;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HoloPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<HoloPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Holo.MOD_ID, "holo"), HoloPlayerComponent.class);
    private final PlayerEntity player;

    public boolean inHoloMode = false;
    public boolean publicCamera = false;
    public boolean loreAccurate = false;
    public boolean battleUsesNormalSaturation = false;
    public boolean battleCanModify = false;
    public boolean noEnchantsAllowed = false;
    public HologramType hologramType = HologramType.NORMAL;
    public PlayerInventory loreModeBattleStoredInv;
    public ItemStack loreModeBattleStoredOffhand;
    public int totalHolosCreated = 0;
    public int callSound = 0;
    public int lastComputerMaxPower = 0;
    public BlockPos computerPos = new BlockPos(0,0,0);
    public RegistryKey<World> modifiedBlocksWorld = null;
    public HashMap<BlockPos, BlockState> modifiedBlocks = new HashMap<>();
    public RegistryKey<World> computerWorld = null;
    public int power = 0;
    public int phoneExplosion = 0;
    public String holoName = "";
    public int doomDelay = 0;
    public Group callGroup = null;
    public boolean playingCallSound = false;
    public boolean playingAlarmSound = false;
    public UUID ownerUUID = null;
    public UUID caller = null;
    public UUID calling = null;
    public ArrayList<HoloModifiers> activeModifiers = new ArrayList<>();
    public ArrayList<UUID> playersInFight = new ArrayList<>();

    public HoloPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public boolean allowTaggingBlocks(World world) {
        if (modifiedBlocksWorld == null) {
            modifiedBlocksWorld = world.getRegistryKey();
            return true;
        }
        return world.getRegistryKey().equals(modifiedBlocksWorld);
    }
    public void resetBlocks(World world) {
        if (modifiedBlocksWorld == null) return;

        modifiedBlocks.forEach(world::setBlockState);
        modifiedBlocks.clear();

        modifiedBlocksWorld = null;
    }
    public void tagOriginalBlock(World world, BlockPos pos, BlockState state) {
        if (allowTaggingBlocks(world))
            modifiedBlocks.put(pos,state);
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
        calling = null;
        sync();
    }

    public void serverTick() {
        if (!hologramType.equals(HologramType.BATTLE_DUEL)) {
            battleUsesNormalSaturation = false;
            battleCanModify = false;
            playersInFight.clear();
        }
        if (phoneExplosion > 0) {
            RegistryEntry<StatusEffect> doomed = Registries.STATUS_EFFECT.getEntry(ModStatusEffects.DOOMED);
            if (!player.getInventory().contains((a)->{
                return a.isOf(ModItems.PHONE);
            }) && !player.hasStatusEffect(doomed)) {
                phoneExplosion = 0;
            } else {
                phoneExplosion--;
                if (phoneExplosion == 0) {
                    Explosion explosion = player.getWorld().createExplosion(null,player.getX(),player.getY(),player.getZ(),2, World.ExplosionSourceType.BLOCK);
                    player.timeUntilRegen = 0;
                    player.setAttacker(null);
                    player.damage(player.getEntityWorld().getDamageSources().explosion(explosion),99999);
                }
            }
            if (!player.hasStatusEffect(doomed)) {
                player.sendMessage(Text.literal("Your phone is detonating.").formatted(Formatting.RED), true);
            }
        }
        if (doomDelay > 0) {
            doomDelay--;
            if (doomDelay == 0) {
                RegistryEntry<StatusEffect> doomed = Registries.STATUS_EFFECT.getEntry(ModStatusEffects.DOOMED);
                player.addStatusEffect(new StatusEffectInstance(doomed, (20*60)*15,0, true,false));
                player.playSound(SoundEvents.AMBIENT_CAVE.value(),1f,1f);
            }
        }
        if (player instanceof FakestPlayer fp) {
            ownerUUID = fp.ownerUUID;
            hologramType = fp.type;
        }
        if (caller != null && player.getServer().getPlayerManager().getPlayer(caller) == null) {
            stopCall();
        }
        if (calling != null && player.getServer().getPlayerManager().getPlayer(calling) == null) {
            stopCall();
        }
        if (caller != null) {
            if (callGroup == null) {
                stopCall();
            }
        }
        if (caller == null && calling == null) {
            if (callSound != 0) {
                callSound = 0;
                playingCallSound = false;
                sync();
            }
        }
    }

    public void stopCall() {
        if (caller != null) {
            if (player.getServer().getPlayerManager().getPlayer(caller) != null) {
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(caller)).callSound = 0;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(caller)).playingCallSound = false;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(caller)).callGroup = null;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(caller)).callSound = 0;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(caller)).calling = null;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(caller)).caller = null;

            }
        }
        if (calling != null) {
            if (player.getServer().getPlayerManager().getPlayer(calling) != null) {
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(calling)).callSound = 0;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(calling)).caller = null;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(calling)).calling = null;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(calling)).playingCallSound = false;
                HoloPlayerComponent.KEY.get(player.getServer().getPlayerManager().getPlayer(calling)).callGroup = null;
            }
        }
        caller = null;
        calling = null;
        callSound = 0;
        callGroup = null;
        playingCallSound = false;
        sync();
    }
    public void startCall(UUID caller, Group group, int ringtone) {
        this.caller = caller;
        callGroup = group;
        playingCallSound = false;
        calling = null;
        callSound = ringtone;
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
        tag.putBoolean("battleCanModify", battleCanModify);
        tag.putBoolean("noEnchantsAllowed", noEnchantsAllowed);
        tag.putInt("computerPosX", computerPos.getX());
        tag.putInt("computerPosY", computerPos.getY());
        tag.putInt("computerPosZ", computerPos.getZ());
        tag.putInt("phoneExplosion", phoneExplosion);
        if (caller != null)
            tag.putUuid("caller", caller);
        if (calling != null)
            tag.putUuid("calling", calling);
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
        battleCanModify = tag.getBoolean("battleCanModify");
        lastComputerMaxPower = tag.getInt("lastComputerMaxPower");
        callSound = tag.getInt("callSound");
        phoneExplosion = tag.getInt("phoneExplosion");
        if (tag.contains("caller")) {
            caller = tag.getUuid("caller");
        } else {
            caller = null;
        }
        if (tag.contains("calling")) {
            calling = tag.getUuid("calling");
        } else {
            calling = null;
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




























