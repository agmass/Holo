package org.agmas.holo.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.payloads.HoloModeSwitchS2CPacket;

import java.util.List;

public class HoloModeUpdates {


    public static void sendHumanModeUpdate(PlayerEntity player) {

        MinecraftServer server = player.getServer();

        PacketByteBuf data = PacketByteBufs.create();
        HoloNbtManager.getPlayerState(player).inHoloMode = false;
        data.writeUuid(player.getUuid());

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                ServerPlayNetworking.send(p, new HoloModeSwitchS2CPacket(player.getUuid(), HologramType.HUMAN));
            });
        });
    }

    public static void spawnHolosOnClient(ServerPlayerEntity player) {

        MinecraftServer server = player.getServer();

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                HoloNbtManager.getPlayerState(p).clones.forEach((pl)->{
                    player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(List.of(pl)));
                });
            });
        });
    }

    public static void refreshHolosOnClient(ServerPlayerEntity player) {

        MinecraftServer server = player.getServer();

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                HoloNbtManager.getPlayerState(p).clones.forEach((pl)->{
                    if (pl.isHologram) {
                        HoloModeUpdates.sendHoloModeUpdate(pl);
                    }
                });
                if (HoloNbtManager.getPlayerState(p).inHoloMode) {
                    ServerPlayNetworking.send(player, new HoloModeSwitchS2CPacket(player.getUuid(), HoloNbtManager.getPlayerState(p).hologramType));
                }
            });
        });
    }


    public static void sendHoloModeUpdate(PlayerEntity player) {

        MinecraftServer server = player.getServer();

        HoloNbtManager.getPlayerState(player).inHoloMode = true;
        HologramType type;
        if (player instanceof FakestPlayer fp) {
            type = fp.type;
        } else {
            type = HoloNbtManager.getPlayerState(player).hologramType;
        }

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                ServerPlayNetworking.send(p, new HoloModeSwitchS2CPacket(player.getUuid(), type));

            });
        });
    }
}
