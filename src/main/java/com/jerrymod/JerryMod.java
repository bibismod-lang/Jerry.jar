package com.jerrymod;

import com.jerrymod.entity.JerryEntity;
import com.jerrymod.entity.JerryMonsterEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class JerryMod implements ModInitializer {
    public static final String ID = "jerrymod";
    public static final Identifier TALK = new Identifier(ID, "talk");
    private static final String KIT_TAG = "jerrymod_kit";

    public static final EntityType<JerryEntity> JERRY = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(ID, "jerry"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, JerryEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 0.85f)).trackRangeChunks(10).build());

    public static final EntityType<JerryMonsterEntity> MONSTER = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(ID, "jerry_monster"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, JerryMonsterEntity::new)
                    .dimensions(EntityDimensions.fixed(1.5f, 4.0f)).trackRangeChunks(10).build());

    // Mismos colores que el huevo del lobo vanilla
    public static final Item JERRY_EGG = Registry.register(Registries.ITEM,
            new Identifier(ID, "jerry_spawn_egg"),
            new SpawnEggItem(JERRY, 0xD7D3D3, 0xCEAF96, new FabricItemSettings()));

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(JERRY, WolfEntity.createWolfAttributes());
        FabricDefaultAttributeRegistry.register(MONSTER, JerryMonsterEntity.createAttributes());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(e -> e.add(JERRY_EGG));

        // Botón / pantalla del cliente -> servidor
        ServerPlayNetworking.registerGlobalReceiver(TALK, (server, player, handler, buf, sender) -> {
            String text = buf.readString(256);
            server.execute(() -> JerryBrain.handle(player, text));
        });

        // Chat normal: cualquier mensaje que empiece con "Jerry"
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            String text = message.getSignedContent();
            if (text.toLowerCase(Locale.ROOT).startsWith("jerry")) {
                sender.server.execute(() -> JerryBrain.handle(sender, text));
            }
        });

        // Cofre inicial: una sola vez por jugador/mundo
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (p.age > 40 && !p.getCommandTags().contains(KIT_TAG)) {
                    p.addCommandTag(KIT_TAG);
                    StarterKit.place(p);
                }
            }
        });
    }
}
