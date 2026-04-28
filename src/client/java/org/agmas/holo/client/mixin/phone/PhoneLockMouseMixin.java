package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.render.PhoneHolder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class PhoneLockMouseMixin {


    @WrapMethod(method = "changeLookDirection")
    public void disableWhenWatchingCams(double cursorDeltaX, double cursorDeltaY, Operation<Void> original) {
        if (((Entity)(Object)this) instanceof ClientPlayerEntity) {
            if (MinecraftClient.getInstance().player.getActiveItem().isOf(ModItems.PHONE)) {
                if (!PhoneHolder.phoneState.equals(PhoneHolder.PhoneState.CAMERA)) {
                    PhoneHolder.cursorPos = PhoneHolder.cursorPos.add(new Vec2f((float) cursorDeltaX*1.5f, (float) cursorDeltaY*1.5f));
                    return;
                }
            }
        }
        original.call(cursorDeltaX,cursorDeltaY);
    }
}
