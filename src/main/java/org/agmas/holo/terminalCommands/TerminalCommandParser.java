package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public class TerminalCommandParser {
    public static HashMap<String, TerminalCommand> nameAndCommands = new HashMap<>();

    public static void initCommands() {
        nameAndCommands.put("swap", new SwapCommand());
        nameAndCommands.put("rename", new RenameCommand());
        nameAndCommands.put("exit", new ExitCommand());
        nameAndCommands.put("detonate", new DetonateCommand());
    }
    public static Text findAndRunCommand(String userInput, ServerPlayerEntity player) {
        String root = userInput.split(" ")[0];
        if (nameAndCommands.containsKey(root)) {
            return nameAndCommands.get(root).run(userInput,player);
        }
        return Text.literal("No command found.").formatted(Formatting.RED);
    }
}
