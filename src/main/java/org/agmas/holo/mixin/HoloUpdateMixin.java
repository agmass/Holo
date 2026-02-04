package org.agmas.holo.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.holo.Holo;
import org.agmas.holo.state.ClonePlayerComponent;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModeUpdates;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerManager.class)
public abstract class HoloUpdateMixin {
    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow public abstract void remove(ServerPlayerEntity player);

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z", ordinal = 1))
    public void sendShellUpdate2(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        HoloModeUpdates.spawnHolosOnClient(player);
    }
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void sendShellUpdate(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        HoloModeUpdates.refreshHolosOnClient(player);
        Holo.updateAttributesAndUpdateMode(player);
        ArrayList<FakestPlayer> newClones = new ArrayList<>();
        ClonePlayerComponent.KEY.get(player).clones.removeIf((clone)->{
            FakestPlayer p = Holo.summonNewBody(clone, clone.isHologram, clone.type, clone.holoName);
            p.copyFrom(clone,true);
            if (!clone.isRemoved()) {
                remove(clone);
            }
            p.ownerUUID = player.getUuid();
            p.ownerName = player.getNameForScoreboard();
            newClones.add(p);
            return true;
        });
        ClonePlayerComponent.KEY.get(player).clones.addAll(newClones);
        if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL)) {
            Holo.swapBody(player,false,false);
            Holo.updateAttributesAndUpdateMode(player);
        }
    }
}
