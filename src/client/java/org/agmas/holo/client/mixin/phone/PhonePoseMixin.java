package org.agmas.holo.client.mixin.phone;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.veil.api.client.util.Easing;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holo.ModItems;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.client.render.PhoneHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HeldItemRenderer.class)
public abstract class PhonePoseMixin {
    @Shadow
    protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @WrapMethod(method = "renderFirstPersonItem")
    public void renderPhonePose(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Operation<Void> original) {
        if (player.getItemUseTime() > 0 && item.isOf(ModItems.PHONE)) {
            PhoneHolder.transform(player,tickDelta,pitch,hand,swingProgress,item,equipProgress,matrices,vertexConsumers,light);
            matrices.push();
            matrices.translate(0.05f,0,0);
            float tweenedRot = Easing.EASE_OUT_SINE.ease(PhoneHolder.rotationTransition);
            matrices.translate(0.1f * (1-tweenedRot),0,0);

            renderArmHoldingItem(matrices, vertexConsumers, light, 0f, player.getMainArm().equals(Arm.LEFT) ? swingProgress : 0f, Arm.LEFT);
            matrices.pop();
            matrices.push();
            matrices.translate(-0.05f,0,0);
            matrices.translate(-0.1f * (1-tweenedRot),0,0);
            renderArmHoldingItem(matrices, vertexConsumers, light, 0f, player.getMainArm().equals(Arm.RIGHT) ? swingProgress : 0f, Arm.RIGHT);
            matrices.pop();
            PhoneHolder.render(itemRenderer,player,tickDelta,pitch,hand,swingProgress,item,equipProgress,matrices,vertexConsumers,light);
            matrices.pop();
        } else {
            original.call(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
    }
}
