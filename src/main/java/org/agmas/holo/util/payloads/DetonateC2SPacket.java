package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

import java.util.UUID;

public record DetonateC2SPacket() implements CustomPayload {

    public static final Identifier DETONATE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "detonate");
    public static final Id<DetonateC2SPacket> ID = new Id<>(DETONATE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, DetonateC2SPacket> CODEC;

    public DetonateC2SPacket() {

    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {

    }

    public static DetonateC2SPacket read(PacketByteBuf buf) {
        return new DetonateC2SPacket();
    }



    static {
        CODEC = PacketCodec.of(DetonateC2SPacket::write, DetonateC2SPacket::read);
    }
}