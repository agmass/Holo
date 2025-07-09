package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

public record TerminalCommandC2SPacket(String command) implements CustomPayload {
    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "terminal_command");
    public static final CustomPayload.Id<TerminalCommandC2SPacket> ID = new CustomPayload.Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, TerminalCommandC2SPacket> CODEC;

    public TerminalCommandC2SPacket(String command) {
        this.command = command;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(command);
    }

    public static TerminalCommandC2SPacket read(PacketByteBuf buf) {

        return new TerminalCommandC2SPacket(buf.readString());
    }


    static {
        CODEC = PacketCodec.of(TerminalCommandC2SPacket::write, TerminalCommandC2SPacket::read);
    }
}