package com.jerrymod.client;

import com.jerrymod.entity.JerryMonsterEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/** Humanoide encorvado de brazos largos. Se dibuja escalado x1.9 => ~4 bloques de alto. */
public class MonsterModel extends EntityModel<JerryMonsterEntity> {
    private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;

    public MonsterModel(ModelPart root) {
        head = root.getChild("head");
        body = root.getChild("body");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();
        r.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-3f, -8f, -3f, 6f, 8f, 6f),
                ModelTransform.pivot(0f, -2f, 0f));
        r.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4f, -12f, -2f, 8f, 12f, 4f),
                ModelTransform.pivot(0f, 10f, 0f));
        r.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-2.5f, -1.5f, -1.5f, 3f, 18f, 3f),
                ModelTransform.pivot(-5.5f, -0.5f, 0f));
        r.addChild("left_arm", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-0.5f, -1.5f, -1.5f, 3f, 18f, 3f),
                ModelTransform.pivot(5.5f, -0.5f, 0f));
        r.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-2f, 0f, -2f, 4f, 14f, 4f),
                ModelTransform.pivot(-2.5f, 10f, 0f));
        r.addChild("left_leg", ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2f, 0f, -2f, 4f, 14f, 4f),
                ModelTransform.pivot(2.5f, 10f, 0f));
        return TexturedModelData.of(md, 64, 64);
    }

    @Override
    public void setAngles(JerryMonsterEntity e, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
        head.yaw = headYaw * 0.017453292f;
        head.pitch = headPitch * 0.017453292f + 0.25f;          // cabeza caída hacia adelante
        head.roll = MathHelper.sin(age * 0.9f) * 0.08f;          // tic nervioso

        float swing = MathHelper.cos(limbAngle * 0.6662f);
        rightLeg.pitch = swing * 1.2f * limbDistance;
        leftLeg.pitch = -swing * 1.2f * limbDistance;

        if (e.isAttacking()) {
            // persiguiendo: brazos estirados hacia el jugador
            rightArm.pitch = -1.45f + swing * 0.25f * limbDistance;
            leftArm.pitch = -1.45f - swing * 0.25f * limbDistance;
            rightArm.roll = 0.12f; leftArm.roll = -0.12f;
        } else {
            rightArm.pitch = -swing * 0.6f * limbDistance + MathHelper.sin(age * 0.07f) * 0.05f;
            leftArm.pitch = swing * 0.6f * limbDistance - MathHelper.sin(age * 0.07f) * 0.05f;
            rightArm.roll = 0.05f; leftArm.roll = -0.05f;
        }
        body.pitch = e.isAttacking() ? 0.3f : 0.12f;
    }

    @Override
    public void render(MatrixStack m, VertexConsumer v, int light, int overlay, float r, float g, float b, float a) {
        for (ModelPart p : new ModelPart[]{head, body, rightArm, leftArm, rightLeg, leftLeg}) {
            p.render(m, v, light, overlay, r, g, b, a);
        }
    }
}
