package org.agmas.holo.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import org.agmas.holo.ModEntities;
import org.agmas.holo.block.entity.HologramControllerBlockEntity;
import org.agmas.holo.state.HoloPlayerComponent;
import org.jetbrains.annotations.Nullable;

public class HologramController extends BlockWithEntity {
    public static final DirectionProperty FACING;
    public static final BooleanProperty USING = BooleanProperty.of("using");
    protected static final VoxelShape SHAPE;
    public static final IntProperty POWER_UPGRADES = IntProperty.of("power_upgrades", 0, 16);

    public HologramController(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(POWER_UPGRADES, 0).with(USING, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(HologramController::new);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING,USING,POWER_UPGRADES});
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModEntities.HOLOGRAM_CONTROLLER_BLOCK_ENTITY, HologramControllerBlockEntity::tick);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    static {
        SHAPE = VoxelShapes.union(Block.createCuboidShape(1.0, 0, 1.0, 15.0, 13.0, 15));
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
            if (state.get(USING)) player.sendMessage(Text.literal("This computer is already in use.").formatted(Formatting.RED), true);
            if (Holo.canSwapBody(player,true)) {
                HoloPlayerComponent.KEY.get(player).lastComputerMaxPower = state.get(POWER_UPGRADES);
                HoloPlayerComponent.KEY.get(player).computerPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                world.setBlockState(pos, state.with(USING, true));
                Holo.switchShellMode(player, false, true);
            } else {
                player.sendMessage(Text.literal("No holograms available.").formatted(Formatting.RED), true);
            }
        }

        return ActionResult.CONSUME;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }



    static {
        FACING = HorizontalFacingBlock.FACING;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HologramControllerBlockEntity(pos,state);
    }
}
