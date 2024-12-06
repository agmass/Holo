package org.agmas.holo.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.agmas.holo.Holo;
import org.agmas.holo.state.StateSaverAndLoader;

public class HologramSpawnEgg extends Item {
    public HologramSpawnEgg(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            if (!StateSaverAndLoader.getPlayerState(user).loreAccurate) {
                if (StateSaverAndLoader.getPlayerState(user).clones.size() >= 4) {
                    user.sendMessage(Text.literal("Too many clones!").formatted(Formatting.RED));
                    return super.use(world, user, hand);
                }
            }
            ItemStack itemStack = user.getStackInHand(hand);
            if (!user.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            Holo.summonNewBody(user, true);
        }
        return super.use(world, user, hand);
    }
}
