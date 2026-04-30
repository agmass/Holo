package org.agmas.holo.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.watermedia.api.media.engines.GFXEngine;
import org.watermedia.api.media.engines.GLEngine;

import java.awt.*;
import java.nio.ByteBuffer;

@Mixin(value = GLEngine.class, priority = 1900)
public abstract class TurnOffGLEngineMixin extends GFXEngine {

    @WrapOperation(method = "convertToRGBA", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL13;glActiveTexture(I)V"), remap = false)
    public void empty(int texture, Operation<Void> original) {
        RenderSystem.activeTexture(texture);
    }
}
