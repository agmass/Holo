package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.holo.ModItems;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftClient.class)
public abstract class NoSwappingHotbarMixin {

    @Shadow
    protected abstract @Nullable PlayerEntity getCameraPlayer();

    @WrapMethod(method = "renderCrosshair")
    public void disableWhenWatchingCams(DrawContext context, RenderTickCounter tickCounter, Operation<Void> original) {
        if (getCameraPlayer() != null) {
            if (getCameraPlayer().getActiveItem().isOf(ModItems.PHONE)) {
                return;
            }
        }
        original.call(context,tickCounter);
    }
}
