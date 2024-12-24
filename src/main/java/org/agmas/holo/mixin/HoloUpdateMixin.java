package org.agmas.holo.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.agmas.holo.Holo;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModeUpdates;
import org.agmas.holo.util.HologramType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.plaf.nimbus.State;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(PlayerManager.class)
public class HoloUpdateMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public void sendShellUpdate2(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        HoloModeUpdates.spawnHolosOnClient(player);
    }
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void sendShellUpdate(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        HoloModeUpdates.refreshHolosOnClient(player);
        Holo.updateAttributesAndUpdateMode(player);
        if (StateSaverAndLoader.getPlayerState(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            Holo.swapBody(player,false,false);
            Holo.updateAttributesAndUpdateMode(player);
        }
        StateSaverAndLoader.getPlayerState(player).clones.forEach((clone)->{
            player.getServer().getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(clone)));
            ((ServerWorld) server.getWorld(clone.worldName)).onPlayerConnected(clone);
        });
    }
}
