package org.agmas.holo.block.entity;

import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.ModBlocks;
import org.agmas.holo.ModEntities;
import org.agmas.holo.block.HologramController;
import org.agmas.holo.util.BattleHologramComputerEntry;
import org.jetbrains.annotations.Nullable;

public class HologramControllerBlockEntity extends BlockEntity {
    public int age = 0;
    public boolean isBattle = false;
    public BattleHologramComputerEntry.BattleHologramOptions hologramOptions;
    public PointLightData pointLightData = new PointLightData();
    public LightRenderHandle<PointLightData> lightHandle;
    public HologramControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public HologramControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.HOLOGRAM_CONTROLLER_BLOCK_ENTITY, pos, state);
    }



    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markRemoved() {
        if (lightHandle != null)
            lightHandle.free();
        super.markRemoved();
    }


    public static void tick(World world, BlockPos pos, BlockState state, HologramControllerBlockEntity blockEntity) {
        blockEntity.isBattle = state.isOf(ModBlocks.BATTLE_HOLOGRAM_CONTROLLER);
        if (!state.get(HologramController.USING)) {
            blockEntity.age = -1;
        } else {
            blockEntity.age++;
            if (blockEntity.age > 300000) blockEntity.age = 0;
        }
    }
}
