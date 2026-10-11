package com.jerrymod.client;

import com.jerrymod.JerryMod;
import com.jerrymod.entity.JerryEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class JerryRenderer extends MobEntityRenderer<JerryEntity, JerryModel> {
    // 0 pupila al frente, 1 izquierda, 2 derecha, 3 cada ojo a un lado distinto
    private static final Identifier[] TEX = {
            new Identifier(JerryMod.ID, "textures/entity/jerry_0.png"),
            new Identifier(JerryMod.ID, "textures/entity/jerry_1.png"),
            new Identifier(JerryMod.ID, "textures/entity/jerry_2.png"),
            new Identifier(JerryMod.ID, "textures/entity/jerry_3.png")
    };

    public JerryRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new JerryModel(ctx.getPart(JerryClient.JERRY_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(JerryEntity e) {
        return TEX[Math.floorMod(e.getEyeType(), 4)];
    }
}
