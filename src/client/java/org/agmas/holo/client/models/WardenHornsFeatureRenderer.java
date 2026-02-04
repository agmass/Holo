package org.agmas.holo.client.models;

import foundry.veil.api.client.render.rendertype.VeilRenderType;
import me.shedaniel.autoconfig.AutoConfig;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.agmas.holidaylib.client.HolidaylibClient;
import org.agmas.holo.Holo;
import org.agmas.holo.client.HoloClient;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.joml.Vector3f;

public class WardenHornsFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    ModelPart horns;
    public WardenHornsFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, EntityModelLoader loader) {
        super(context);
        horns = loader.getModelPart(WardensHorns.MODEL_LAYER);
    }

    @Override
    protected Identifier getTexture(AbstractClientPlayerEntity entity) {
        return Identifier.of(Holo.MOD_ID,"textures/entity/wardenhorns.png");
    }


    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (HoloClient.playersInHolo.containsKey(entity.getUuid())) {
            if (HoloClient.playersInHolo.get(entity.getUuid()).equals(HologramType.SILENT)) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
                getContextModel().head.copyTransform(horns);

                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(HolidaylibClient.shaderFallbackLayer(VeilRenderType.get(Identifier.of(Holo.MOD_ID, "scanline"), getTexture(entity)), HolidaylibClient.translucentMaskedEmissiveRenderLayer(getTexture(entity))));

                int m = LivingEntityRenderer.getOverlay(entity, 0.0F);
                matrices.translate(0, entity.isSneaky() ? -1.9F : -2.1875F, 0);
                matrices.scale(1.3333334F, 1.3333334F, 1.3333334F);
                horns.render(matrices, vertexConsumer, light, m, HoloClient.SILENT_HOLO_COLOR.getRGB());
            }
        }

    }

}
