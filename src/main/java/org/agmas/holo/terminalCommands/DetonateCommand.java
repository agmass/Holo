package org.agmas.holo.terminalCommands;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.agmas.holo.state.ClonePlayerComponent;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;

import java.util.ArrayList;

public class DetonateCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        if (HoloPlayerComponent.KEY.get(player).inHoloMode)
            str.add("detonate");
        ClonePlayerComponent.KEY.get(player).clones.forEach((p)->{
            if (p.isHologram)
                str.add("detonate " + p.holoName);
        });
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        PlayerEntity holoToDetonate = null;
        if (cmd.split(" ").length == 1) {
            if (HoloPlayerComponent.KEY.get(player).inHoloMode) {
                holoToDetonate = player;
            } else {
                return Text.literal("Your flesh is not equipped with a bomb.").formatted(Formatting.RED);
            }
        } else {
            String referencedHolo = cmd.split(" ")[1];
            if (referencedHolo.equals(HoloPlayerComponent.KEY.get(player).holoName)) {
                if (HoloPlayerComponent.KEY.get(player).inHoloMode) {
                    holoToDetonate = player;
                } else {
                    return Text.literal("Your flesh is not equipped with a bomb.").formatted(Formatting.RED);
                }
            }

            for (FakestPlayer c : ClonePlayerComponent.KEY.get(player).clones) {
                if (c.holoName.equals(referencedHolo)) {
                    if (c.isHologram) {
                        holoToDetonate = c;
                    } else {
                        return Text.literal("Your flesh is not equipped with a bomb.").formatted(Formatting.RED);
                    }
                }
            }
        }
        if (holoToDetonate != null) {
            holoToDetonate.getWorld().createExplosion(holoToDetonate,holoToDetonate.getX(),holoToDetonate.getY(),holoToDetonate.getZ(),4, World.ExplosionSourceType.TNT);
            holoToDetonate.kill();
            return Text.literal("Holo detonated.").formatted(Formatting.GREEN);
        }
        return Text.literal("No appropriate Holo found.").formatted(Formatting.RED);
    }
}
