package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.render.PhoneHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class PhoneNoCrosshairMixin {

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
