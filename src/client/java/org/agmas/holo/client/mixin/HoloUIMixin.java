package org.agmas.holo.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.holo.client.HoloClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Date;

@Mixin(InGameHud.class)
public abstract class HoloUIMixin {

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow protected abstract int getHeartCount(LivingEntity entity);

    @Shadow protected abstract LivingEntity getRiddenEntity();

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Unique
    private static final Identifier SHELL_VIGNETTE_TEXTURE = Identifier.of("holo", "textures/misc/shellvignette.png");

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
