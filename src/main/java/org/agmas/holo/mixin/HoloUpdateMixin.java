package org.agmas.holo.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.HoloModeUpdates;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public class HoloUpdateMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public void sendShellUpdate2(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        HoloModeUpdates.spawnHolosOnClient(player);
    }
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void sendShellUpdate(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        HoloModeUpdates.refreshHolosOnClient(player);
        Holo.updateAttributesAndUpdateMode(player);
        if (HoloNbtManager.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            Holo.swapBody(player,false,false);
            Holo.updateAttributesAndUpdateMode(player);
        }
        HoloNbtManager.getPlayerState(player).clones.forEach((clone)->{
            player.getServer().getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(clone)));
            ((ServerWorld) server.getWorld(clone.worldName)).onPlayerConnected(clone);
        });
    }
}
