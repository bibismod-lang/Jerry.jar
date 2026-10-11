package com.jerrymod.client;

import com.jerrymod.JerryMod;
import com.jerrymod.entity.JerryEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public class JerryClient implements ClientModInitializer {
    public static final EntityModelLayer JERRY_LAYER = new EntityModelLayer(new Identifier(JerryMod.ID, "jerry"), "main");
    public static final EntityModelLayer MONSTER_LAYER = new EntityModelLayer(new Identifier(JerryMod.ID, "jerry_monster"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(JERRY_LAYER, JerryModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MONSTER_LAYER, MonsterModel::getTexturedModelData);
        EntityRendererRegistry.register(JerryMod.JERRY, JerryRenderer::new);
        EntityRendererRegistry.register(JerryMod.MONSTER, MonsterRenderer::new);

        // Tecla J: abre la pantalla para hablar con Jerry
        KeyBinding talk = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jerrymod.talk", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.jerrymod"));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (talk.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new JerryScreen());
            }
        });

        // Botón "Jerry" dentro de la pantalla del chat
        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof ChatScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Jerry"),
                        b -> mc.setScreen(new JerryScreen())).dimensions(w - 62, 4, 58, 20).build());
            }
        });

        // Barra de felicidad
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
            List<JerryEntity> list = mc.world.getEntitiesByClass(JerryEntity.class,
                    mc.player.getBoundingBox().expand(32), e -> true);
            if (list.isEmpty()) return;
            JerryEntity j = list.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player))).get();

            int h = j.getHappiness(), x = 10, y = 10;
            int color = h > 60 ? 0xFF55FF55 : h > 30 ? 0xFFFFFF55 : 0xFFFF5555;
            String mood = h > 60 ? "Feliz" : h > 30 ? "Aburrido" : h > 15 ? "Enojado" : "¡PELIGRO!";
            ctx.fill(x - 1, y - 1, x + 101, y + 9, 0xFF000000);
            ctx.fill(x, y, x + 100, y + 8, 0xFF333333);
            ctx.fill(x, y, x + h, y + 8, color);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("Jerry: " + mood), x, y + 12, 0xFFFFFF);
        });
    }
}
