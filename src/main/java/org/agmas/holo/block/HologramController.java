package org.agmas.holo.block;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.FakestPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramController extends Block {
    public static final DirectionProperty FACING;
    protected static final VoxelShape SHAPE;

    public HologramController(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    static {
        SHAPE = VoxelShapes.union(Block.createCuboidShape(2.0, 1.5, 3.0, 14.0, 12.5, 14));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!world.isClient && hand.equals(Hand.MAIN_HAND)) {
            if (Holo.canSwapBody(player,true)) {
                Holo.switchShellMode(player, false, true);
            } else {
                player.sendMessage(Text.literal("No holograms available.").formatted(Formatting.RED), true);
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }


    static {
        FACING = HorizontalFacingBlock.FACING;
    }
}
