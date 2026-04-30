package org.agmas.holo.client.models;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holidaylib.client.HolidaylibClient;
import org.agmas.holo.Holo;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.client.config.HoloConfig;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.joml.Vector3d;

import java.awt.*;

public class HoloLightRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PointLightData pointLightData = new PointLightData();
    public LightRenderHandle<PointLightData> lightHandle;

    public HoloLightRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, EntityModelLoader loader) {
        super(context);
        pointLightData.setPosition(new Vector3d(0,0,0));
        pointLightData.setColor(Colors.BLUE);
        pointLightData.setBrightness(0f);
        pointLightData.setRadius(2.5f);
        HoloConfig config = AutoConfig.getConfigHolder(HoloConfig.class).getConfig();
        if (config.useVeilLights)
            lightHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(pointLightData);
    }

    @Override
    protected Identifier getTexture(AbstractClientPlayerEntity entity) {
        return null;
    }


    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

        HoloConfig config = AutoConfig.getConfigHolder(HoloConfig.class).getConfig();
        if (config.useVeilLights) {
            if (!(entity instanceof ClientPlayerEntity)) {
                changeLightWithPlayer(entity, lightHandle, tickDelta);
            } else {
                pointLightData.setBrightness(0f);
            }
        }
    }

    public static void changeLightWithPlayer(AbstractClientPlayerEntity entity, LightRenderHandle<PointLightData> lightHandle, float tickDelta) {
        if (HoloPlayerComponent.KEY.get(entity).inHoloMode || HoloClient.playersInHolo.containsKey(entity.getUuid())) {
            Color color;
            lightHandle.getLightData().setPosition(new Vector3d(entity.getX(), entity.getBodyY(1), entity.getZ()));
            lightHandle.getLightData().setRadius(5f);
            HologramType type = HoloPlayerComponent.KEY.get(entity).hologramType;
            if (HoloClient.playersInHolo.containsKey(entity.getUuid())) {
                type = HoloClient.playersInHolo.get(entity.getUuid());
            }
            switch (type) {
                case SILENT -> color = HoloClient.SILENT_HOLO_COLOR;
                case BATTLE, BATTLE_DUEL -> color = HoloClient.BATTLE_HOLO_COLOR;
                case SCOUT -> color = HoloClient.SCOUT_HOLO_COLOR;
                case CAMERA -> color = HoloClient.CAMERA_HOLO_COLOR;
                default -> color = HoloClient.HOLO_COLOR;
            }
            lightHandle.getLightData().setColor(color.getRGB());
            lightHandle.getLightData().setBrightness(Math.abs((float) Math.sin((entity.age + tickDelta) / 40f)));
        } else {
            lightHandle.getLightData().setColor(Colors.WHITE);
            lightHandle.getLightData().setRadius(100000f);
            lightHandle.getLightData().setBrightness(0.0001f);
        }
    }

}
