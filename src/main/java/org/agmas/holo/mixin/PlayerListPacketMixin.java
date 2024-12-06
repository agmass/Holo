package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.agmas.holo.util.FakestPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerListS2CPacket.Entry.class)
public class PlayerListPacketMixin {
    @Shadow @Final private boolean listed;

    @ModifyArgs(method = "<init>(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Entry;<init>(Ljava/util/UUID;Lcom/mojang/authlib/GameProfile;ZILnet/minecraft/world/GameMode;Lnet/minecraft/text/Text;Lnet/minecraft/network/encryption/PublicPlayerSession$Serialized;)V"))
    private static void changeEntry(Args args, @Local(argsOnly = true) ServerPlayerEntity spe) {
        if (spe instanceof FakestPlayer fp) {
            args.set(2, false);
            if (spe.getServer().isDedicated()) {
                GameProfile profile = spe.getServer().getPlayerManager().getPlayer(fp.ownerUUID).getGameProfile();

                args.set(1, profile);
            }
        }
    }
}
