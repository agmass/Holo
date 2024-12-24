package org.agmas.holo.client.models;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class WardensHorns extends EntityModel<PlayerEntity> {
	private final ModelPart bone;

	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(Holo.MOD_ID,"wardenhorns"), "main");
	public WardensHorns(ModelPart root) {
		this.bone = root.getChild("bone");
	}

	public static TexturedModelData getTexturedModelData() {

		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, 18.0F, 0.0F));

		ModelPartData bone4 = bone.addChild("bone4", ModelPartBuilder.create(), ModelTransform.pivot(1.5F, 6.0F, 0.0F));

		ModelPartData cube_r1 = bone4.addChild("cube_r1", ModelPartBuilder.create().uv(1, 9).cuboid(-2.5F, 0.0F, 0.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.9F, -8.2F, -0.4F, 0.0F, 0.0F, -1.9897F));

		ModelPartData cube_r2 = bone4.addChild("cube_r2", ModelPartBuilder.create().uv(10, 11).cuboid(-2.5F, 0.0F, 0.0F, 3.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(5.7F, -6.1F, -0.5F, 0.0F, 0.0F, -1.1345F));

		ModelPartData cube_r3 = bone4.addChild("cube_r3", ModelPartBuilder.create().uv(-1, 4).cuboid(-3.5F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(3.5F, -2.6F, 0.0F, 0.0F, 0.0F, -0.2618F));

		ModelPartData bone2 = bone.addChild("bone2", ModelPartBuilder.create(), ModelTransform.pivot(2.5F, 5.0F, -0.05F));

		ModelPartData cube_r4 = bone2.addChild("cube_r4", ModelPartBuilder.create().uv(14, 0).cuboid(-4.0F, -1.0F, -5.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(0, 11).cuboid(-3.0F, -2.0F, -5.4F, 3.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-10.0F, -6.0F, 4.45F, 0.0F, 0.0F, 2.3213F));

		ModelPartData cube_r5 = bone2.addChild("cube_r5", ModelPartBuilder.create().uv(12, 8).cuboid(-4.0F, -1.0F, -5.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-9.0F, -2.0F, 4.45F, 0.0F, 0.0F, 1.309F));

		ModelPartData cube_r6 = bone2.addChild("cube_r6", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -2.0F, -5.0F, 6.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, 0.0F, 4.05F, 0.0F, 0.0F, 0.3491F));
		return TexturedModelData.of(modelData, 16, 16);
	}


	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		bone.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

	@Override
	public void setAngles(PlayerEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}
}