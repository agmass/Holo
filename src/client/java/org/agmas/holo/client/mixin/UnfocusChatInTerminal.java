package org.agmas.holo.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.agmas.holo.client.screen.TerminalChatScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class UnfocusChatInTerminal {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "isChatFocused", at = @At("HEAD"), cancellable = true)
    public void unfocusChat(CallbackInfoReturnable<Boolean> cir) {
        if (client.currentScreen instanceof TerminalChatScreen) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    @Inject(method = "isChatHidden", at = @At("HEAD"), cancellable = true)
    public void hideChat(CallbackInfoReturnable<Boolean> cir) {
        if (client.currentScreen instanceof TerminalChatScreen) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
