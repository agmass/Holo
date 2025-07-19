package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModifiers;

import java.util.ArrayList;

public class EnableCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        for (HoloModifiers value : HoloModifiers.values()) {
            if (HoloNbtManager.getPlayerState(player).loreAccurate && !Holo.loreAllowedModifiers.contains(value)) continue;
            if (HoloNbtManager.getPlayerState(player).activeModifiers.contains(value)) continue;
            str.add("enable " + value.toString());
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
                if (!HoloNbtManager.getPlayerState(player).activeModifiers.contains(value)) HoloNbtManager.getPlayerState(player).activeModifiers.add(value);
                Holo.updateAttributesForModifiers(player,true);
                return Text.literal("Enabled modifier " + value.toString() + " for ").formatted(Formatting.GREEN).append(Text.literal(Holo.modifierToPower.get(value)+" power.").formatted(Formatting.RED));
            }
        }
        return Text.literal("No appropriate modifier specified.").formatted(Formatting.RED);
    }
}
