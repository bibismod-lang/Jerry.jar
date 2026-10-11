package com.jerrymod.client;

import com.jerrymod.JerryMod;
import com.jerrymod.entity.JerryMonsterEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MonsterRenderer extends MobEntityRenderer<JerryMonsterEntity, MonsterModel> {
    private static final Identifier TEX = new Identifier(JerryMod.ID, "textures/entity/jerry_monster.png");

    public MonsterRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new MonsterModel(ctx.getPart(JerryClient.MONSTER_LAYER)), 1.4f);
    }

    @Override
    protected void scale(JerryMonsterEntity e, MatrixStack m, float tickDelta) {
        m.scale(1.9f, 1.9f, 1.9f); // 34 px * 1.9 ≈ 4 bloques
    }

    @Override
    public Identifier getTexture(JerryMonsterEntity e) { return TEX; }
}
