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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModeUpdates;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Holo implements ModInitializer {



    public static final String MOD_ID = "holo";
    public static MinecraftServer server;

    public static final Identifier HUMAN_MODE = new Identifier(MOD_ID, "human_mode");
    public static final Identifier HOLO_MODE = new Identifier(MOD_ID, "holo_mode");
    public static final Identifier SWAP_PACKET = new Identifier(MOD_ID, "swap");

    public static UUID HUMAN_DAMAGE_MODIFIER_ID = UUID.fromString("88d46329-20a2-42a8-8d7b-0b11fdcda31f");
    public static UUID HUMAN_DAMAGE_SPEED_MODIFIER_ID = UUID.fromString("5da6b87c-bd81-4068-b9a2-386adaf38762");
    public static UUID HUMAN_SPEED_MODIFIER_ID = UUID.fromString("bc5dd5c1-4594-4f2e-9d60-2e30f69d02e6");
    public static UUID HUMAN_HEALTH_ID = UUID.fromString("87a934eb-7c72-400e-9ae7-09220be58577");

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModBlocks.initialize();
        ModItems.initialize();

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
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((t)->{
            t.add(ModBlocks.HOLOGRAM_CONTROLLER);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((srv)->{
            server = srv;
        });
        ServerPlayConnectionEvents.DISCONNECT.register(((serverPlayNetworkHandler, minecraftServer) -> {
            for (FakestPlayer clone : StateSaverAndLoader.getPlayerState(serverPlayNetworkHandler.player).clones) {
                serverPlayNetworkHandler.player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(clone.getUuid())));
                clone.remove(Entity.RemovalReason.DISCARDED);
            }
        }));

        ArrayList<ServerPlayerEntity> bufferedKeys = new ArrayList<>();
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
        ServerTickEvents.START_WORLD_TICK.register((serverWorld -> {
            serverWorld.getPlayers().forEach((p)->{
                if (StateSaverAndLoader.getPlayerState(p).inHoloMode) {
                    p.getHungerManager().setFoodLevel(20);
                }
            });
            for (ServerPlayerEntity bufferPlayer : bufferedKeys) {
                if (bufferedKeys.contains(bufferPlayer)) {
                    FakestPlayer fakestPlayer = StateSaverAndLoader.getPlayerState(bufferPlayer).clones.get(0);
                    swapBody(bufferPlayer, fakestPlayer, true);
                    updateAttributesAndUpdateMode(bufferPlayer);
                }
            }
            bufferedKeys.clear();
        }));
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
        clone.setFireTicks(original.getFireTicks());
        clone.timeUntilRegen = 0;
        original.getActiveStatusEffects().forEach((statusEffect,statusEffectInstance)->{
            clone.addStatusEffect(statusEffectInstance);
        });
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
            builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(HUMAN_DAMAGE_MODIFIER_ID, "Holo modifier", (double) -100, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(HUMAN_DAMAGE_SPEED_MODIFIER_ID, "Holo modifier", (double) -0.8, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(HUMAN_SPEED_MODIFIER_ID, "Holo modifier", (double) -0.09, EntityAttributeModifier.Operation.ADDITION));
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

    private static void swapBody(PlayerEntity player, FakestPlayer bodyToTake, boolean bodyStays) {
        player.dismountVehicle();

        if (bodyStays)
            summonBody(player);

        if (bodyToTake != null) {
            StateSaverAndLoader.getPlayerState(player).clones.remove(bodyToTake);
            StateSaverAndLoader.getPlayerState(player).inHoloMode = bodyToTake.isHologram;
            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);

            player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(bodyToTake.getUuid())));
            bodyToTake.remove(Entity.RemovalReason.DISCARDED);
        }
    }
    private static void swapBody(PlayerEntity player, boolean holo, boolean bodyStays) {
        player.dismountVehicle();
        if (bodyStays)
            summonBody(player);

        FakestPlayer bodyToTake = null;
        for (FakestPlayer clone : StateSaverAndLoader.getPlayerState(player).clones) {
            if (clone.isHologram == holo)  { bodyToTake = clone; break; }
        }
        if (bodyToTake != null) {
            StateSaverAndLoader.getPlayerState(player).inHoloMode = holo;
            StateSaverAndLoader.getPlayerState(player).clones.remove(bodyToTake);
            tinyPlayerClone(bodyToTake, (ServerPlayerEntity) player);
            player.requestTeleport(bodyToTake.getPos().x, bodyToTake.getPos().y, bodyToTake.getPos().z);
            player.getWorld().getServer().getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(bodyToTake.getUuid())));
            bodyToTake.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private static void summonBody(PlayerEntity player) {
        summonBody(player, StateSaverAndLoader.getPlayerState(player).inHoloMode);
    }

    public static FakestPlayer summonNewBody(PlayerEntity player, boolean holoMode) {
        GameProfile profile = new GameProfile(Holo.getFreeUUID(), "");
        profile.getProperties().putAll(player.getGameProfile().getProperties());
        FakestPlayer fakePlayer = FakestPlayer.get((ServerWorld) player.getWorld(), profile, player.getEntityName(), player.getUuid());
        player.getWorld().getServer().getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(fakePlayer)));

        fakePlayer.setServerWorld((ServerWorld) player.getWorld());
        fakePlayer.refreshPositionAndAngles(player.getX(),player.getY(),player.getZ(),player.getYaw(),player.getPitch());

        ((ServerWorld) player.getWorld()).onPlayerConnected(fakePlayer);
        fakePlayer.isHologram = holoMode;
        if (fakePlayer.isHologram) {
            HoloModeUpdates.sendHoloModeUpdate(fakePlayer);
        }
        fakePlayer.worldName = player.getWorld().getRegistryKey();
        fakePlayer.savedPos = fakePlayer.getPos();
        StateSaverAndLoader.getPlayerState(player).clones.add(fakePlayer);
        StateSaverAndLoader.getPlayerState(player).playerName = player.getEntityName();
        player.getWorld().getServer().forcePlayerSampleUpdate();
        return fakePlayer;
    }

    public static void summonBody(PlayerEntity player, boolean holoMode) {
        FakestPlayer fakePlayer = summonNewBody(player,holoMode);
        Holo.tinyPlayerClone(player, fakePlayer);
    }

    public static void switchShellMode(PlayerEntity player, boolean shell, boolean bodyStays) {
        swapBody(player, !shell,bodyStays);
        updateAttributesAndUpdateMode(player);
    }


    public static void updateAttributesAndUpdateMode(PlayerEntity player) {
        if (StateSaverAndLoader.getPlayerState(player).inHoloMode) {
            getHoloAttributes(player).forEach((attributeEntry, modifier) -> {
                if (player.getAttributeInstance(attributeEntry) != null) {
                    if (!player.getAttributeInstance(attributeEntry).hasModifier(modifier)) {
                        player.getAttributeInstance(attributeEntry).addPersistentModifier(modifier);
                    }
                }
            });
            player.getAttributes().removeModifiers(getHumanAttributes(player));
            HoloModeUpdates.sendHoloModeUpdate(player);
        } else {
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
