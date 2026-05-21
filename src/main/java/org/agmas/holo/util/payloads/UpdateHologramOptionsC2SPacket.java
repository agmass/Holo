package org.agmas.holo.util.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.agmas.holo.Holo;
import org.agmas.holo.util.BattleHologramComputerEntry;

import java.util.UUID;

public record UpdateHologramOptionsC2SPacket(BlockPos duelComputer, BattleHologramComputerEntry.BattleHologramOptions battleHologramOptions) implements CustomPayload {

    public static final Identifier UPDATE_DUEL_COMPUTER_PAYLOAD_ID = Identifier.of(Holo.MOD_ID, "update_duel_computer");
    public static final Id<UpdateHologramOptionsC2SPacket> ID = new Id<>(UPDATE_DUEL_COMPUTER_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, UpdateHologramOptionsC2SPacket> CODEC;

    public UpdateHologramOptionsC2SPacket(BlockPos duelComputer, BattleHologramComputerEntry.BattleHologramOptions battleHologramOptions) {
        this.duelComputer = duelComputer;
        this.battleHologramOptions = battleHologramOptions;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(duelComputer);
        buf.writeInt(battleHologramOptions.worldBorderSize);
        buf.writeBoolean(battleHologramOptions.noEnchantments);
        buf.writeBoolean(battleHologramOptions.alwaysHaveBestPotions);
        buf.writeBoolean(battleHologramOptions.allowBuilding);
        buf.writeBoolean(battleHologramOptions.holoSaturation);
    }

    public static UpdateHologramOptionsC2SPacket read(PacketByteBuf buf) {
        BlockPos duelComputer = buf.readBlockPos();
        BattleHologramComputerEntry.BattleHologramOptions options = new BattleHologramComputerEntry.BattleHologramOptions();
        options.worldBorderSize = buf.readInt();
        options.noEnchantments = buf.readBoolean();
        options.alwaysHaveBestPotions = buf.readBoolean();
        options.allowBuilding = buf.readBoolean();
        options.holoSaturation = buf.readBoolean();
        return new UpdateHologramOptionsC2SPacket(duelComputer, options);
    }

    @Override
    public BlockPos duelComputer() {
        return duelComputer;
    }

    @Override
    public BattleHologramComputerEntry.BattleHologramOptions battleHologramOptions() {
        return battleHologramOptions;
    }

    static {
        CODEC = PacketCodec.of(UpdateHologramOptionsC2SPacket::write, UpdateHologramOptionsC2SPacket::read);
    }
}