package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

public record DoomOpportunityC2SPacket() implements CustomPayload {

    public static final Identifier DOOM_OPPORTUNITY_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "doom");
    public static final Id<DoomOpportunityC2SPacket> ID = new Id<>(DOOM_OPPORTUNITY_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, DoomOpportunityC2SPacket> CODEC;

    public DoomOpportunityC2SPacket() {

    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {

    }

    public static DoomOpportunityC2SPacket read(PacketByteBuf buf) {
        return new DoomOpportunityC2SPacket();
    }



    static {
        CODEC = PacketCodec.of(DoomOpportunityC2SPacket::write, DoomOpportunityC2SPacket::read);
    }
}