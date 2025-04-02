package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.Holo;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.FakestPlayer;

import java.util.ArrayList;

public class ExitCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        if (StateSaverAndLoader.getPlayerState(player).inHoloMode)
            str.add("exit");
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        if (Holo.canSwapBody(player,false)) {
            Holo.swapBody(player,false,true);
            Holo.updateAttributesAndUpdateMode(player);
        }
        return Text.literal("No appropriate Human Form found.").formatted(Formatting.RED);
    }
}
