package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

import java.util.ArrayList;

public record HoloStatusInfoS2CPacket(String holoName, int hostHealth, int power, int maxPower, int playersAliveInDuel) implements CustomPayload {

    public static final Identifier HOLO_MODE_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "holo_status_info");
    public static final Id<HoloStatusInfoS2CPacket> ID = new Id<>(HOLO_MODE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, HoloStatusInfoS2CPacket> CODEC;

    public HoloStatusInfoS2CPacket(String holoName, int hostHealth, int power, int maxPower, int playersAliveInDuel) {
        this.holoName = holoName;
        this.hostHealth = hostHealth;
        this.power = power;
        this.maxPower = maxPower;
        this.playersAliveInDuel = playersAliveInDuel;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(holoName);
        buf.writeInt(hostHealth);
        buf.writeInt(power);
        buf.writeInt(maxPower);
        buf.writeInt(playersAliveInDuel);
    }

    public static HoloStatusInfoS2CPacket read(PacketByteBuf buf) {
        return new HoloStatusInfoS2CPacket(buf.readString(), buf.readInt(), buf.readInt(),buf.readInt(),buf.readInt());
    }

    @Override
    public String holoName() {
        return holoName;
    }

    @Override
    public int hostHealth() {
        return hostHealth;
    }

    @Override
    public int power() {
        return power;
    }

    @Override
    public int maxPower() {
        return maxPower;
    }

    @Override
    public int playersAliveInDuel() {
        return playersAliveInDuel;
    }

    static {
        CODEC = PacketCodec.of(HoloStatusInfoS2CPacket::write, HoloStatusInfoS2CPacket::read);
    }
}