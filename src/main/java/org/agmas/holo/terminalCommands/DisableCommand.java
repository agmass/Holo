package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.HoloModifiers;

import java.util.ArrayList;

public class DisableCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        for (HoloModifiers value : HoloModifiers.values()) {
            if (HoloNbtManager.getPlayerState(player).loreAccurate && !Holo.loreAllowedModifiers.contains(value)) continue;
            if (!HoloNbtManager.getPlayerState(player).activeModifiers.contains(value)) continue;
            str.add("disable " + value.toString());
        }
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        if (cmd.split(" ").length < 2) {
            return Text.literal("Insufficient arguments supplied").formatted(Formatting.RED);
        }
        String referencedHoloMod = cmd.split(" ")[1].toLowerCase();
        for (HoloModifiers value : HoloModifiers.values()) {
            if (HoloNbtManager.getPlayerState(player).loreAccurate && !Holo.loreAllowedModifiers.contains(value)) continue;
            if (referencedHoloMod.equals(value.toString().toLowerCase())) {
                HoloNbtManager.getPlayerState(player).activeModifiers.remove(value);
                Holo.updateAttributesForModifiers(player,true);
                return Text.literal("Disabled modifier " + value.toString()).formatted(Formatting.GREEN);
            }
        }
        return Text.literal("No appropriate modifier specified.").formatted(Formatting.RED);
    }
}
