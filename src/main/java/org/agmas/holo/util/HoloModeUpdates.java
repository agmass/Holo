package org.agmas.holo.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.agmas.holo.Holo;
import org.agmas.holo.state.StateSaverAndLoader;

import java.util.List;

public class HoloModeUpdates {


    public static void sendHumanModeUpdate(PlayerEntity player) {

        MinecraftServer server = player.getServer();

        PacketByteBuf data = PacketByteBufs.create();
        StateSaverAndLoader.getPlayerState(player).inHoloMode = false;
        data.writeUuid(player.getUuid());

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                ServerPlayNetworking.send(p, Holo.HUMAN_MODE, data);
            });
        });
    }

    public static void spawnHolosOnClient(ServerPlayerEntity player) {

        MinecraftServer server = player.getServer();

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                StateSaverAndLoader.getPlayerState(p).clones.forEach((pl)->{
                    player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(List.of(pl)));
                });
            });
        });
    }

    public static void refreshHolosOnClient(ServerPlayerEntity player) {

        MinecraftServer server = player.getServer();

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                StateSaverAndLoader.getPlayerState(p).clones.forEach((pl)->{
                    if (pl.isHologram) {
                        HoloModeUpdates.sendHoloModeUpdate(pl);
                    }
                });
                if (StateSaverAndLoader.getPlayerState(p).inHoloMode) {
                    PacketByteBuf data = PacketByteBufs.create();
                    data.writeUuid(p.getUuid());
                    ServerPlayNetworking.send(player, Holo.HOLO_MODE, data);
                }
            });
        });
    }


    public static void sendHoloModeUpdate(PlayerEntity player) {

        MinecraftServer server = player.getServer();

        PacketByteBuf data = PacketByteBufs.create();
        StateSaverAndLoader.getPlayerState(player).inHoloMode = true;
            data.writeUuid(player.getUuid());

        server.execute(() -> {
            server.getPlayerManager().getPlayerList().forEach((p)->{
                ServerPlayNetworking.send(p, Holo.HOLO_MODE, data);
            });
        });
    }
}
