package org.agmas.holo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.agmas.holo.block.HologramController;
import org.agmas.holo.compat.HardcoreRevivalCompat;
import org.agmas.holo.mixin.PlayerEntityAccessor;
import org.agmas.holo.state.ClonePlayerComponent;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.state.StyleMeterComponent;
import org.agmas.holo.statusEffects.ModStatusEffects;
import org.agmas.holo.terminalCommands.TerminalCommand;
import org.agmas.holo.terminalCommands.TerminalCommandParser;
import org.agmas.holo.util.*;
import org.agmas.holo.util.payloads.*;
import org.agmas.holo.voicechat.HoloVoicechatPlugin;

import java.util.*;

public class Holo implements ModInitializer {
    public static final String MOD_ID = "holo";
    public static MinecraftServer server;
    public static HashMap<BattleHologramComputerEntry, ArrayList<PlayerEntity>> playersWaitingForBattle = new HashMap<>();
    public static ArrayList<ArrayList<PlayerEntity>> fights = new ArrayList<>();

    public static Map<HoloModifiers, Integer> modifierToPower = ImmutableMap.of(
            HoloModifiers.OFFENSE, 2,
            HoloModifiers.MOBILITY, 1,
            HoloModifiers.NIGHT_VISION, 3,
            HoloModifiers.GLIDER, 3,
            HoloModifiers.CONSTRUCTION, 2
    );
    public static List<HoloModifiers> loreAllowedModifiers = List.of(
            HoloModifiers.NIGHT_VISION,
            HoloModifiers.GLIDER
    );

    public static final CustomPayload.Id<SwapC2SPacket> SWAP_PACKET = SwapC2SPacket.ID;

    public static final CustomPayload.Id<HoloModeSwitchS2CPacket> HOLO_MODE = HoloModeSwitchS2CPacket.ID;

    public static final CustomPayload.Id<TerminalCommandC2SPacket> TERMINAL_COMMAND = TerminalCommandC2SPacket.ID;

    public static final CustomPayload.Id<SendCallC2SPacket> SEND_CALL = SendCallC2SPacket.ID;

    public static final CustomPayload.Id<SendTerminalAutocompleteS2CPacket> SEND_TERMINAL_AUTOCOMPLETE = SendTerminalAutocompleteS2CPacket.ID;
    public static final CustomPayload.Id<RequestTerminalAutocompleteC2SPacket> REQUEST_TERMINAL_AUTOCOMPLETE = RequestTerminalAutocompleteC2SPacket.ID;
    public static final CustomPayload.Id<TemporarilyShowEntityS2CPacket> TEMPORARILY_SHOW_ENTITY = TemporarilyShowEntityS2CPacket.ID;
    public static final CustomPayload.Id<HoloStatusInfoS2CPacket> HOLO_STATUS_INFO = HoloStatusInfoS2CPacket.ID;

    public static Identifier HUMAN_DAMAGE_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_damage");
    public static Identifier HUMAN_DAMAGE_SPEED_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_damage_speed");
    public static Identifier HUMAN_SPEED_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_speed");

    public static Identifier SCOUT_SPEED_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "scout_speed");
    public static Identifier SCOUT_JUMP_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "scout_jump");
    public static Identifier SCOUT_STEP_HEIGHT_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "scout_step_height");
    public static Identifier SCOUT_HEALTH_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "scout_health");

    public static Identifier HUMAN_JUMP_MODIFIER_ID = Identifier.of(Holo.MOD_ID, "human_health");
    public static SoundEvent holo_switch;
    public static SoundEvent whatsappdanger;
    public static SoundEvent holo_death;

    public static SoundEvent incoming;
    public static SoundEvent xylophone;
    public static SoundEvent third_sanctuary;
    public static SoundEvent seaside;
    public static SoundEvent marimba;
    public static List<SoundEvent>  ringtones;

    public static ArrayList<FakestPlayer> queuedHoloRemovals = new ArrayList<>();

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModBlocks.initialize();
        ModItems.initialize();
        ModStatusEffects.init();
        TerminalCommandParser.initCommands();
        ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MOD_ID,"noholooverlay"), FabricLoader.getInstance().getModContainer(MOD_ID).get(), ResourcePackActivationType.NORMAL);
        PayloadTypeRegistry.playS2C().register(HoloModeSwitchS2CPacket.ID, HoloModeSwitchS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SendTerminalAutocompleteS2CPacket.ID, SendTerminalAutocompleteS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TemporarilyShowEntityS2CPacket.ID, TemporarilyShowEntityS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(HoloStatusInfoS2CPacket.ID, HoloStatusInfoS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapC2SPacket.ID, SwapC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TerminalCommandC2SPacket.ID, TerminalCommandC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestTerminalAutocompleteC2SPacket.ID, RequestTerminalAutocompleteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SendCallC2SPacket.ID, SendCallC2SPacket.CODEC);

        holo_switch = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "holo_switch"),
                SoundEvent.of(Identifier.of(MOD_ID, "holo_switch")));
        whatsappdanger = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "whatsappdanger"),
                SoundEvent.of(Identifier.of(MOD_ID, "whatsappdanger")));
        holo_death = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "holo_death"),
                SoundEvent.of(Identifier.of(MOD_ID, "holo_death")));

        // Ringtones

        incoming = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "incoming"),
                SoundEvent.of(Identifier.of(MOD_ID, "incoming")));
        marimba = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "marimba"),
                SoundEvent.of(Identifier.of(MOD_ID, "marimba")));
        xylophone = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "xylophone"),
                SoundEvent.of(Identifier.of(MOD_ID, "xylophone")));
        seaside = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "seaside"),
                SoundEvent.of(Identifier.of(MOD_ID, "seaside")));
        third_sanctuary = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "third_sanctuary"),
                SoundEvent.of(Identifier.of(MOD_ID, "third_sanctuary")));

        ringtones = List.of(
                incoming, xylophone, third_sanctuary, seaside, marimba
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("clearPlayerHoloData").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).then(CommandManager.argument("player", EntityArgumentType.player()).executes(commandContext -> {
                ServerPlayerEntity entity = EntityArgumentType.getPlayer(commandContext, "player");
                entity.networkHandler.sendPacket(new DisconnectS2CPacket(Text.literal("Your holo data was reset by an admin\nYou will be put into human form the same way you were upon disconnecting.")));
                HoloPlayerComponent.KEY.get(entity).reset();

                return 1;
        })));
            dispatcher.register(CommandManager.literal("holo_loreMode").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2) || serverCommandSource.getPlayer().getUuidAsString().equals("5de5299b-83c1-4fe4-9c47-b8aae4fed6b1")).executes(context -> {
                if (context.getSource().getPlayer() != null) {
                    switchShellMode(context.getSource().getPlayer(), true, false);
                    HoloPlayerComponent.KEY.get(context.getSource().getPlayer()).loreAccurate = !HoloPlayerComponent.KEY.get(context.getSource().getPlayer()).loreAccurate;
                    HoloPlayerComponent.KEY.sync(context.getSource().getPlayer());
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

        if (FabricLoader.getInstance().isModLoaded("hardcorerevival")) {
            HardcoreRevivalCompat.register();
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((t)->{
            t.add(ModItems.HOLOGRAM_SPAWN_EGG);
            t.add(ModItems.BATTLE_HOLOGRAM_SPAWN_EGG);
            t.add(ModItems.SILENT_HOLOGRAM_SPAWN_EGG);
            t.add(ModItems.SCOUT_HOLOGRAM_SPAWN_EGG);
            t.add(ModItems.CAMERA_HOLOGRAM_SPAWN_EGG);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((t)->{
            t.add(ModBlocks.HOLOGRAM_CONTROLLER);
            t.add(ModBlocks.BATTLE_HOLOGRAM_CONTROLLER);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((srv)->{
            for (PlayerEntity player : srv.getPlayerManager().getPlayerList()) {

                if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    swapBody(player,false,false);
                    updateAttributesAndUpdateMode(player);
                }
                for (FakestPlayer clone : ClonePlayerComponent.KEY.get(player).clones) {
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
            if (HoloPlayerComponent.KEY.get(serverPlayNetworkHandler.player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                resetFromFight(serverPlayNetworkHandler.player);
            }
            queuedHoloRemovals.addAll(ClonePlayerComponent.KEY.get(serverPlayNetworkHandler.player).clones);
        }));

        ArrayList<ServerPlayerEntity> bufferedKeys = new ArrayList<>();
        ServerPlayNetworking.registerGlobalReceiver(Holo.REQUEST_TERMINAL_AUTOCOMPLETE, (payload, context) -> {
            ArrayList<String> commands = new ArrayList<>();
            for (TerminalCommand value : TerminalCommandParser.nameAndCommands.values()) {
                if (HoloPlayerComponent.KEY.get(context.player()).inHoloMode || value.usableAsHuman())
                    commands.addAll(value.autoCompletion(context.player()));
            }
            ServerPlayNetworking.send(context.player(), new SendTerminalAutocompleteS2CPacket(commands));
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.TERMINAL_COMMAND, (payload, context) -> {
            String command = payload.command();
            if (HoloPlayerComponent.KEY.get(context.player()).hologramType != HologramType.BATTLE_DUEL) {
                context.player().sendMessage(TerminalCommandParser.findAndRunCommand(command, context.player()));
            } else{
                context.player().sendMessage(Text.literal("You can't use the terminal in a duel!").formatted(Formatting.RED));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.SEND_CALL, (payload, context) -> {
            ServerPlayerEntity playerEntity = context.server().getPlayerManager().getPlayer(payload.target());
            if (playerEntity != null) {
                VoicechatServerApi api = (VoicechatServerApi) HoloVoicechatPlugin.api;
                Group group = api.groupBuilder()
                        .setPersistent(false)
                        .setName("Call for " + playerEntity.getNameForScoreboard())
                        .setHidden(true)
                        .setType(Group.Type.OPEN)
                        .build();

                VoicechatConnection connection = api.getConnectionOf(playerEntity.getUuid());
                if (connection == null) {
                    return;
                }
                connection.setGroup(group);
                int ringtone = playerEntity.getRandom().nextBetween(1,Holo.ringtones.size());
                HoloPlayerComponent.KEY.get(context.player()).callSound = ringtone;
                HoloPlayerComponent.KEY.get(playerEntity).startCall(playerEntity.getUuid(), group,ringtone);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Holo.SWAP_PACKET, (payload, context) -> {
            if (HoloPlayerComponent.KEY.get(context.player()).inHoloMode) {
                if (!bufferedKeys.contains(context.player())) {
                    bufferedKeys.add(context.player());
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register(((old, newPlayer, b) -> {
            queuedHoloRemovals.addAll(ClonePlayerComponent.KEY.get(old).clones);
            if (HoloPlayerComponent.KEY.get(newPlayer).inHoloMode) {
                switchShellMode(newPlayer, true, false);
            } else if (HoloPlayerComponent.KEY.get(newPlayer).loreAccurate) {
                newPlayer.getServer().getPlayerManager().getUserBanList().add(new BannedPlayerEntry(newPlayer.getGameProfile()));
                newPlayer.networkHandler.sendPacket(new DisconnectS2CPacket(Text.translatable("multiplayer.disconnect.banned")));
            }
            HoloModeUpdates.spawnHolosOnClient(newPlayer);
        }));
        ServerPlayerEvents.ALLOW_DEATH.register(((serverPlayerEntity, damageSource, v) -> {

            if (HoloPlayerComponent.KEY.get(serverPlayerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                Text text = serverPlayerEntity.getDamageTracker().getDeathMessage();

                serverPlayerEntity.networkHandler.send(new DeathMessageS2CPacket(serverPlayerEntity.getId(), text), PacketCallbacks.of(() -> {
                    int i = 256;
                    String string = text.asTruncatedString(256);
                    Text text2 = Text.translatable("death.attack.message_too_long", new Object[]{Text.literal(string).formatted(Formatting.YELLOW)});
                    Text text3 = Text.translatable("death.attack.even_more_magic", new Object[]{serverPlayerEntity.getDisplayName()}).styled((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2)));
                    return new DeathMessageS2CPacket(serverPlayerEntity.getId(), text3.copy().withColor(Colors.BLUE));
                }));
                serverPlayerEntity.server.getPlayerManager().broadcast(text.copy().withColor(Colors.BLUE), false);
                resetFromFight(serverPlayerEntity);
                return false;
            }
            if (HoloPlayerComponent.KEY.get(serverPlayerEntity).inHoloMode) {
                Holo.switchShellMode(serverPlayerEntity, true, false);
                serverPlayerEntity.getServer().sendMessage(damageSource.getDeathMessage(serverPlayerEntity).copy().formatted(Formatting.AQUA));
                return false;
            }
            return true;
        }));
        ServerTickEvents.END_SERVER_TICK.register((server -> {
            queuedHoloRemovals.removeIf((clone)->{
                clone.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(clone.getUuid())));
                clone.getServer().getPlayerManager().remove(clone);
                return true;
            });
            fights.removeIf((fight)->{
                fight.removeIf((p)->{
                    if (!p.isPartOfGame() || !HoloPlayerComponent.KEY.get(p).inHoloMode|| !HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
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
            Iterator<Map.Entry<BattleHologramComputerEntry, ArrayList<PlayerEntity>>> iterator = playersWaitingForBattle.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BattleHologramComputerEntry, ArrayList<PlayerEntity>> entry = iterator.next();
                entry.getValue().removeIf((p)->{
                    return p.getWorld() != entry.getKey().world || !p.isPartOfGame();
                });
                ArrayList<PlayerEntity> fight = new ArrayList<>();
                if (entry.getValue().size() >= entry.getKey().count || (entry.getValue().size() >= 2 && entry.getKey().start)) {
                    entry.getKey().start = false;
                    entry.getKey().world.setBlockState(entry.getKey().pos, entry.getKey().world.getBlockState(entry.getKey().pos).with(HologramController.USING, false));
                    int i2 = 0;
                    for (PlayerEntity player : entry.getValue()) {
                        i2++;
                        FakestPlayer player1 = summonNewBody(player, HoloPlayerComponent.KEY.get(player).inHoloMode, HoloPlayerComponent.KEY.get(player).hologramType, HoloPlayerComponent.KEY.get(player).holoName);
                        FakestPlayer player2 = summonNewBody(player, true, HologramType.BATTLE_DUEL, "duel_holo_" + i2);

                        HoloPlayerComponent.KEY.get(player).inHoloMode = true;
                        HoloPlayerComponent.KEY.get(player).holoName = "duel_holo_" + i2;
                        HoloPlayerComponent.KEY.get(player).hologramType = HologramType.BATTLE_DUEL;
                        HoloPlayerComponent.KEY.get(player).battleUsesNormalSaturation = !entry.getKey().hologramOptions.holoSaturation;
                        HoloPlayerComponent.KEY.get(player).loreModeBattleStoredInv = new PlayerInventory(player);
                        HoloPlayerComponent.KEY.get(player).loreModeBattleStoredInv.clone(player.getInventory());
                        HoloPlayerComponent.KEY.get(player).loreModeBattleStoredOffhand = player.getOffHandStack();
                        tinyPlayerClone((ServerPlayerEntity) player, player1);
                        tinyPlayerClone(player2, (ServerPlayerEntity) player);
                        player.requestTeleport(entry.getKey().pos.getX(), entry.getKey().pos.getY(), entry.getKey().pos.getZ());
                        ClonePlayerComponent.KEY.get(player).clones.remove(player2);
                        queuedHoloRemovals.add(player2);
                        HoloModeUpdates.sendHoloModeUpdate(player);
                        fight.add(player);
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            if (entry.getKey().hologramOptions.noEnchantments) {
                                ItemStack stack = player1.getInventory().getStack(i).copy();
                                stack.remove(DataComponentTypes.ENCHANTMENTS);
                                player.getInventory().setStack(i, stack);
                            } else {
                                player.getInventory().setStack(i, player1.getInventory().getStack(i).copy());
                            }
                        }
                        player.getInventory().markDirty();
                        if (entry.getKey().hologramOptions.alwaysHaveBestPotions) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, StatusEffectInstance.INFINITE, 2));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, StatusEffectInstance.INFINITE, 2));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE, 1));
                        }

                        WorldBorder worldBorder = new WorldBorder();
                        worldBorder.setCenter(entry.getKey().pos.getX() * entry.getKey().world.getDimension().coordinateScale(), entry.getKey().pos.getZ() * entry.getKey().world.getDimension().coordinateScale());
                        worldBorder.setSize(entry.getKey().hologramOptions.worldBorderSize);
                        if (player instanceof ServerPlayerEntity spe) {
                            if (entry.getKey().hologramOptions.worldBorderSize == 0) {
                                spe.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(worldBorder));
                                spe.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
                            }
                            spe.timeUntilRegen = 20 * 5;
                        }
                    }
                    fights.add(fight);
                    for (PlayerEntity playerEntity : fight) {
                        HoloPlayerComponent.KEY.get(playerEntity).playersInFight.clear();
                        for (PlayerEntity entity : fight) {
                            HoloPlayerComponent.KEY.get(playerEntity).playersInFight.add(entity.getUuid());
                        }
                        HoloPlayerComponent.KEY.get(playerEntity).sync();
                    }
                    entry.getValue().clear();
                } else if (entry.getValue().isEmpty()) {
                    iterator.remove();
                    continue;
                }
                for (PlayerEntity player : entry.getValue()) {
                    player.sendMessage(Text.literal("Waiting for opponent.. (" + entry.getValue().size() + "/" + entry.getKey().count + ")").formatted(Formatting.GOLD), true);
                }
            }
        }));
        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity,damageSource)->{

            if (livingEntity instanceof PlayerEntity playerEntity) {
                if (HoloPlayerComponent.KEY.get(playerEntity).inHoloMode && !HoloPlayerComponent.KEY.get(playerEntity).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    switch (HoloPlayerComponent.KEY.get(playerEntity).hologramType) {
                        case BATTLE -> playerEntity.dropItem(ModItems.BATTLE_HOLOGRAM_SPAWN_EGG);
                        case CAMERA -> playerEntity.dropItem(ModItems.CAMERA_HOLOGRAM_SPAWN_EGG);
                        case SCOUT -> playerEntity.dropItem(ModItems.SCOUT_HOLOGRAM_SPAWN_EGG);
                        case SILENT -> playerEntity.dropItem(ModItems.SILENT_HOLOGRAM_SPAWN_EGG);
                        default -> playerEntity.dropItem(ModItems.HOLOGRAM_SPAWN_EGG);
                    }
                }
            }
            // Non-direct damage types
            if (livingEntity.getAttacker() != null && livingEntity.getAttacker() instanceof PlayerEntity player) {
                if (damageSource.isOf(DamageTypes.PLAYER_EXPLOSION) || damageSource.isOf(DamageTypes.EXPLOSION)) {
                    StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.EXPLODED);
                }
                if (damageSource.isOf(DamageTypes.FALL)) {
                    StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.SPLATTERED);
                }
            }
            if (damageSource.getAttacker() != null && damageSource.getAttacker() instanceof PlayerEntity player) {
                if (damageSource.isOf(DamageTypes.ARROW) || damageSource.isOf(DamageTypes.TRIDENT) || damageSource.isOf(DamageTypes.WIND_CHARGE)) {
                    StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.RANGED_KILL);
                }
                if (livingEntity instanceof PlayerEntity) {
                    StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.KILLED_PLAYER);
                } else {
                    StyleMeterComponent.KEY.get(player).addStylePoints(StyleMeterComponent.StyleReason.KILLED_MOB);
                }
            }
            StyleMeterComponent.handleDamage(livingEntity,damageSource);
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register((livingEntity,damageSource,baseDamageTaken, damageTaken, blocked)->{
            if (!blocked) {
                if (livingEntity instanceof PlayerEntity player) {
                    StyleMeterComponent.KEY.get(player).consecutiveHits = 0;
                    if (player.getUuidAsString().equals("5de5299b-83c1-4fe4-9c47-b8aae4fed6b1")) {
                        if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                            if (damageSource.getAttacker() != null && damageSource.getAttacker() instanceof PlayerEntity player2) {
                                player.getWorld().playSound((Entity)null,player.getBlockPos(),whatsappdanger,SoundCategory.MASTER,1f,(livingEntity.getRandom().nextFloat()*0.3f)+0.9f);
                                StyleMeterComponent.KEY.get(player2).addStylePoints(StyleMeterComponent.StyleReason.WHATSAPP_DANGER);
                            }

                        }
                    }
                }
                StyleMeterComponent.handleDamage(livingEntity,damageSource);
            }
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity,damageSource,damage)->{
            if (livingEntity instanceof PlayerEntity player) {
                if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.SCOUT) && damageSource.isOf(DamageTypes.FALL)) {
                    return false;
                }
            }
            if (livingEntity instanceof FakestPlayer player) {
                if (player.isHologram && !damageSource.isOf(DamageTypes.OUT_OF_WORLD)) {
                    if (damageSource.getAttacker() == null) return false;
                    return damageSource.getAttacker() instanceof PlayerEntity;
                }
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register((s)->{
            s.getPlayerManager().getPlayerList().forEach((p)-> {
                ArrayList<FakestPlayer> clonestoYoink = new ArrayList<>();
                ClonePlayerComponent.KEY.get(p).clones.removeIf((fp) -> {
                    if (!fp.isAlive()) {
                        clonestoYoink.add(fp);
                    }
                    return !fp.isAlive();
                });
                queuedHoloRemovals.addAll(clonestoYoink);
            });
        });
        ServerTickEvents.START_WORLD_TICK.register((serverWorld -> {
            serverWorld.getPlayers().forEach((p)->{
                if (HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    if (p.getVehicle() != null) {
                        p.dismountVehicle();
                    }
                }
                if (HoloPlayerComponent.KEY.get(p).inHoloMode) {
                    if (serverWorld.getTime() % 10 == 0) {
                        sendHoloStatusUpdate(p);
                    }
                    if (!HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                        refreshPower(p);
                   if (HoloPlayerComponent.KEY.get(p).activeModifiers.contains(HoloModifiers.NIGHT_VISION))
                       p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 40, 0));
                   if (HoloPlayerComponent.KEY.get(p).activeModifiers.contains(HoloModifiers.GLIDER))
                       p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0));
                    if (HoloPlayerComponent.KEY.get(p).power > 4+(4*HoloPlayerComponent.KEY.get(p).lastComputerMaxPower)) {
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));

                        if (serverWorld.getTime() % 10 == 0) {
                            p.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), SoundCategory.MASTER,1, 1);
                            p.damage(p.getDamageSources().lightningBolt(), 2);

                            if (p.getHealth() < 4) {
                                bufferedKeys.add(p);
                                HoloPlayerComponent.KEY.get(p).activeModifiers.clear();
                                p.sendMessage(Text.literal("You were kicked out of that hologram for reckless power usage. ").append(Text.literal("Please use less power.").formatted(Formatting.RED)));
                            }
                            p.sendMessage(Text.literal("Decrease your power usage!").formatted(Formatting.RED),true);
                        }
                    }
                    }
                    if (HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.BATTLE) || HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                        if (p.getHungerManager().getSaturationLevel() > 13) {
                            p.getHungerManager().setFoodLevel(20);
                        } else {
                            p.getHungerManager().setFoodLevel(19);
                        }
                    } else {
                        p.getHungerManager().setFoodLevel(20);
                    }
                }
            });
            for (ServerPlayerEntity bufferPlayer : bufferedKeys) {
                if (bufferedKeys.contains(bufferPlayer)) {
                    FakestPlayer fakestPlayer = ClonePlayerComponent.KEY.get(bufferPlayer).clones.get(0);
                    if (HoloPlayerComponent.KEY.get(bufferPlayer).hologramType.equals(HologramType.BATTLE_DUEL)) {
                        resetFromFight(bufferPlayer);
                    } else {
                        swapBody(bufferPlayer, fakestPlayer, !HoloPlayerComponent.KEY.get(bufferPlayer).hologramType.equals(HologramType.BATTLE_DUEL));
                        updateAttributesAndUpdateMode(bufferPlayer);
                    }
                }
            }
            bufferedKeys.clear();
        }));
    }

    public static void sendHoloStatusUpdate(ServerPlayerEntity p) {
        PlayerEntity human = null;
        for (FakestPlayer clone : ClonePlayerComponent.KEY.get(p).clones) {
            if (!clone.isHologram) {
                human = clone;
                break;
            }
        }
        if (human != null) {
            int playersAlive = 0;
            if (HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.BATTLE_DUEL)) {
                for (ArrayList<PlayerEntity> fight : fights) {
                    if (fight.contains(p)) {
                        playersAlive += fight.size()-1;
                        break;
                    }
                }
            }
            ServerPlayNetworking.send(p, new HoloStatusInfoS2CPacket(HoloPlayerComponent.KEY.get(p).holoName, (int) human.getHealth(), HoloPlayerComponent.KEY.get(p).power, 4+ (4*HoloPlayerComponent.KEY.get(p).lastComputerMaxPower), playersAlive));
        }
    }

    public static void refreshPower(PlayerEntity p) {
        HoloPlayerComponent.KEY.get(p).power = ClonePlayerComponent.KEY.get(p).clones.size();

        if (p.getHealth() < 5) {
            HoloPlayerComponent.KEY.get(p).power++;
        }
        if (p.getWorld().getDimension().piglinSafe() || p.isOnFire()) {
            HoloPlayerComponent.KEY.get(p).power++;
        }

        if (HoloPlayerComponent.KEY.get(p).hologramType.equals(HologramType.SCOUT)) {
            HoloPlayerComponent.KEY.sync(p);
            return;
        }
        if (HoloPlayerComponent.KEY.get(p).loreAccurate) HoloPlayerComponent.KEY.get(p).power = 0;
        if (p.isSubmergedInWater()) {
            HoloPlayerComponent.KEY.get(p).power++;
        }
        for (HoloModifiers activeModifier : HoloPlayerComponent.KEY.get(p).activeModifiers) {
            HoloPlayerComponent.KEY.get(p).power += modifierToPower.get(activeModifier);
        }
        HoloPlayerComponent.KEY.sync(p);
    }

    public static void resetFromFight(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity spe) {
            spe.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
            spe.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(player.getWorld().getWorldBorder()));
            spe.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(player.getWorld().getWorldBorder()));
        }
        if (player.isPartOfGame() && HoloPlayerComponent.KEY.get(player).inHoloMode && HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            swapBody(player, false, false);
        }
        updateAttributesAndUpdateMode(player);
        HoloPlayerComponent.KEY.sync(player);
        if (HoloPlayerComponent.KEY.get(player).loreAccurate && HoloPlayerComponent.KEY.get(player).loreModeBattleStoredInv != null) {
            player.getInventory().clone(HoloPlayerComponent.KEY.get(player).loreModeBattleStoredInv);
            player.getInventory().offHand.set(0, HoloPlayerComponent.KEY.get(player).loreModeBattleStoredOffhand);
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
        clone.getAttributes().removeModifiers(getScoutAttributes(clone));
        if (HoloPlayerComponent.KEY.get(original).loreAccurate || HoloPlayerComponent.KEY.get(clone).loreAccurate) {
            if (!(original instanceof FakestPlayer)) {
                clone.getInventory().clone(original.getInventory());
                clone.getInventory().offHand.set(0, original.getOffHandStack());
            }
        } else {
            boolean shouldCopy = original.getHealth() > 0;
            shouldCopy = shouldCopy || (original.getHealth() > 0 && (HoloPlayerComponent.KEY.get(original).hologramType.equals(HologramType.BATTLE_DUEL) || HoloPlayerComponent.KEY.get(clone).hologramType.equals(HologramType.BATTLE_DUEL)));
            if (shouldCopy) {
                clone.getInventory().clone(original.getInventory());
                clone.getInventory().offHand.set(0, original.getOffHandStack());
            }
        }
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
            clone.getDataTracker().set(((PlayerEntityAccessor)clone).PLAYER_MODEL_PARTS(), (byte)spe.getClientOptions().playerModelParts());
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

    public static ImmutableMultimap<RegistryEntry<EntityAttribute>,EntityAttributeModifier> getScoutAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<RegistryEntry<EntityAttribute>, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(SCOUT_SPEED_MODIFIER_ID, (double) 0.175, EntityAttributeModifier.Operation.ADD_VALUE));
        builder.put(EntityAttributes.GENERIC_JUMP_STRENGTH, new EntityAttributeModifier(SCOUT_JUMP_MODIFIER_ID, (double) 0.175, EntityAttributeModifier.Operation.ADD_VALUE));
        builder.put(EntityAttributes.GENERIC_STEP_HEIGHT, new EntityAttributeModifier(SCOUT_JUMP_MODIFIER_ID, (double) 1, EntityAttributeModifier.Operation.ADD_VALUE));
        builder.put(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(SCOUT_HEALTH_MODIFIER_ID, (double) -14, EntityAttributeModifier.Operation.ADD_VALUE));
        return  builder.build();
    }
    public static ImmutableMultimap<RegistryEntry<EntityAttribute>,EntityAttributeModifier> getHoloAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<RegistryEntry<EntityAttribute>, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (!HoloPlayerComponent.KEY.get(player).loreAccurate) {
            if (!HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE)) {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID,  (double) -100, EntityAttributeModifier.Operation.ADD_VALUE));
            } else {
                builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID,  (double) -4.5, EntityAttributeModifier.Operation.ADD_VALUE));
            }
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID, (double) -0.9, EntityAttributeModifier.Operation.ADD_VALUE));
            if (!HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.SCOUT)) {
                builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID, (double) -0.075, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.SCOUT)) {
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(SCOUT_SPEED_MODIFIER_ID, (double) 0.175, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_JUMP_STRENGTH, new EntityAttributeModifier(SCOUT_JUMP_MODIFIER_ID, (double) 0.175, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_STEP_HEIGHT, new EntityAttributeModifier(SCOUT_JUMP_MODIFIER_ID, (double) 1, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(SCOUT_HEALTH_MODIFIER_ID, (double) -14, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        return builder.build();
    }
    public static ImmutableMultimap<RegistryEntry<EntityAttribute>,EntityAttributeModifier> getHumanAttributes(PlayerEntity player) {
        ImmutableMultimap.Builder<RegistryEntry<EntityAttribute>, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        if (HoloPlayerComponent.KEY.get(player).loreAccurate) {
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID, (double) -3.5, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID,  -0.8, EntityAttributeModifier.Operation.ADD_VALUE));
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID,  (double) -0.045, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        return builder.build();
    }




    public static boolean canSwapBody(PlayerEntity player, boolean holo) {

        FakestPlayer bodyToTake = null;
        for (FakestPlayer clone : ClonePlayerComponent.KEY.get(player).clones) {
            if (clone.isHologram == holo)  {
                return true;
            }
        }
        return false;
    }

    public static void turnOffComputers(PlayerEntity player) {
        if (HoloPlayerComponent.KEY.get(player).computerPos != null && HoloPlayerComponent.KEY.get(player).computerWorld != null) {
            BlockState state = player.getServer().getWorld(HoloPlayerComponent.KEY.get(player).computerWorld).getBlockState(HoloPlayerComponent.KEY.get(player).computerPos);
            if (state.isOf(ModBlocks.HOLOGRAM_CONTROLLER)) {
                player.getServer().getWorld(HoloPlayerComponent.KEY.get(player).computerWorld).setBlockState(HoloPlayerComponent.KEY.get(player).computerPos, state.with(HologramController.USING, false));
            }
        }
    }

    public static void swapBody(PlayerEntity player, FakestPlayer bodyToTake, boolean bodyStays) {
        player.dismountVehicle();

        if (bodyStays)
            summonBody(player);

        if (bodyToTake != null) {
            ClonePlayerComponent.KEY.get(player).clones.remove(bodyToTake);
            HoloPlayerComponent.KEY.get(player).inHoloMode = bodyToTake.isHologram;
            HoloPlayerComponent.KEY.get(player).holoName = bodyToTake.holoName;
            HoloPlayerComponent.KEY.get(player).hologramType = bodyToTake.type;

            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            updateAttributesAndUpdateMode(player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);
            sendHoloStatusUpdate((ServerPlayerEntity) player);
            if (!bodyToTake.isHologram)
                turnOffComputers(player);
            queuedHoloRemovals.add(bodyToTake);
        }
    }
    public static void swapBody(PlayerEntity player, boolean holo, boolean bodyStays) {
        player.dismountVehicle();
        if (bodyStays)
            summonBody(player);

        FakestPlayer bodyToTake = null;
        for (FakestPlayer clone : ClonePlayerComponent.KEY.get(player).clones) {
            if (clone.isHologram == holo)  { bodyToTake = clone; break; }
        }
        if (bodyToTake != null) {
            HoloPlayerComponent.KEY.get(player).inHoloMode = holo;
            HoloPlayerComponent.KEY.get(player).holoName = bodyToTake.holoName;
            if (holo) {
                HoloPlayerComponent.KEY.get(player).hologramType = bodyToTake.type;
            }
            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);
            ClonePlayerComponent.KEY.get(player).clones.remove(bodyToTake);
            if (!holo)
                turnOffComputers(player);
            sendHoloStatusUpdate((ServerPlayerEntity) player);
            queuedHoloRemovals.add(bodyToTake);
        }
    }

    private static void summonBody(PlayerEntity player) {
        summonBody(player, HoloPlayerComponent.KEY.get(player).inHoloMode);
    }

    public static FakestPlayer summonNewBody(PlayerEntity player, boolean holoMode, HologramType type, String holoName) {
        GameProfile profile = new GameProfile(Holo.getFreeUUID(), "");
        profile.getProperties().putAll(player.getGameProfile().getProperties());
        FakestPlayer fakePlayer = FakestPlayer.get((ServerWorld) player.getWorld(), profile, player.getNameForScoreboard(), player.getUuid());
        if (player instanceof FakestPlayer fp) {
            fakePlayer.ownerUUID = fp.ownerUUID;
            fakePlayer.ownerName = fp.ownerName;
        }
        player.getWorld().getServer().getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(fakePlayer)));
        fakePlayer.setServerWorld((ServerWorld) player.getWorld());
        fakePlayer.refreshPositionAndAngles(player.getX(),player.getY(),player.getZ(),player.getYaw(),player.getPitch());
        if (player instanceof ServerPlayerEntity fp) {
            fakePlayer.getDataTracker().set(((PlayerEntityAccessor) fakePlayer).PLAYER_MODEL_PARTS(), (byte) fp.getClientOptions().playerModelParts());
        }

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
        if (!(player instanceof FakestPlayer)) {
            ClonePlayerComponent.KEY.get(player).clones.add(fakePlayer);
        }
        player.getWorld().getServer().forcePlayerSampleUpdate();
        HoloPlayerComponent.KEY.sync(player);
        return fakePlayer;
    }

    public static void summonBody(PlayerEntity player, boolean holoMode) {
        FakestPlayer fakePlayer = summonNewBody(player,holoMode, HoloPlayerComponent.KEY.get(player).hologramType, HoloPlayerComponent.KEY.get(player).holoName);
        Holo.tinyPlayerClone(player, fakePlayer);
    }

    public static void switchShellMode(PlayerEntity player, boolean shell, boolean bodyStays) {
        swapBody(player, !shell,bodyStays);
        updateAttributesAndUpdateMode(player);
        HoloPlayerComponent.KEY.sync(player);
    }

    public static void updateAttributesForModifiers(PlayerEntity player, boolean refresh) {
        if (HoloPlayerComponent.KEY.get(player).inHoloMode) {
            if (refresh) {
                if (!HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                    getHoloAttributes(player).forEach((attributeEntry, modifier) -> {
                        if (player.getAttributeInstance(attributeEntry) != null) {
                            if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier.id())) {
                                player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                            }
                        }
                    });
                }
            }

            if (HoloPlayerComponent.KEY.get(player).activeModifiers.contains(HoloModifiers.MOBILITY) || HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.SCOUT))
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(HUMAN_SPEED_MODIFIER_ID);

            if (HoloPlayerComponent.KEY.get(player).activeModifiers.contains(HoloModifiers.OFFENSE)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).removeModifier(HUMAN_DAMAGE_MODIFIER_ID);
                player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).removeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID);
            }
        }
    }

    public static void updateAttributesAndUpdateMode(PlayerEntity player) {
        if (HoloPlayerComponent.KEY.get(player).inHoloMode) {
            if (!HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
                getHoloAttributes(player).forEach((attributeEntry, modifier) -> {
                    if (player.getAttributeInstance(attributeEntry) != null) {
                        if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier.id())) {
                            player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                        }
                    }
                });
            }
            if (!HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.SCOUT)) {
                player.getAttributes().removeModifiers(getScoutAttributes(player));
            }
            player.getAttributes().removeModifiers(getHumanAttributes(player));
            HoloModeUpdates.sendHoloModeUpdate(player);
            updateAttributesForModifiers(player,false);
        } else {
            player.getAttributes().removeModifiers(getScoutAttributes(player));
            player.getAttributes().removeModifiers(getHoloAttributes(player));
            HoloPlayerComponent.KEY.get(player).hologramType = HologramType.NORMAL;
            getHumanAttributes(player).forEach((attributeEntry, modifier) -> {
                    if (player.getAttributeInstance(attributeEntry) != null) {
                        if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier.id())) {
                            player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                        }
                    }
            });

            HoloModeUpdates.sendHumanModeUpdate(player);
        }
        HoloPlayerComponent.KEY.sync(player);
    }


}
