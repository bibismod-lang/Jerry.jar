package com.jerrymod.client;

import com.jerrymod.JerryMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pantalla con el botón para hablar con Jerry. */
public class JerryScreen extends Screen {
    private TextFieldWidget field;

    public JerryScreen() { super(Text.literal("Hablar con Jerry")); }

    @Override
    protected void init() {
        field = new TextFieldWidget(textRenderer, width / 2 - 150, height / 2 - 20, 300, 20, Text.empty());
        field.setMaxLength(200);
        field.setSuggestion("Jerry dame 64 de madera de abedul");
        field.setChangedListener(s -> field.setSuggestion(s.isEmpty() ? "Jerry dame 64 de madera de abedul" : ""));
        addDrawableChild(field);
        setInitialFocus(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("Hablar con Jerry"), b -> send())
                .dimensions(width / 2 - 75, height / 2 + 8, 150, 20).build());
    }

    private void send() {
        String t = field.getText().trim();
        if (!t.isEmpty()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(t);
            ClientPlayNetworking.send(JerryMod.TALK, buf);
        }
        close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { send(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 42, 0xFFFFFF);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
