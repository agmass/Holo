package org.agmas.holo.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.HologramType;
import org.agmas.holo.util.payloads.TemporarilyShowEntityS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class SilentHologramListenerMixin {
    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow public abstract <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed);

    @Inject(method = "emitGameEvent", at = @At("HEAD"))
    public void sendShellUpdate(RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter, CallbackInfo ci) {
        getPlayers().forEach((p)->{
            if (HoloNbtManager.getPlayerState(p).hologramType.equals(HologramType.SILENT) && emitterPos.isInRange(p.getEyePos(), 30)) {
                if (emitter.sourceEntity() != p) {
                    int d = (int) (1.0 - (20 / emitterPos.distanceTo(p.getEyePos())));
                    double e = MathHelper.lerp(d, emitterPos.x, p.getEyePos().x);
                    double f = MathHelper.lerp(d, emitterPos.y, p.getEyePos().y);
                    double g = MathHelper.lerp(d, emitterPos.z, p.getEyePos().z);
                    if (emitter.sourceEntity() != null) {
                        if (emitter.sourceEntity() instanceof PlayerEntity) {
                            spawnParticles(new VibrationParticleEffect(new EntityPositionSource(emitter.sourceEntity(),(float)emitter.sourceEntity().getStandingEyeHeight()), 20), e, f, g, 1, 0.0, 0, 0.0, 0.0);

                        }
                        MinecraftServer server = p.getServer();


                        server.execute(() -> {
                            ServerPlayNetworking.send(p, new TemporarilyShowEntityS2CPacket(emitter.sourceEntity().getUuid()));
                        });
                    }
                }
            }
        });
    }
}
