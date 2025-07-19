package org.agmas.holo.block;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;

public class HologramController extends Block {
    public static final DirectionProperty FACING;
    protected static final VoxelShape SHAPE;
    protected static final VoxelShape SHAPE_SOUTH;
    protected static final VoxelShape SHAPE_WEST;
    protected static final VoxelShape SHAPE_EAST;
    public static final IntProperty POWER_UPGRADES = IntProperty.of("power_upgrades", 0, 16);

    public HologramController(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(POWER_UPGRADES, 0));
    }
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING,POWER_UPGRADES});
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE;
        };
    }

    static {
        SHAPE = VoxelShapes.union(Block.createCuboidShape(2.0, 1.5, 3.0, 14.0, 12.5, 14));
        SHAPE_SOUTH = VoxelShapes.union(Block.createCuboidShape(2.0, 1.5, 2.0, 14.0, 12.5, 13));

        SHAPE_WEST = VoxelShapes.union(Block.createCuboidShape(3.0, 1.5, 2.0, 14.0, 12.5, 14));
        SHAPE_EAST= VoxelShapes.union(Block.createCuboidShape(2.0, 1.5, 2.0, 13.0, 12.5, 14));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.NETHERITE_INGOT) && state.get(POWER_UPGRADES) < 16) {
            stack.decrementUnlessCreative(1,player);
            world.setBlockState(pos,state.with(POWER_UPGRADES,state.get(POWER_UPGRADES)+1));
            world.playSound(pos.getX(),pos.getY(),pos.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS,1f,1f,true);
            return ItemActionResult.CONSUME;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {

        if (!world.isClient) {
            if (Holo.canSwapBody(player,true)) {
                HoloNbtManager.getPlayerState(player).activeModifiers.clear();
                HoloNbtManager.getPlayerState(player).lastComputerMaxPower = state.get(POWER_UPGRADES);
                Holo.switchShellMode(player, false, true);
            } else {
                player.sendMessage(Text.literal("No holograms available.").formatted(Formatting.RED), true);
            }
        }

        return super.onUse(state, world, pos, player, hit);
    }


    static {
        FACING = HorizontalFacingBlock.FACING;
    }
}
