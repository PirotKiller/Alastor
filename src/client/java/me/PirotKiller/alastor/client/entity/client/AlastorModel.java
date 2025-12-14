package me.PirotKiller.alastor.client.entity.client;

import me.PirotKiller.alastor.Alastor;
import me.PirotKiller.alastor.client.entity.custom.AlastorEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AlastorModel<T extends AlastorEntity> extends SinglePartEntityModel<T> {
        public static final EntityModelLayer ALASTOR = new EntityModelLayer(Identifier.of(Alastor.MOD_ID, "alastor"), "main");
        private final ModelPart alastor;
        private final ModelPart head;
        private final ModelPart staff;
        public AlastorModel(ModelPart root) {
            this.alastor = root.getChild("alastor");
            this.head = this.alastor.getChild("head");
            this.staff = this.alastor.getChild("staff");
        }
        public static TexturedModelData getTexturedModelData() {
            ModelData modelData = new ModelData();
            ModelPartData modelPartData = modelData.getRoot();
            ModelPartData alastor = modelPartData.addChild("alastor", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

            ModelPartData horns = alastor.addChild("horns", ModelPartBuilder.create().uv(0, 95).cuboid(-10.0F, -42.0F, 1.6F, 20.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

            ModelPartData head = alastor.addChild("head", ModelPartBuilder.create().uv(0, 33).cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -32.0F, 0.0F));

            ModelPartData cube_r1 = head.addChild("cube_r1", ModelPartBuilder.create().uv(38, 49).cuboid(1.0F, -4.0F, -1.0F, 0.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, -6.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

            ModelPartData cube_r2 = head.addChild("cube_r2", ModelPartBuilder.create().uv(38, 49).cuboid(1.0F, -4.0F, -1.0F, 0.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, -6.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

            ModelPartData neck = alastor.addChild("neck", ModelPartBuilder.create().uv(26, 49).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -30.0F, 0.0F));

            ModelPartData body = alastor.addChild("body", ModelPartBuilder.create().uv(0, 13).cuboid(-5.0F, -7.0F, -3.0F, 10.0F, 14.0F, 6.0F, new Dilation(0.0F))
                    .uv(16, 49).cuboid(5.0F, -6.0F, -2.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F))
                    .uv(48, 37).cuboid(-6.0F, -6.0F, -2.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -23.0F, 0.0F));

            ModelPartData right_limb = alastor.addChild("right_limb", ModelPartBuilder.create().uv(16, 45).cuboid(-7.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-6.0F, -27.0F, 0.0F));

            ModelPartData right_arm = alastor.addChild("right_arm", ModelPartBuilder.create().uv(34, 45).cuboid(-7.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-13.0F, -27.0F, 0.0F));

            ModelPartData left_limb = alastor.addChild("left_limb", ModelPartBuilder.create().uv(46, 0).cuboid(0.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, -27.0F, 0.0F));

            ModelPartData left_arm = alastor.addChild("left_arm", ModelPartBuilder.create().uv(46, 4).cuboid(0.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(13.0F, -27.0F, 0.0F));

            ModelPartData right_leg = alastor.addChild("right_leg", ModelPartBuilder.create().uv(8, 45).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, -16.0F, 0.0F));

            ModelPartData left_leg = alastor.addChild("left_leg", ModelPartBuilder.create().uv(0, 45).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -16.0F, 0.0F));

            ModelPartData right_foot = alastor.addChild("right_foot", ModelPartBuilder.create().uv(42, 49).cuboid(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                    .uv(42, 52).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                    .uv(24, 33).cuboid(-1.0F, 0.0F, -3.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, -2.0F, 0.0F));

            ModelPartData left_foot = alastor.addChild("left_foot", ModelPartBuilder.create().uv(48, 49).cuboid(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                    .uv(52, 45).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                    .uv(46, 8).cuboid(-1.0F, 0.0F, -3.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -2.0F, 0.0F));

            ModelPartData suit1 = alastor.addChild("suit1", ModelPartBuilder.create(), ModelTransform.pivot(5.0F, -17.6F, 0.0F));

            ModelPartData cube_r3 = suit1.addChild("cube_r3", ModelPartBuilder.create().uv(32, 13).cuboid(1.0F, 0.0F, -6.0F, 6.0F, 0.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 4.6F, -4.0F, 0.0F, -1.5708F, 1.3526F));

            ModelPartData suit2 = alastor.addChild("suit2", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -18.0F, 2.9F));

            ModelPartData cube_r4 = suit2.addChild("cube_r4", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, 0.0F, -2.0F, 10.0F, 0.0F, 13.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, 0.0F, 0.1F, -1.3963F, 0.0F, 0.0F));

            ModelPartData suit3 = alastor.addChild("suit3", ModelPartBuilder.create(), ModelTransform.pivot(-5.0F, -17.0F, 0.0F));

            ModelPartData cube_r5 = suit3.addChild("cube_r5", ModelPartBuilder.create().uv(32, 25).cuboid(1.0F, 0.0F, -2.0F, 6.0F, 0.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, -4.0F, 0.0F, -1.5708F, -1.3526F));

            ModelPartData tentacle1 = alastor.addChild("tentacle1", ModelPartBuilder.create().uv(0, 61).cuboid(-11.0F, -8.0F, 0.0F, 12.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, -27.0F, 3.0F));

            ModelPartData tentacle2 = alastor.addChild("tentacle2", ModelPartBuilder.create().uv(0, 70).cuboid(-10.4F, -1.6F, 0.0F, 12.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.6F, -24.4F, 3.0F));

            ModelPartData tentacle3 = alastor.addChild("tentacle3", ModelPartBuilder.create(), ModelTransform.pivot(3.0F, -27.0F, 3.1F));

            ModelPartData cube_r6 = tentacle3.addChild("cube_r6", ModelPartBuilder.create().uv(0, 61).cuboid(-11.0F, -9.0F, -1.0F, 12.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, -1.0F, 0.0F, 3.1416F, 0.0F));

            ModelPartData tentacle4 = alastor.addChild("tentacle4", ModelPartBuilder.create(), ModelTransform.pivot(3.5F, -24.4F, 3.1F));

            ModelPartData cube_r7 = tentacle4.addChild("cube_r7", ModelPartBuilder.create().uv(0, 70).cuboid(-11.0F, -9.0F, -1.0F, 12.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 7.4F, -1.0F, 0.0F, 3.1416F, 0.0F));

            ModelPartData staff = alastor.addChild("staff", ModelPartBuilder.create().uv(2, 1).cuboid(-0.5F, -13.0F, -0.5F, 1.0F, 16.0F, 1.0F, new Dilation(0.0F))
                    .uv(91, 1).cuboid(-2.0F, -16.0F, -1.0F, 4.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(18.5F, -9.0F, -0.5F));
            return TexturedModelData.of(modelData, 128, 128);
        }


        @Override
        public void setAngles(AlastorEntity  entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            this.getPart().traverse().forEach(ModelPart::resetTransform);
            this.setHeadAngles(netHeadYaw, headPitch);
            this.updateAnimation(entity.idleAnimationState, AlastorAnimations.ideal, ageInTicks, 1f);
            this.animateMovement(AlastorAnimations.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
            this.updateAnimation(entity.attackAnimationState, AlastorAnimations.attack1, ageInTicks, 1f);
        }

        private void setHeadAngles(float headYaw, float headPitch) {
            headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
            headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

            this.head.yaw = headYaw * 0.017453292F;
            this.head.pitch = headPitch * 0.017453292F;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
            alastor.render(matrices, vertexConsumer, light, overlay, color);
        }

        @Override
        public ModelPart getPart() {
            return alastor;
        }
}
