package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

import java.util.ArrayList;
import java.util.UUID;

public record SendCallC2SPacket(UUID target) implements CustomPayload {

    public static final Identifier CALL_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "send_call");
    public static final Id<SendCallC2SPacket> ID = new Id<>(CALL_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SendCallC2SPacket> CODEC;

    public SendCallC2SPacket(UUID target) {
        this.target = target;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(target);
    }

    public static SendCallC2SPacket read(PacketByteBuf buf) {
        return new SendCallC2SPacket(buf.readUuid());
    }

    public UUID target() {
        return this.target;
    }


    static {
        CODEC = PacketCodec.of(SendCallC2SPacket::write, SendCallC2SPacket::read);
    }
}