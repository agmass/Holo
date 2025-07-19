package org.agmas.holo.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.Format;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Mixin(InGameHud.class)
public abstract class HoloUIMixin {

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow protected abstract int getHeartCount(LivingEntity entity);

    @Shadow protected abstract LivingEntity getRiddenEntity();

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Shadow public abstract TextRenderer getTextRenderer();

    @Unique
    private static final Identifier SHELL_VIGNETTE_TEXTURE = Identifier.of("holo", "textures/misc/shellvignette.png");
    @Inject(method = "render", at = @At("TAIL"))
    public void cardRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HoloClient.hologramType != null) {
            List<Text> lines;
            if (HoloClient.hologramType.equals(HologramType.BATTLE_DUEL)) {
                Text line1 = Text.literal("■ " + HoloClient.holoName.toUpperCase()).formatted(Formatting.DARK_RED);
                Text line2 = Text.literal("IN A DUEL").formatted(Formatting.DARK_GRAY);
                Text line4 = Text.literal("■ ").formatted(Formatting.RED).append(Text.literal(HoloClient.playersInDuel + " players left.").formatted(Formatting.WHITE));
               lines = List.of(line4, Text.of(" "), line2, line1);
            } else {
                Text line1 = Text.literal("■ " + HoloClient.holoName.toUpperCase()).formatted(Formatting.AQUA);
                Text line2 = Text.literal("TYPE: ").formatted(Formatting.AQUA).append(Text.literal(HoloClient.hologramType.toString()).formatted(Formatting.WHITE));
                MutableText line3 = Text.literal("POWER: ").formatted(Formatting.YELLOW).append(HoloClient.power + "/" + HoloClient.maxPower).formatted(Formatting.YELLOW);

                if (MinecraftClient.getInstance().getWindow().getFramerateLimit() != MinecraftClient.getInstance().options.getMaxFps().getValue()) {
                    MinecraftClient.getInstance().getWindow().setFramerateLimit(MinecraftClient.getInstance().options.getMaxFps().getValue());
                }

                if (HoloClient.power == HoloClient.maxPower) {
                    line3 = line3.formatted(Formatting.RED);
                }
                if (MinecraftClient.getInstance().player != null) {
                    if (MinecraftClient.getInstance().player.isSubmergedInWater()) {
                        line3 = line3.append(Text.literal(" (IN WATER)").formatted(Formatting.RED));
                    }
                    if (MinecraftClient.getInstance().player.getHealth() <= 5) {
                        line3 = line3.append(Text.literal(" (LOW HEALTH)").formatted(Formatting.RED));
                    }

                    if (MinecraftClient.getInstance().player.getWorld().getDimension().piglinSafe() || MinecraftClient.getInstance().player.isOnFire()) {
                        line3 = line3.append(Text.literal(" (COOLER ENABLED)").formatted(Formatting.RED));
                    }
                }
                Text line4 = Text.literal("■ ").formatted(HoloClient.hostHealth > 10 ? Formatting.GREEN : HoloClient.hostHealth > 5 ? Formatting.YELLOW : Formatting.RED).append(Text.literal(getCameraPlayer().getNameForScoreboard()).formatted(Formatting.WHITE));

                Text line5 = Text.literal("HEALTH: ").formatted(HoloClient.hostHealth > 10 ? Formatting.GREEN : HoloClient.hostHealth > 5 ? Formatting.YELLOW : Formatting.RED).append(Text.literal(HoloClient.hostHealth + "").formatted(Formatting.WHITE));
                lines = List.of(line5, line4, Text.of(" "), line3, line2, line1);
            }
            if (HoloClient.power > HoloClient.maxPower) {
                MinecraftClient.getInstance().getWindow().setFramerateLimit(8);
                Text line = Text.literal("PLEASE USE LESS POWER").formatted(Formatting.RED);
                MutableText line2 = Text.literal(HoloClient.power + "/" + HoloClient.maxPower).formatted(Formatting.YELLOW);
                MutableText line3 = Text.literal("");
                if (MinecraftClient.getInstance().player != null) {
                    if (MinecraftClient.getInstance().player.isSubmergedInWater()) {
                        line3 = line3.append(Text.literal(" (IN WATER)").formatted(Formatting.GRAY));
                    }
                    if (MinecraftClient.getInstance().player.getHealth() <= 5) {
                        line3 = line3.append(Text.literal(" (LOW HEALTH)").formatted(Formatting.GRAY));
                    }
                    if (MinecraftClient.getInstance().player.getWorld().getDimension().piglinSafe() || MinecraftClient.getInstance().player.isOnFire()) {
                        line3 = line3.append(Text.literal(" (COOLER ENABLED)").formatted(Formatting.GRAY));
                    }
                }
                lines = List.of(line,line2,line3);
            }
            int drawY = context.getScaledWindowHeight();

            for (Text line : lines) {
                drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
                context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, Colors.WHITE);
            }
        }
    }
    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"))
    public int noFood(InGameHud instance, LivingEntity entity) {
        if (HoloClient.playersInHolo.containsKey(getCameraPlayer().getUuid()) && getRiddenEntity() == null) {
            return -1;
        }
        return getHeartCount(getRiddenEntity());
    }

    @Inject(method = "renderMiscOverlays", at = @At("HEAD"), cancellable = true)
    public void shellVignette(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        LocalDate localDate = LocalDate.now();
        int i = localDate.get(ChronoField.DAY_OF_MONTH);
        int j = localDate.get(ChronoField.MONTH_OF_YEAR);
        if (HoloClient.playersInHolo.containsKey(client.cameraEntity.getUuid())) {


            renderOverlay(context,(j == 12 && i == 25) ? Identifier.of("holo", "textures/misc/christmas_shell_vignette.png") : SHELL_VIGNETTE_TEXTURE, 0.5f);

            ci.cancel();
        }
    }

}
