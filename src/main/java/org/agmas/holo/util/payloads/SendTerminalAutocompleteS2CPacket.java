package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

import java.util.ArrayList;

public record SendTerminalAutocompleteS2CPacket(ArrayList<String> commands) implements CustomPayload {

    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "send_terminal_autocomplete");
    public static final CustomPayload.Id<SendTerminalAutocompleteS2CPacket> ID = new CustomPayload.Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SendTerminalAutocompleteS2CPacket> CODEC;

    public SendTerminalAutocompleteS2CPacket(ArrayList<String> commands) {
        this.commands = commands;
    }

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        for (String command : commands) {
            buf.writeString(command);
        }
    }

    public static SendTerminalAutocompleteS2CPacket read(PacketByteBuf buf) {
        ArrayList<String> suggestions = new ArrayList<>();
        boolean complete = false;
        while (!complete) {
            try {
                String s = buf.readString();
                if (s == null) {
                    complete = true;
                    break;
                }
                suggestions.add(s);
            } catch (Exception e) {
                complete = true;
            }
        }
        return new SendTerminalAutocompleteS2CPacket(suggestions);
    }

    public ArrayList<String> commands() {
        return this.commands;
    }


    static {
        CODEC = PacketCodec.of(SendTerminalAutocompleteS2CPacket::write, SendTerminalAutocompleteS2CPacket::read);
    }
}