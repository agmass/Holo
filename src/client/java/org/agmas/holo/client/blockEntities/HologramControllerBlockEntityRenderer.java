package org.agmas.holo.client.blockEntities;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holo.Holo;
import org.agmas.holo.block.entity.HologramControllerBlockEntity;
import org.agmas.holo.client.config.HoloConfig;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class HologramControllerBlockEntityRenderer implements BlockEntityRenderer<HologramControllerBlockEntity> {


    public HologramControllerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }


    @Override
    public void render(HologramControllerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        HoloConfig config = AutoConfig.getConfigHolder(HoloConfig.class).getConfig();
        if (config.useVeilLights) {
            if (entity.lightHandle == null) {
                entity.pointLightData.setPosition(new Vector3d(entity.getPos().toCenterPos().x, entity.getPos().toCenterPos().y, entity.getPos().toCenterPos().z));
                entity.pointLightData.setColor(Colors.BLUE);
                entity.pointLightData.setRadius(3f);
                entity.lightHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(entity.pointLightData);
            }
            if (entity.isBattle) entity.lightHandle.getLightData().setColor(Colors.RED);
            entity.lightHandle.getLightData().setBrightness(Math.abs((float) Math.sin((entity.age + tickDelta) / 20f)));
            if (entity.age == -1) {
                entity.lightHandle.getLightData().setBrightness(0f);
            }
        }
    }

}
