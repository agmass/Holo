package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public class TagBreakingBattleMixin {
    @WrapMethod(method = "breakBlock")
    public boolean sendShellUpdate(BlockPos pos, boolean drop, Entity breakingEntity, int maxUpdateDepth, Operation<Boolean> original) {
        if (breakingEntity != null) {
            for (PlayerEntity player : breakingEntity.getWorld().getPlayers()) {
                if (HoloPlayerComponent.KEY.get(player).modifiedBlocks.containsKey(pos)) {
                    return false;
                }
            }
        }
        return original.call(pos,drop,breakingEntity,maxUpdateDepth);
    }
}
