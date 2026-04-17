package org.agmas.holo.block;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.ModItems;
import org.agmas.holo.block.entity.HologramControllerBlockEntity;
import org.agmas.holo.util.BattleHologramComputerEntry;

import java.util.ArrayList;
import java.util.List;

public class BattleHologramComputer extends HologramController{
    public static final IntProperty HOLOGRAM_COUNT = IntProperty.of("holograms", 2, 16);
    public BattleHologramComputer(Settings settings) {
        super(settings);
        setDefaultState(this.getDefaultState().with(HOLOGRAM_COUNT, 2));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{HologramController.FACING,HologramController.USING,POWER_UPGRADES,HOLOGRAM_COUNT});
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        List<ItemStack> stacks = super.getDroppedStacks(state, builder);
        if (state.get(HOLOGRAM_COUNT) > 2) {
            stacks.add(new ItemStack(ModItems.BATTLE_HOLOGRAM_SPAWN_EGG, state.get(HOLOGRAM_COUNT) - 2));
        }
        return stacks;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BattleHologramComputerEntry entryToRemove = null;
        for (BattleHologramComputerEntry entry : Holo.playersWaitingForBattle.keySet()) {
            if (entry.pos.equals(pos)) {
                entryToRemove = entry;
            }
        }
        if (entryToRemove != null) {
            Holo.playersWaitingForBattle.remove(entryToRemove);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return onUse(state,world,pos,player,hit).shouldSwingHand() ? ItemActionResult.CONSUME : ItemActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof HologramControllerBlockEntity hologramControllerBlockEntity) {
                for (ArrayList<PlayerEntity> playerEntities : Holo.fights) {
                    if (playerEntities.contains(player))
                        return ActionResult.FAIL;
                }
                if (player.getMainHandStack().isOf(ModItems.BATTLE_HOLOGRAM_SPAWN_EGG)) {
                    if (state.get(HOLOGRAM_COUNT) < 16) {
                        player.getMainHandStack().decrement(1);
                        player.sendMessage(Text.of("This machine now accepts " + (state.get(HOLOGRAM_COUNT) + 1) + " players."), true);
                        world.setBlockState(pos, state.with(HOLOGRAM_COUNT, state.get(HOLOGRAM_COUNT) + 1));
                    } else {
                        player.sendMessage(Text.of("You have reached maximum players!"), true);
                    }
                    return ActionResult.CONSUME;
                }
                BattleHologramComputerEntry oldComputer = null;
                for (BattleHologramComputerEntry computer : Holo.playersWaitingForBattle.keySet()) {
                    if (Holo.playersWaitingForBattle.get(computer).contains(player)) {
                        oldComputer = computer;
                    }
                }
                if (oldComputer == null) {
                    BattleHologramComputerEntry thisComputer = null;
                    for (BattleHologramComputerEntry computer : Holo.playersWaitingForBattle.keySet()) {
                        if (computer.pos.equals(pos) && computer.world.equals(world)) {
                            thisComputer = computer;
                            computer.count = state.get(HOLOGRAM_COUNT);
                            computer.hologramOptions = hologramControllerBlockEntity.hologramOptions;
                        }
                    }
                    if (thisComputer == null)
                        thisComputer = new BattleHologramComputerEntry(pos, world, hologramControllerBlockEntity.hologramOptions, state.get(HOLOGRAM_COUNT));
                    if (!Holo.playersWaitingForBattle.containsKey(thisComputer)) {
                        Holo.playersWaitingForBattle.put(thisComputer, new ArrayList<>());
                    }
                    if (Holo.playersWaitingForBattle.get(thisComputer).size() <= state.get(HOLOGRAM_COUNT) - 1) {
                        Holo.playersWaitingForBattle.get(thisComputer).add(player);
                        world.setBlockState(pos, state.with(USING, true));
                        player.sendMessage(Text.literal("Joined Arena!").formatted(Formatting.GREEN), true);
                        if (state.get(HOLOGRAM_COUNT) > 2) {
                            player.sendMessage(Text.literal("To start a match early, Shift+Click the computer while queued.").formatted(Formatting.BLUE));
                        }
                    }
                } else {
                    if (player.isSneaking()) {
                        oldComputer.start = true;
                        return ActionResult.SUCCESS;
                    }
                    Holo.playersWaitingForBattle.get(oldComputer).remove(player);
                    if (Holo.playersWaitingForBattle.get(oldComputer).isEmpty()) {
                        world.setBlockState(pos, state.with(USING, false));
                    }
                    player.sendMessage(Text.literal("Left Arena").formatted(Formatting.RED), true);
                }
            }
        }
        if (world.isClient) {
            if (!state.get(USING) && player.isSneaking()) {
                //editing = pos;
                //editingState = state;
                //openEditScreen = true;
            }
        }

        return ActionResult.SUCCESS;

    }

    public static boolean openEditScreen = false;
    public static BlockPos editing = new BlockPos(0,0,0);
    public static BlockState editingState = null;
}
