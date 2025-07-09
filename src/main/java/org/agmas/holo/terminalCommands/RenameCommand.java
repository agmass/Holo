package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.FakestPlayer;

import java.util.ArrayList;

public class RenameCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        HoloNbtManager.getPlayerState(player).clones.forEach((p)->{
            if (p.isHologram)
                str.add("rename " + p.holoName + " <name>");
        });
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        if (cmd.split(" ").length < 3) {
            return Text.literal("Insufficient arguments supplied").formatted(Formatting.RED);
        }
        String referencedHolo = cmd.split(" ")[1];
        if (referencedHolo.equals(HoloNbtManager.getPlayerState(player).holoName)) {
            return Text.literal("You cannot rename a holo while inside of it.").formatted(Formatting.RED);
        }
        for (FakestPlayer c : HoloNbtManager.getPlayerState(player).clones) {
            if (c.holoName.equals(cmd.split(" ")[2])) {
                return Text.literal("A holo already has this ID!").formatted(Formatting.RED);
            }
        }
        for (FakestPlayer c : HoloNbtManager.getPlayerState(player).clones) {
            if (c.holoName.equals(referencedHolo)) {
                if (!c.isHologram) {
                    return Text.literal("You cannot rename your mortal coil.").formatted(Formatting.RED);
                }
                String oldName = c.holoName;
                c.holoName = cmd.split(" ")[2];
                return Text.literal("Renamed " + oldName + "to "+ c.holoName +"!").formatted(Formatting.GREEN);
            }
        }
        return Text.literal("No appropriate Holo found.").formatted(Formatting.RED);
    }
}
