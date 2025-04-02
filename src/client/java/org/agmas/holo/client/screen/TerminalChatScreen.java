package org.agmas.holo.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.network.PacketByteBuf;
import org.agmas.holo.Holo;

import java.awt.*;
import java.util.ArrayList;

public class TerminalChatScreen extends ChatScreen {
    public TerminalChatScreen(String originalChatText) {
        super(originalChatText);
    }

    public static boolean refresh = false;
    public static ArrayList<String> possibleSuggestions = new ArrayList<>();

    @Override
    protected void init() {
        super.init();
        this.chatField.setDrawsBackground(true);

        this.chatField.setEditableColor(Color.GREEN.getRGB());
        chatField.setWidth(width-6);
        chatField.setX(chatField.getX()-1);
        chatField.setY(chatField.getY()-1);
    }

    @Override
    public boolean sendMessage(String chatText, boolean addToHistory) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeString(chatText);
        if (client != null) {
            client.execute(() -> {
                ClientPlayNetworking.send(Holo.TERMINAL_COMMAND, data);
            });
        }
        return true;
    }
}
