package org.agmas.holo.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
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
    @Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow protected abstract int getHeartCount(LivingEntity entity);

    @Shadow protected abstract LivingEntity getRiddenEntity();

    @Shadow @Final private static Identifier ICONS;
    @Unique
    private static final Identifier SHELL_VIGNETTE_TEXTURE = new Identifier("holo", "textures/misc/shellvignette.png");

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"))
    public int noFood(InGameHud instance, LivingEntity entity) {
        if (HoloClient.playersInHolo.containsKey(getCameraPlayer().getUuid()) && getRiddenEntity() == null) {
            return -1;
        }
        return getHeartCount(getRiddenEntity());
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    public void shellVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        LocalDate localDate = LocalDate.now();
        int i = localDate.get(ChronoField.DAY_OF_MONTH);
        int j = localDate.get(ChronoField.MONTH_OF_YEAR);
        if (HoloClient.playersInHolo.containsKey(entity.getUuid())) {
            context.drawTexture((j == 12 && i == 25) ? new Identifier("holo", "textures/misc/christmas_shell_vignette.png") : SHELL_VIGNETTE_TEXTURE, 0, 0, -90, 0.0F, 0.0F, scaledWidth, scaledHeight, scaledWidth, scaledHeight);
            ci.cancel();
        }
    }

}
