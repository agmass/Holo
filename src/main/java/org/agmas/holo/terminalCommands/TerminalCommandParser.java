package org.agmas.holo.terminalCommands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.holo.state.HoloPlayerComponent;

import java.util.HashMap;

public class TerminalCommandParser {
    public static HashMap<String, TerminalCommand> nameAndCommands = new HashMap<>();

    public static void initCommands() {
        nameAndCommands.put("swap", new SwapCommand());
        nameAndCommands.put("rename", new RenameCommand());
        nameAndCommands.put("exit", new ExitCommand());
        nameAndCommands.put("detonate", new DetonateCommand());
        nameAndCommands.put("power", new PowerInfoCommand());
        nameAndCommands.put("enable", new EnableCommand());
        nameAndCommands.put("disable", new DisableCommand());
        nameAndCommands.put("public", new PublicCommand());
        nameAndCommands.put("private", new PrivateCommand());
    }
    public static Text findAndRunCommand(String userInput, ServerPlayerEntity player) {
        String root = userInput.split(" ")[0];
        if (nameAndCommands.containsKey(root)) {
            if (HoloPlayerComponent.KEY.get(player).inHoloMode || nameAndCommands.get(root).usableAsHuman())
                return nameAndCommands.get(root).run(userInput,player);
        }
        return Text.literal("No command found.").formatted(Formatting.RED);
    }
}
