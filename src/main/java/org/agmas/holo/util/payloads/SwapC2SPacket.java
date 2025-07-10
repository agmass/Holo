package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

public record SwapC2SPacket() implements CustomPayload {

    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "swap");
    public static final CustomPayload.Id<SwapC2SPacket> ID = new CustomPayload.Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SwapC2SPacket> CODEC;

    public SwapC2SPacket() {
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
    }

    public static SwapC2SPacket read(PacketByteBuf buf) {

        return new SwapC2SPacket();
    }


    static {
        CODEC = PacketCodec.of(SwapC2SPacket::write, SwapC2SPacket::read);
    }
}