package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

import java.util.ArrayList;

public record RequestTerminalAutocompleteC2SPacket() implements CustomPayload {
    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "request_terminal_autocomplete");
    public static final CustomPayload.Id<RequestTerminalAutocompleteC2SPacket> ID = new CustomPayload.Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, RequestTerminalAutocompleteC2SPacket> CODEC;

    public RequestTerminalAutocompleteC2SPacket() {
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
    }

    public static RequestTerminalAutocompleteC2SPacket read(PacketByteBuf buf) {

        return new RequestTerminalAutocompleteC2SPacket();
    }


    static {
        CODEC = PacketCodec.of(RequestTerminalAutocompleteC2SPacket::write, RequestTerminalAutocompleteC2SPacket::read);
    }
}