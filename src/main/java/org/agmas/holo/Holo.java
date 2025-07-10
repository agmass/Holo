package org.agmas.holo;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.terminalCommands.TerminalCommand;
import org.agmas.holo.terminalCommands.TerminalCommandParser;
import org.agmas.holo.util.BattleHologramComputerEntry;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModeUpdates;
import org.agmas.holo.util.HologramType;

import java.net.URL;
import java.util.*;

public class Holo implements ModInitializer {



    public static final String MOD_ID = "holo";
    public static MinecraftServer server;
    public static HashMap<BattleHologramComputerEntry, ArrayList<PlayerEntity>> playersWaitingForBattle = new HashMap<>();
    public static ArrayList<ArrayList<PlayerEntity>> fights = new ArrayList<>();

    public static final Identifier HUMAN_MODE = new Identifier(MOD_ID, "human_mode");
    public static final Identifier HOLO_MODE = new Identifier(MOD_ID, "holo_mode");
    public static final Identifier SWAP_PACKET = new Identifier(MOD_ID, "swap");

    public static final Identifier TERMINAL_COMMAND = new Identifier(MOD_ID, "terminal_command");

    public static final Identifier SEND_TERMINAL_AUTOCOMPLETE = new Identifier(MOD_ID, "terminal_autocomplete");
    public static final Identifier REQUEST_TERMINAL_AUTOCOMPLETE = new Identifier(MOD_ID, "request_terminal_autocomplete");

    public static final Identifier TEMPORARILY_SHOW_ENTITY = new Identifier(MOD_ID, "temporarily_show_entity");
    public static final Identifier OPEN_BATTLE_COMPUTER_SCREEN = new Identifier(MOD_ID, "open_battle_computer_screen");

    public static UUID HUMAN_DAMAGE_MODIFIER_ID = UUID.fromString("88d46329-20a2-42a8-8d7b-0b11fdcda31f");
    public static UUID HUMAN_DAMAGE_SPEED_MODIFIER_ID = UUID.fromString("5da6b87c-bd81-4068-b9a2-386adaf38762");
    public static UUID HUMAN_SPEED_MODIFIER_ID = UUID.fromString("bc5dd5c1-4594-4f2e-9d60-2e30f69d02e6");
    public static UUID HUMAN_HEALTH_ID = UUID.fromString("87a934eb-7c72-400e-9ae7-09220be58577");

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModBlocks.initialize();
        ModItems.initialize();
        TerminalCommandParser.initCommands();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("holo_loreMode").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
                if (context.getSource().getPlayer() != null) {
                    switchShellMode(context.getSource().getPlayer(), true, false);
                    StateSaverAndLoader.getPlayerState(context.getSource().getPlayer()).loreAccurate = !StateSaverAndLoader.getPlayerState(context.getSource().getPlayer()).loreAccurate;
                    getHumanAttributes(context.getSource().getPlayer()).forEach((attributeEntry, modifier) -> {
                        if (context.getSource().getPlayer().getAttributeInstance(attributeEntry) != null) {
                            if (!context.getSource().getPlayer().getAttributeInstance(attributeEntry).hasModifier(modifier)) {
                                context.getSource().getPlayer().getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                            }
                        }
                    });

                }
                return 1;
            }));
        });



        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((t)->{
            t.add(ModItems.HOLOGRAM_SPAWN_EGG);
            t.add(ModItems.BATTLE_HOLOGRAM_SPAWN_EGG);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((t)->{
            t.add(ModBlocks.HOLOGRAM_CONTROLLER);
            t.add(ModBlocks.BATTLE_HOLOGRAM_CONTROLLER);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((srv)->{
            for (PlayerEntity player : srv.getPlayerManager().getPlayerList()) {

                if (StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    swapBody(player,false,false);
                    updateAttributesAndUpdateMode(player);
                }
                for (FakestPlayer clone : StateSaverAndLoader.getPlayerState(player).clones) {
                    player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(clone.getUuid())));
                    clone.getServer().getPlayerManager().remove(clone);
                }
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register((srv)->{
            server = srv;
            fights.clear();
            playersWaitingForBattle.clear();
        });
        ServerPlayConnectionEvents.DISCONNECT.register(((serverPlayNetworkHandler, minecraftServer) -> {
            for (FakestPlayer clone : StateSaverAndLoader.getPlayerState(serverPlayNetworkHandler.player).clones) {
                clone.getServer().getPlayerManager().remove(clone);
                clone.remove(Entity.RemovalReason.DISCARDED);
            }
        }));

        ArrayList<ServerPlayerEntity> bufferedKeys = new ArrayList<>();
        ServerPlayNetworking.registerGlobalReceiver(Holo.REQUEST_TERMINAL_AUTOCOMPLETE, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            PacketByteBuf data = PacketByteBufs.create();
            for (TerminalCommand value : TerminalCommandParser.nameAndCommands.values()) {
                value.autoCompletion(serverPlayerEntity).forEach(data::writeString);
            }
            ServerPlayNetworking.send(serverPlayerEntity, SEND_TERMINAL_AUTOCOMPLETE, data);
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.TERMINAL_COMMAND, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            String command = packetByteBuf.readString();
            if (StateSaverAndLoader.getPlayerState(serverPlayerEntity).inHoloMode) {
                serverPlayerEntity.sendMessage(TerminalCommandParser.findAndRunCommand(command, serverPlayerEntity));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.SWAP_PACKET, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            if (StateSaverAndLoader.getPlayerState(serverPlayerEntity).inHoloMode) {
                if (!bufferedKeys.contains(serverPlayerEntity)) {
                    bufferedKeys.add(serverPlayerEntity);
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register(((old, newPlayer, b) -> {
            if (StateSaverAndLoader.getPlayerState(newPlayer).inHoloMode) {
                switchShellMode(newPlayer, true, false);
            } else if (StateSaverAndLoader.getPlayerState(newPlayer).loreAccurate) {
                newPlayer.getServer().getPlayerManager().getUserBanList().add(new BannedPlayerEntry(newPlayer.getGameProfile()));
                newPlayer.networkHandler.sendPacket(new DisconnectS2CPacket(Text.translatable("multiplayer.disconnect.banned")));
            }
        }));
        ServerPlayerEvents.ALLOW_DEATH.register(((serverPlayerEntity, damageSource, v) -> {
            if (StateSaverAndLoader.getPlayerState(serverPlayerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                resetFromFight(serverPlayerEntity);
                return false;
            }
            return true;
        }));
        ServerTickEvents.START_SERVER_TICK.register((server -> {

            fights.removeIf((fight)->{
                fight.removeIf((p)->{
                    if (!p.isPartOfGame() || !StateSaverAndLoader.getPlayerState(p).inHoloMode|| !StateSaverAndLoader.getPlayerState(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                        resetFromFight(p);
                        return true;
                    }
                    return false;
                });
                if (fight.size() == 1) {
                    resetFromFight(fight.get(0));
                    return true;
                }
                return fight.isEmpty();
            });
            for (Map.Entry<BattleHologramComputerEntry, ArrayList<PlayerEntity>> entry : playersWaitingForBattle.entrySet()) {
                entry.getValue().removeIf((p)->{
                    return p.getWorld() != entry.getKey().world || !p.isPartOfGame();
                });
                ArrayList<PlayerEntity> fight = new ArrayList<>();
                if (entry.getValue().size() >= entry.getKey().count || (entry.getValue().size() >= 2 && entry.getKey().start)) {
                    entry.getKey().start = false;
                    for (PlayerEntity player : entry.getValue()) {
                        FakestPlayer player1 = summonNewBody(player, StateSaverAndLoader.getPlayerState(player).inHoloMode, HologramType.NORMAL, "duel_holo");
                        FakestPlayer player2 = summonNewBody(player, true, HologramType.BATTLE_DUEL, "duel_holo");

                        StateSaverAndLoader.getPlayerState(player).inHoloMode = true;
                        StateSaverAndLoader.getPlayerState(player).hologramType = HologramType.BATTLE_DUEL;
                        tinyPlayerClone((ServerPlayerEntity) player, player1);
                        tinyPlayerClone(player2, (ServerPlayerEntity) player);
                        StateSaverAndLoader.getPlayerState(player).clones.remove(player2);
                        player.requestTeleport(entry.getKey().pos.getX(),entry.getKey().pos.getY(),entry.getKey().pos.getZ());
                        player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(player1.getUuid())));
                        player2.remove(Entity.RemovalReason.DISCARDED);
                        HoloModeUpdates.sendHoloModeUpdate(player);
                        fight.add(player);
                        for (int i = 0; i <  player.getInventory().size(); i++) {
                            player.getInventory().setStack(i, player1.getInventory().getStack(i).copy());
                        }
                        player.getInventory().markDirty();

                        WorldBorder worldBorder = new WorldBorder();
                        worldBorder.setCenter(entry.getKey().pos.getX()*entry.getKey().world.getDimension().coordinateScale(), entry.getKey().pos.getZ()*entry.getKey().world.getDimension().coordinateScale());
                        worldBorder.setSize(24);
                        if (player instanceof ServerPlayerEntity spe) {
                            if (!entry.getKey().infinite) {
                                spe.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(worldBorder));
                                spe.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
                            }
                            spe.timeUntilRegen = 20*5;
                        }
                    }
                    fights.add(fight);
                    entry.getValue().clear();
                }
                for (PlayerEntity player : entry.getValue()) {
                    player.sendMessage(Text.literal("Waiting for opponent.. (" + entry.getValue().size() + "/" + entry.getKey().count + ")").formatted(Formatting.GOLD),true);
                }
            }
        }));
        ServerTickEvents.END_SERVER_TICK.register((s)->{
            s.getPlayerManager().getPlayerList().forEach((p)-> {
                ArrayList<FakestPlayer> clonestoYoink = new ArrayList<>();
                StateSaverAndLoader.getPlayerState(p).clones.removeIf((fp) -> {
                    if (!fp.isAlive()) {
                        clonestoYoink.add(fp);
                    }
                    return !fp.isAlive();
                });
                for (FakestPlayer fp : clonestoYoink) {
                    fp.getServer().getPlayerManager().remove(fp);
                }
            });
        });
        ServerTickEvents.START_WORLD_TICK.register((serverWorld -> {
            serverWorld.getPlayers().forEach((p)->{
                if (StateSaverAndLoader.getPlayerState(p).inHoloMode) {
                    if (StateSaverAndLoader.getPlayerState(p).hologramType.equals(HologramType.BATTLE) || StateSaverAndLoader.getPlayerState(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                        p.getHungerManager().setFoodLevel(19);
                    } else {
                        p.getHungerManager().setFoodLevel(20);
                    }
                }
            });
            for (ServerPlayerEntity bufferPlayer : bufferedKeys) {
                if (bufferedKeys.contains(bufferPlayer)) {
                    FakestPlayer fakestPlayer = StateSaverAndLoader.getPlayerState(bufferPlayer).clones.get(0);
                    swapBody(bufferPlayer, fakestPlayer, !StateSaverAndLoader.getPlayerState(bufferPlayer).hologramType.equals(HologramType.BATTLE_DUEL));
                    updateAttributesAndUpdateMode(bufferPlayer);
                }
            }
            bufferedKeys.clear();
        }));
    }


    public static void resetFromFight(PlayerEntity player) {
        if (player.isPartOfGame() && StateSaverAndLoader.getPlayerState(player).inHoloMode && StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            swapBody(player, false, false);
        }
        updateAttributesAndUpdateMode(player);
        if (player instanceof ServerPlayerEntity spe) {
            spe.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(player.getWorld().getWorldBorder()));
            spe.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(player.getWorld().getWorldBorder()));
        }
    }

    public static UUID getFreeUUID() {
        UUID uuid = UUID.randomUUID();
        if (server.getUserCache() != null) {
            while (server.getUserCache().getByUuid(uuid).isPresent()) {
                uuid = UUID.randomUUID();
            }
        }
        return uuid;
    }



    public static void tinyPlayerClone(PlayerEntity original, ServerPlayerEntity clone) {
        clone.getInventory().clone(original.getInventory());
        clone.setHealth(original.getHealth());
        clone.getHungerManager().setSaturationLevel(original.getHungerManager().getSaturationLevel());
        clone.getHungerManager().setExhaustion(original.getHungerManager().getExhaustion());
        clone.getHungerManager().setFoodLevel(original.getHungerManager().getFoodLevel());
        clone.experienceLevel = original.experienceLevel;
        clone.totalExperience = original.totalExperience;
        clone.setHealth(original.getHealth());
        clone.experienceProgress = original.experienceProgress;
        clone.setScore(original.getScore());
        if (original instanceof ServerPlayerEntity spe) {
            clone.changeGameMode(spe.interactionManager.getGameMode());
        }
        clone.setFireTicks(original.getFireTicks());
        clone.timeUntilRegen = 0;
        clone.fallDistance = original.fallDistance;
        clone.updateLastActionTime();
        clone.getInventory().markDirty();

        clone.timeUntilRegen = original.timeUntilRegen;
        clone.playerTick();
        clone.teleport((ServerWorld) original.getWorld(), original.getPos().x, original.getPos().y, original.getPos().z, original.getYaw(), original.getPitch());
    }

    public static ImmutableMultimap<EntityAttribute,EntityAttributeModifier> getHoloAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (!StateSaverAndLoader.getPlayerState(player).loreAccurate) {
            if (!StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE)) {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID, "Holo modifier", (double) -100, EntityAttributeModifier.Operation.ADDITION));
            } else {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID, "Holo modifier", (double) -4.5, EntityAttributeModifier.Operation.ADDITION));
            }
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID, "Holo modifier", (double) -0.9, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID, "Holo modifier", (double) -0.075, EntityAttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }
    public static ImmutableMultimap<EntityAttribute,EntityAttributeModifier> getHumanAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (StateSaverAndLoader.getPlayerState(player).loreAccurate) {
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID, "Human modifier", (double) -3.5, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID, "Human modifier", (double) -0.8, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID, "Human modifier", (double) -0.045, EntityAttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }




    public static boolean canSwapBody(PlayerEntity player, boolean holo) {

        FakestPlayer bodyToTake = null;
        for (FakestPlayer clone : StateSaverAndLoader.getPlayerState(player).clones) {
            if (clone.isHologram == holo)  {
                return true;
            }
        }
        return false;
    }

    public static void swapBody(PlayerEntity player, FakestPlayer bodyToTake, boolean bodyStays) {
        player.dismountVehicle();

        if (bodyStays)
            summonBody(player);

        if (bodyToTake != null) {
            StateSaverAndLoader.getPlayerState(player).clones.remove(bodyToTake);
            StateSaverAndLoader.getPlayerState(player).inHoloMode = bodyToTake.isHologram;
            StateSaverAndLoader.getPlayerState(player).holoName = bodyToTake.holoName;
            StateSaverAndLoader.getPlayerState(player).hologramType = bodyToTake.type;

            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);

            player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(bodyToTake.getUuid())));
            bodyToTake.remove(Entity.RemovalReason.DISCARDED);
        }
    }
    public static void swapBody(PlayerEntity player, boolean holo, boolean bodyStays) {
        player.dismountVehicle();
        if (bodyStays)
            summonBody(player);

        FakestPlayer bodyToTake = null;
        for (FakestPlayer clone : StateSaverAndLoader.getPlayerState(player).clones) {
            if (clone.isHologram == holo)  { bodyToTake = clone; break; }
        }
        if (bodyToTake != null) {
            StateSaverAndLoader.getPlayerState(player).inHoloMode = holo;
            StateSaverAndLoader.getPlayerState(player).holoName = bodyToTake.holoName;
            if (holo) {
                StateSaverAndLoader.getPlayerState(player).hologramType = bodyToTake.type;
            }
            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);
            StateSaverAndLoader.getPlayerState(player).clones.remove(bodyToTake);
            player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(bodyToTake.getUuid())));
            bodyToTake.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private static void summonBody(PlayerEntity player) {
        summonBody(player, StateSaverAndLoader.getPlayerState(player).inHoloMode);
    }

    public static FakestPlayer summonNewBody(PlayerEntity player, boolean holoMode, HologramType type, String holoName) {
        GameProfile profile = new GameProfile(Holo.getFreeUUID(), "");
        profile.getProperties().putAll(player.getGameProfile().getProperties());
        FakestPlayer fakePlayer = FakestPlayer.get((ServerWorld) player.getWorld(), profile, player.getEntityName(), player.getUuid());
        player.getWorld().getServer().getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(fakePlayer)));

        fakePlayer.setServerWorld((ServerWorld) player.getWorld());
        fakePlayer.refreshPositionAndAngles(player.getX(),player.getY(),player.getZ(),player.getYaw(),player.getPitch());

        ((ServerWorld) player.getWorld()).onPlayerConnected(fakePlayer);
        fakePlayer.isHologram = holoMode;
        fakePlayer.type = type;
        fakePlayer.holoName = holoName;
        if (!fakePlayer.isHologram) {
            fakePlayer.holoName = player.getName().getString();
        }
        if (fakePlayer.isHologram) {
            HoloModeUpdates.sendHoloModeUpdate(fakePlayer);
        }
        fakePlayer.worldName = player.getWorld().getRegistryKey();
        StateSaverAndLoader.getPlayerState(player).clones.add(fakePlayer);
        StateSaverAndLoader.getPlayerState(player).playerName = player.getEntityName();
        player.getWorld().getServer().forcePlayerSampleUpdate();
        return fakePlayer;
    }

    public static void summonBody(PlayerEntity player, boolean holoMode) {
        FakestPlayer fakePlayer = summonNewBody(player,holoMode,StateSaverAndLoader.getPlayerState(player).hologramType, StateSaverAndLoader.getPlayerState(player).holoName);
        Holo.tinyPlayerClone(player, fakePlayer);
    }

    public static void switchShellMode(PlayerEntity player, boolean shell, boolean bodyStays) {
        swapBody(player, !shell,bodyStays);
        updateAttributesAndUpdateMode(player);
    }


    public static void updateAttributesAndUpdateMode(PlayerEntity player) {
        if (StateSaverAndLoader.getPlayerState(player).inHoloMode) {
            if (!StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                getHoloAttributes(player).forEach((attributeEntry, modifier) -> {
                    if (player.getAttributeInstance(attributeEntry) != null) {
                        if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier)) {
                            player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                        }
                    }
                });
            }
            player.getAttributes().removeModifiers(getHumanAttributes(player));
            HoloModeUpdates.sendHoloModeUpdate(player);
        } else {
            StateSaverAndLoader.getPlayerState(player).hologramType = HologramType.NORMAL;
            getHumanAttributes(player).forEach((attributeEntry, modifier) -> {
                    if (player.getAttributeInstance(attributeEntry) != null) {
                        if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier)) {
                            player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                        }
                    }
            });

            player.getAttributes().removeModifiers(getHoloAttributes(player));
            HoloModeUpdates.sendHumanModeUpdate(player);
        }
    }
}
