package org.agmas.holo.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BattleHologramComputerEntry {
    public BlockPos pos;
    public World world;
    public int count = 2;
    public BattleHologramOptions hologramOptions;
    public boolean start = false;


    public BattleHologramComputerEntry(BlockPos pos, World world, BattleHologramOptions hologramOptions, int count) {
        this.pos = pos;
        this.world = world;
        this.hologramOptions = hologramOptions;
        this.count = count;
    }

    public static class BattleHologramOptions {
        public int worldBorderSize = 128;
        public boolean noEnchantments = false;
        public boolean alwaysHaveBestPotions = false;
        public boolean holoSaturation = true;

    }
}
