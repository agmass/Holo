package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class TagBlocksBattleMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At(value = "HEAD"))
    public void sendShellUpdate(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (HoloPlayerComponent.KEY.get(context.getPlayer()).hologramType.equals(HologramType.BATTLE_DUEL) && HoloPlayerComponent.KEY.get(context.getPlayer()).battleCanModify) {
            HoloPlayerComponent.KEY.get(context.getPlayer()).tagOriginalBlock(context.getWorld(),context.getBlockPos(),context.getWorld().getBlockState(context.getBlockPos()));
        }
    }
}
