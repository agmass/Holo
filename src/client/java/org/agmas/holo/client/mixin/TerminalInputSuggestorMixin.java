package org.agmas.holo.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.client.screen.TerminalChatScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Mixin(ChatInputSuggestor.class)
public abstract class TerminalInputSuggestorMixin {

    @Shadow @Final private Screen owner;

    @Shadow @Final private List<OrderedText> messages;

    @Shadow protected abstract void showCommandSuggestions();

    @Shadow private int x;

    @Shadow private int width;

    @Shadow public abstract void show(boolean narrateFirstSuggestion);

    @Shadow @Nullable private ChatInputSuggestor.@Nullable SuggestionWindow window;

    @Shadow private boolean windowActive;

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private TextFieldWidget textField;

    @Shadow public abstract void refresh();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void refreshOnPacket(CallbackInfo ci) {
        if (TerminalChatScreen.refresh) {
            TerminalChatScreen.refresh = false;
            refresh();
        }
    }
    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    public void refreshMixin(CallbackInfo ci) {
        if (owner instanceof TerminalChatScreen) {
            messages.clear();
            ArrayList<String> copiedSuggest = (ArrayList<String>) TerminalChatScreen.possibleSuggestions.clone(); //stop concurrent modification errors
            copiedSuggest.forEach((m) -> {
                    if (m.startsWith(textField.getText()) || textField.getText().isEmpty())
                        messages.add(OrderedText.styledForwardsVisitedString(m, Style.EMPTY.withColor(Formatting.GREEN)));
                });
            x = 0;
            width= this.owner.width;

            window = null;
            if (windowActive && client.options.getAutoSuggestions().getValue()) {
                show(false);
            }
            ci.cancel();
        }
    }
}
