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
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HologramType;

public class HologramSpawnEgg extends Item {

    public HologramType type;

    public HologramSpawnEgg(Settings settings, HologramType spawnType) {
        super(settings);
        type = spawnType;
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
            boolean nameAvailable = false;
            int loops = StateSaverAndLoader.getPlayerState(user).totalHolosCreated;
            while (!nameAvailable) {
                loops++;
                nameAvailable = true;
                for (FakestPlayer c : StateSaverAndLoader.getPlayerState(user).clones) {
                    if (c.holoName.equals("h" + loops)) {
                        nameAvailable = false;
                        break;
                    }
                }
            }
            FakestPlayer fp = Holo.summonNewBody(user, true, type,"h" + loops);
            StateSaverAndLoader.getPlayerState(user).totalHolosCreated++;
        }
        return super.use(world, user, hand);
    }
}
