package org.agmas.holo.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BattleHologramComputerEntry {
    public BlockPos pos;
    public World world;
    public boolean infinite;
    public int count = 2;
    public boolean start = false;


    public BattleHologramComputerEntry(BlockPos pos, World world, Boolean infinite, int count) {
        this.pos = pos;
        this.world = world;
        this.infinite = infinite;
        this.count = count;
    }
}
