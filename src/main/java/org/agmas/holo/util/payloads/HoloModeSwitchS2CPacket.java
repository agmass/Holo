package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;
import org.agmas.holo.util.HologramType;

import java.util.UUID;

public record HoloModeSwitchS2CPacket(UUID player, HologramType hologramType) implements CustomPayload {
    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "holo_mode");
    public static final CustomPayload.Id<HoloModeSwitchS2CPacket> ID = new CustomPayload.Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, HoloModeSwitchS2CPacket> CODEC;

    public HoloModeSwitchS2CPacket(UUID player, HologramType hologramType) {
        this.player = player;
        this.hologramType = hologramType;
    }

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.player);
        buf.writeEnumConstant(this.hologramType);
    }

    public static HoloModeSwitchS2CPacket read(PacketByteBuf buf) {
        return new HoloModeSwitchS2CPacket(buf.readUuid(), buf.readEnumConstant(HologramType.class));
    }

    public HologramType hologramType() {
        return this.hologramType;
    }

    public UUID player() {
        return this.player;
    }


    static {
        CODEC = PacketCodec.of(HoloModeSwitchS2CPacket::write, HoloModeSwitchS2CPacket::read);
    }
}