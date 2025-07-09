package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

import java.util.UUID;

public record TemporarilyShowEntityS2CPacket(UUID entity) implements CustomPayload {
    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "temporarily_show_entity");
    public static final CustomPayload.Id<TemporarilyShowEntityS2CPacket> ID = new CustomPayload.Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, TemporarilyShowEntityS2CPacket> CODEC;

    public TemporarilyShowEntityS2CPacket(UUID entity) {
        this.entity = entity;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(entity);
    }

    public static TemporarilyShowEntityS2CPacket read(PacketByteBuf buf) {

        return new TemporarilyShowEntityS2CPacket(buf.readUuid());
    }


    static {
        CODEC = PacketCodec.of(TemporarilyShowEntityS2CPacket::write, TemporarilyShowEntityS2CPacket::read);
    }
}