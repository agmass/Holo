package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class TerminalCommand {
    public ArrayList<String> autoCompletion(ServerPlayerEntity player) {
        return new ArrayList<>();
    }
    public Text run(String cmd, ServerPlayerEntity player) {
        return Text.literal("Command not registered.").formatted(Formatting.RED);
    }
}
