package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.Holo;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.FakestPlayer;

import java.util.ArrayList;

public class SwapCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        StateSaverAndLoader.getPlayerState(player).clones.forEach((p)->{
            str.add("swap " + p.holoName);
        });
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        if (cmd.split(" ").length < 2) {
            return Text.literal("Insufficient arguments supplied").formatted(Formatting.RED);
        }
        String referencedHolo = cmd.split(" ")[1];
        for (FakestPlayer c : StateSaverAndLoader.getPlayerState(player).clones) {
            if (c.holoName.equals(referencedHolo)) {
                Holo.swapBody(player,c,true);
                Holo.updateAttributesAndUpdateMode(player);
                return Text.literal("Swapped to " + c.holoName + "!").formatted(Formatting.GREEN);
            }
        }
        return Text.literal("No appropriate Holo found.").formatted(Formatting.RED);
    }
}
