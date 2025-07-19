package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.Holo;
import org.agmas.holo.state.HoloNbtManager;

import java.util.ArrayList;

public class PowerInfoCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        if (HoloNbtManager.getPlayerState(player).inHoloMode)
            str.add("power");
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        return Text.literal("Computers can have their power upgraded with ").formatted(Formatting.WHITE).append(Text.literal("Netherite Ingots.\n").formatted(Formatting.YELLOW).append(Text.literal("If you overcharge on power, you will recieve de-buffs and start lagging.").formatted(Formatting.RED).append("\nBeing at low health or being in water can use up extra power. Be careful!").formatted(Formatting.DARK_RED)));
    }
}
