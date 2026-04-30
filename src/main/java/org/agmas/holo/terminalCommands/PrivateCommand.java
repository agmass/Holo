package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.state.ClonePlayerComponent;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.FakestPlayer;
import org.agmas.holo.util.HoloModifiers;
import org.agmas.holo.util.HologramType;

import java.util.ArrayList;

public class PrivateCommand extends TerminalCommand{
    @Override
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        ArrayList<String> str = new ArrayList<>();
        ClonePlayerComponent.KEY.get(player).clones.forEach((p)->{
            if (p.isHologram)
                str.add("private " + p.holoName );
        });
        return str;
    }

    @Override
    public Text run(String cmd, ServerPlayerEntity player) {
        if (cmd.split(" ").length < 2) {
            return Text.literal("Insufficient arguments supplied").formatted(Formatting.RED);
        }
        String referencedHolo = cmd.split(" ")[1];
        for (FakestPlayer c : ClonePlayerComponent.KEY.get(player).clones) {
            if (c.holoName.equals(referencedHolo)) {
                HoloPlayerComponent.KEY.get(referencedHolo).publicCamera = false;
                return Text.literal("Made " + c.holoName + "a private camera.").formatted(Formatting.GREEN);
            }
        }
        return Text.literal("No appropriate Holo found.").formatted(Formatting.RED);
    }

    @Override
    public boolean usableAsHuman() {
        return true;
    }
}
