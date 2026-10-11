package com.jerrymod.client;

import com.jerrymod.entity.JerryEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/** Copia de la geometría del modelo del lobo/perro vanilla, con animaciones propias. */
public class JerryModel extends EntityModel<JerryEntity> {
    private final ModelPart head, body, upperBody, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail;

    public JerryModel(ModelPart root) {
        head = root.getChild("head");
        body = root.getChild("body");
        upperBody = root.getChild("upper_body");
        rightHindLeg = root.getChild("right_hind_leg");
        leftHindLeg = root.getChild("left_hind_leg");
        rightFrontLeg = root.getChild("right_front_leg");
        leftFrontLeg = root.getChild("left_front_leg");
        tail = root.getChild("tail");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();
        float half = (float) Math.PI / 2f;
        r.addChild("head", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-2f, -3f, -2f, 6f, 6f, 4f)
                .uv(16, 14).cuboid(-2f, -5f, 0f, 2f, 2f, 1f)
                .uv(16, 14).cuboid(2f, -5f, 0f, 2f, 2f, 1f)
                .uv(0, 10).cuboid(-0.5f, 0f, -5f, 3f, 3f, 4f),
                ModelTransform.pivot(-1f, 13.5f, -7f));
        r.addChild("body", ModelPartBuilder.create().uv(18, 14).cuboid(-3f, -2f, -3f, 6f, 9f, 6f),
                ModelTransform.of(0f, 14f, 2f, half, 0f, 0f));
        r.addChild("upper_body", ModelPartBuilder.create().uv(21, 0).cuboid(-3f, -3f, -3f, 8f, 6f, 7f),
                ModelTransform.of(-1f, 14f, -3f, half, 0f, 0f));
        ModelPartBuilder leg = ModelPartBuilder.create().uv(0, 18).cuboid(0f, 0f, -1f, 2f, 8f, 2f);
        r.addChild("right_hind_leg", leg, ModelTransform.pivot(-2.5f, 16f, 7f));
        r.addChild("left_hind_leg", leg, ModelTransform.pivot(0.5f, 16f, 7f));
        r.addChild("right_front_leg", leg, ModelTransform.pivot(-2.5f, 16f, -4f));
        r.addChild("left_front_leg", leg, ModelTransform.pivot(0.5f, 16f, -4f));
        r.addChild("tail", ModelPartBuilder.create().uv(9, 18).cuboid(0f, 0f, -1f, 2f, 8f, 2f),
                ModelTransform.of(-1f, 12f, 8f, 0.62831855f, 0f, 0f));
        return TexturedModelData.of(md, 64, 32);
    }

    @Override
    public void setAngles(JerryEntity e, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
        float half = (float) Math.PI / 2f;
        boolean paw = e.getTrick() == JerryEntity.TRICK_PAW;
        boolean sit = e.isInSittingPose() || paw;

        if (sit) {
            upperBody.setPivot(-1f, 16f, -3f); upperBody.pitch = 1.2566371f;
            body.setPivot(0f, 18f, 0f); body.pitch = 0.7853982f;
            tail.setPivot(-1f, 21f, 6f);
            rightHindLeg.setPivot(-2.5f, 22.7f, 2f); rightHindLeg.pitch = 4.712389f;
            leftHindLeg.setPivot(0.5f, 22.7f, 2f); leftHindLeg.pitch = 4.712389f;
            rightFrontLeg.setPivot(-2.49f, 17f, -4f); rightFrontLeg.pitch = 5.811947f;
            leftFrontLeg.setPivot(0.51f, 17f, -4f); leftFrontLeg.pitch = 5.811947f;
        } else {
            upperBody.setPivot(-1f, 14f, -3f); upperBody.pitch = half;
            body.setPivot(0f, 14f, 2f); body.pitch = half;
            tail.setPivot(-1f, 12f, 8f);
            rightHindLeg.setPivot(-2.5f, 16f, 7f);
            leftHindLeg.setPivot(0.5f, 16f, 7f);
            rightFrontLeg.setPivot(-2.5f, 16f, -4f);
            leftFrontLeg.setPivot(0.5f, 16f, -4f);
            rightHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
            leftHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance;
            rightFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance;
            leftFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
        }

        // Dame la pata: levanta la pata delantera
        if (paw) {
            leftFrontLeg.pitch = -1.2f + MathHelper.sin(age * 0.8f) * 0.35f;
        }

        head.pitch = headPitch * 0.017453292f;
        head.yaw = headYaw * 0.017453292f;

        // Cola: feliz = mueve rápido, enojado = quieta y baja
        int h = e.getHappiness();
        float speed = h > 60 ? 0.9f : h > 30 ? 0.3f : 0.05f;
        tail.yaw = MathHelper.sin(age * speed) * 0.5f * (h > 30 ? 1f : 0.2f);
        tail.pitch = h > 30 ? 0.62831855f : 1.5f;
    }

    @Override
    public void render(MatrixStack m, VertexConsumer v, int light, int overlay, float r, float g, float b, float a) {
        for (ModelPart p : new ModelPart[]{head, body, upperBody, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail}) {
            p.render(m, v, light, overlay, r, g, b, a);
        }
    }
}
