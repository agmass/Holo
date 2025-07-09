package org.agmas.holo;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.border.WorldBorder;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.terminalCommands.TerminalCommand;
import org.agmas.holo.terminalCommands.TerminalCommandParser;
import org.agmas.holo.util.BattleHologramComputerEntry;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModeUpdates;
import org.agmas.holo.util.HologramType;
import org.agmas.holo.util.payloads.*;

import java.util.*;

public class Holo implements ModInitializer {



    public static final String MOD_ID = "holo";
    public static MinecraftServer server;
    public static HashMap<BattleHologramComputerEntry, ArrayList<PlayerEntity>> playersWaitingForBattle = new HashMap<>();
    public static ArrayList<ArrayList<PlayerEntity>> fights = new ArrayList<>();


    public static final CustomPayload.Id<SwapC2SPacket> SWAP_PACKET = SwapC2SPacket.ID;

    public static final CustomPayload.Id<HoloModeSwitchS2CPacket> HOLO_MODE = HoloModeSwitchS2CPacket.ID;

    public static final CustomPayload.Id<TerminalCommandC2SPacket> TERMINAL_COMMAND = TerminalCommandC2SPacket.ID;

    public static final CustomPayload.Id<SendTerminalAutocompleteS2CPacket> SEND_TERMINAL_AUTOCOMPLETE = SendTerminalAutocompleteS2CPacket.ID;
    public static final CustomPayload.Id<RequestTerminalAutocompleteC2SPacket> REQUEST_TERMINAL_AUTOCOMPLETE = RequestTerminalAutocompleteC2SPacket.ID;
    public static final CustomPayload.Id<TemporarilyShowEntityS2CPacket> TEMPORARILY_SHOW_ENTITY = TemporarilyShowEntityS2CPacket.ID;
    public static final Identifier OPEN_BATTLE_COMPUTER_SCREEN = Identifier.of(MOD_ID, "open_battle_computer_screen");

    public static Identifier HUMAN_DAMAGE_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_damage");
    public static Identifier HUMAN_DAMAGE_SPEED_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_damage_speed");
    public static Identifier HUMAN_SPEED_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_speed");
    public static Identifier HUMAN_HEALTH_ID = Identifier.of(Holo.MOD_ID, "human_health");

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModBlocks.initialize();
        ModItems.initialize();
        TerminalCommandParser.initCommands();
        ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MOD_ID,"noholooverlay"), FabricLoader.getInstance().getModContainer(MOD_ID).get(), ResourcePackActivationType.NORMAL);
        PayloadTypeRegistry.playS2C().register(HoloModeSwitchS2CPacket.ID, HoloModeSwitchS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SendTerminalAutocompleteS2CPacket.ID, SendTerminalAutocompleteS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TemporarilyShowEntityS2CPacket.ID, TemporarilyShowEntityS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapC2SPacket.ID, SwapC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TerminalCommandC2SPacket.ID, TerminalCommandC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestTerminalAutocompleteC2SPacket.ID, RequestTerminalAutocompleteC2SPacket.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("holo_loreMode").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
                if (context.getSource().getPlayer() != null) {
                    switchShellMode(context.getSource().getPlayer(), true, false);
                    HoloNbtManager.getPlayerState(context.getSource().getPlayer()).loreAccurate = !HoloNbtManager.getPlayerState(context.getSource().getPlayer()).loreAccurate;
                    getHumanAttributes(context.getSource().getPlayer()).forEach((attributeEntry, modifier) -> {
                        if (context.getSource().getPlayer().getAttributeInstance(attributeEntry) != null) {
                            if (!context.getSource().getPlayer().getAttributeInstance(attributeEntry).hasModifier(modifier.id())) {
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

                if (HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    swapBody(player,false,false);
                    updateAttributesAndUpdateMode(player);
                }
                for (FakestPlayer clone : HoloNbtManager.getPlayerState(player).clones) {
                    player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(clone.getUuid())));
                    clone.getServer().getPlayerManager().remove(clone);
                }
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register((srv)->{
            server = srv;
            fights.clear();
            playersWaitingForBattle.clear();
            if (server.getOverworld().hasAttached(HoloNbtManager.holoData)) {
                HoloNbtManager.INSTANCE = HoloNbtManager.createFromNbt(server.getOverworld().getAttached(HoloNbtManager.holoData));
            } else {
                HoloNbtManager.INSTANCE = new HoloNbtManager();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((srv)->{
            NbtCompound holoDat = new NbtCompound();
            HoloNbtManager.getServerState(srv).writeNbt(holoDat);
            srv.getOverworld().setAttached(HoloNbtManager.holoData, holoDat);
        });
        ServerPlayConnectionEvents.DISCONNECT.register(((serverPlayNetworkHandler, minecraftServer) -> {
            for (FakestPlayer clone : HoloNbtManager.getPlayerState(serverPlayNetworkHandler.player).clones) {
                clone.getServer().getPlayerManager().remove(clone);
                clone.remove(Entity.RemovalReason.DISCARDED);
            }
        }));

        ArrayList<ServerPlayerEntity> bufferedKeys = new ArrayList<>();
        ServerPlayNetworking.registerGlobalReceiver(Holo.REQUEST_TERMINAL_AUTOCOMPLETE, (payload, context) -> {
            ArrayList<String> commands = new ArrayList<>();
            for (TerminalCommand value : TerminalCommandParser.nameAndCommands.values()) {
                commands.addAll(value.autoCompletion(context.player()));
            }
            ServerPlayNetworking.send(context.player(), new SendTerminalAutocompleteS2CPacket(commands));
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.TERMINAL_COMMAND, (payload, context) -> {
            String command = payload.command();
            if (HoloNbtManager.getPlayerState(context.player()).inHoloMode) {
                context.player().sendMessage(TerminalCommandParser.findAndRunCommand(command, context.player()));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.SWAP_PACKET, (payload, context) -> {
            if (HoloNbtManager.getPlayerState(context.player()).inHoloMode) {
                if (!bufferedKeys.contains(context.player())) {
                    bufferedKeys.add(context.player());
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register(((old, newPlayer, b) -> {
            if (HoloNbtManager.getPlayerState(newPlayer).inHoloMode) {
                switchShellMode(newPlayer, true, false);
            } else if (HoloNbtManager.getPlayerState(newPlayer).loreAccurate) {
                newPlayer.getServer().getPlayerManager().getUserBanList().add(new BannedPlayerEntry(newPlayer.getGameProfile()));
                newPlayer.networkHandler.sendPacket(new DisconnectS2CPacket(Text.translatable("multiplayer.disconnect.banned")));
            }
        }));
        ServerPlayerEvents.ALLOW_DEATH.register(((serverPlayerEntity, damageSource, v) -> {
            if (HoloNbtManager.getPlayerState(serverPlayerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                resetFromFight(serverPlayerEntity);
                return false;
            }
            return true;
        }));
        ServerTickEvents.START_SERVER_TICK.register((server -> {

            fights.removeIf((fight)->{
                fight.removeIf((p)->{
                    if (!p.isPartOfGame() || !HoloNbtManager.getPlayerState(p).inHoloMode|| !HoloNbtManager.getPlayerState(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
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
                        FakestPlayer player1 = summonNewBody(player, HoloNbtManager.getPlayerState(player).inHoloMode, HologramType.NORMAL, "duel_holo");
                        FakestPlayer player2 = summonNewBody(player, true, HologramType.BATTLE_DUEL, "duel_holo");

                        HoloNbtManager.getPlayerState(player).inHoloMode = true;
                        HoloNbtManager.getPlayerState(player).hologramType = HologramType.BATTLE_DUEL;
                        tinyPlayerClone((ServerPlayerEntity) player, player1);
                        tinyPlayerClone(player2, (ServerPlayerEntity) player);
                        HoloNbtManager.getPlayerState(player).clones.remove(player2);
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
                HoloNbtManager.getPlayerState(p).clones.removeIf((fp) -> {
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
                if (HoloNbtManager.getPlayerState(p).inHoloMode) {
                    if (HoloNbtManager.getPlayerState(p).hologramType.equals(HologramType.BATTLE) || HoloNbtManager.getPlayerState(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                        p.getHungerManager().setFoodLevel(19);
                    } else {
                        p.getHungerManager().setFoodLevel(20);
                    }
                }
            });
            for (ServerPlayerEntity bufferPlayer : bufferedKeys) {
                if (bufferedKeys.contains(bufferPlayer)) {
                    FakestPlayer fakestPlayer = HoloNbtManager.getPlayerState(bufferPlayer).clones.get(0);
                    swapBody(bufferPlayer, fakestPlayer, !HoloNbtManager.getPlayerState(bufferPlayer).hologramType.equals(HologramType.BATTLE_DUEL));
                    updateAttributesAndUpdateMode(bufferPlayer);
                }
            }
            bufferedKeys.clear();
        }));
    }


    public static void resetFromFight(PlayerEntity player) {
        if (player.isPartOfGame() && HoloNbtManager.getPlayerState(player).inHoloMode && HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
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

    public static ImmutableMultimap<RegistryEntry<EntityAttribute>,EntityAttributeModifier> getHoloAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<RegistryEntry<EntityAttribute>, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (!HoloNbtManager.getPlayerState(player).loreAccurate) {
            if (!HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE)) {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID,  (double) -100, EntityAttributeModifier.Operation.ADD_VALUE));
            } else {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID,  (double) -4.5, EntityAttributeModifier.Operation.ADD_VALUE));
            }
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID, (double) -0.9, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID,  (double) -0.075, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        return builder.build();
    }
    public static ImmutableMultimap<RegistryEntry<EntityAttribute>,EntityAttributeModifier> getHumanAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<RegistryEntry<EntityAttribute>, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (HoloNbtManager.getPlayerState(player).loreAccurate) {
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID, (double) -3.5, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID,  -0.8, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID,  (double) -0.045, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        return builder.build();
    }




    public static boolean canSwapBody(PlayerEntity player, boolean holo) {

        FakestPlayer bodyToTake = null;
        for (FakestPlayer clone : HoloNbtManager.getPlayerState(player).clones) {
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
            HoloNbtManager.getPlayerState(player).clones.remove(bodyToTake);
            HoloNbtManager.getPlayerState(player).inHoloMode = bodyToTake.isHologram;
            HoloNbtManager.getPlayerState(player).holoName = bodyToTake.holoName;
            HoloNbtManager.getPlayerState(player).hologramType = bodyToTake.type;

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
        for (FakestPlayer clone : HoloNbtManager.getPlayerState(player).clones) {
            if (clone.isHologram == holo)  { bodyToTake = clone; break; }
        }
        if (bodyToTake != null) {
            HoloNbtManager.getPlayerState(player).inHoloMode = holo;
            HoloNbtManager.getPlayerState(player).holoName = bodyToTake.holoName;
            if (holo) {
                HoloNbtManager.getPlayerState(player).hologramType = bodyToTake.type;
            }
            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);
            HoloNbtManager.getPlayerState(player).clones.remove(bodyToTake);
            player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(bodyToTake.getUuid())));
            bodyToTake.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private static void summonBody(PlayerEntity player) {
        summonBody(player, HoloNbtManager.getPlayerState(player).inHoloMode);
    }

    public static FakestPlayer summonNewBody(PlayerEntity player, boolean holoMode, HologramType type, String holoName) {
        GameProfile profile = new GameProfile(Holo.getFreeUUID(), "");
        profile.getProperties().putAll(player.getGameProfile().getProperties());
        FakestPlayer fakePlayer = FakestPlayer.get((ServerWorld) player.getWorld(), profile, player.getNameForScoreboard(), player.getUuid());
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
        HoloNbtManager.getPlayerState(player).clones.add(fakePlayer);
        HoloNbtManager.getPlayerState(player).playerName = player.getNameForScoreboard();
        player.getWorld().getServer().forcePlayerSampleUpdate();
        return fakePlayer;
    }

    public static void summonBody(PlayerEntity player, boolean holoMode) {
        FakestPlayer fakePlayer = summonNewBody(player,holoMode, HoloNbtManager.getPlayerState(player).hologramType, HoloNbtManager.getPlayerState(player).holoName);
        Holo.tinyPlayerClone(player, fakePlayer);
    }

    public static void switchShellMode(PlayerEntity player, boolean shell, boolean bodyStays) {
        swapBody(player, !shell,bodyStays);
        updateAttributesAndUpdateMode(player);
    }


    public static void updateAttributesAndUpdateMode(PlayerEntity player) {
        if (HoloNbtManager.getPlayerState(player).inHoloMode) {
            if (!HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                getHoloAttributes(player).forEach((attributeEntry, modifier) -> {
                    if (player.getAttributeInstance(attributeEntry) != null) {
                        if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier.id())) {
                            player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                        }
                    }
                });
            }
            player.getAttributes().removeModifiers(getHumanAttributes(player));
            HoloModeUpdates.sendHoloModeUpdate(player);
        } else {
            HoloNbtManager.getPlayerState(player).hologramType = HologramType.NORMAL;
            getHumanAttributes(player).forEach((attributeEntry, modifier) -> {
                    if (player.getAttributeInstance(attributeEntry) != null) {
                        if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier.id())) {
                            player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                        }
                    }
            });

            player.getAttributes().removeModifiers(getHoloAttributes(player));
            HoloModeUpdates.sendHumanModeUpdate(player);
        }
    }
}
