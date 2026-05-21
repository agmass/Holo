package org.agmas.holo.block.entity;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
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
    public BattleHologramComputerEntry.BattleHologramOptions hologramOptions = new BattleHologramComputerEntry.BattleHologramOptions();
    public PointLightData pointLightData = new PointLightData();
    public LightRenderHandle<PointLightData> lightHandle;
    public HologramControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public HologramControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.HOLOGRAM_CONTROLLER_BLOCK_ENTITY, pos, state);
    }


    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean("allowBuilding", hologramOptions.allowBuilding);
        nbt.putBoolean("alwaysPotted", hologramOptions.alwaysHaveBestPotions);
        nbt.putBoolean("saturation", hologramOptions.holoSaturation);
        nbt.putBoolean("enchantments", hologramOptions.noEnchantments);
        nbt.putInt("worldBorder", hologramOptions.worldBorderSize);
        super.writeNbt(nbt, registryLookup);
    }



    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        hologramOptions.allowBuilding = nbt.getBoolean("allowBuilding");
        hologramOptions.alwaysHaveBestPotions = nbt.getBoolean("alwaysPotted");
        hologramOptions.holoSaturation = nbt.getBoolean("saturation");
        hologramOptions.noEnchantments = nbt.getBoolean("enchantments");
        hologramOptions.worldBorderSize = nbt.getInt("worldBorder");
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world == null) return;

        BlockState state = getCachedState();
        world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
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
