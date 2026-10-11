package com.jerrymod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jerrymod.entity.JerryEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Cerebro de Jerry: trucos locales + IA (API de Anthropic) para pedir objetos. */
public final class JerryBrain {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static final Set<String> BLOCKED = Set.of(
            "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block",
            "minecraft:command_block_minecart", "minecraft:structure_block", "minecraft:jigsaw",
            "minecraft:barrier", "minecraft:debug_stick", "minecraft:bedrock", "minecraft:spawner",
            "minecraft:end_portal_frame", "minecraft:structure_void", "minecraft:light");

    private static final int MAX_ITEMS = 2304; // 36 stacks de 64

    public static void handle(ServerPlayerEntity player, String raw) {
        String text = raw.trim();
        JerryEntity jerry = nearest(player);
        if (jerry == null) {
            player.sendMessage(Text.literal("§7No hay ningún Jerry cerca (32 bloques)."), true);
            return;
        }
        String low = norm(text);

        // Trucos locales (funcionan aunque no tengas API key)
        if (low.contains("la pata")) {
            jerry.doTrick(JerryEntity.TRICK_PAW);
            jerry.addHappiness(2);
            say(player, "*levanta la pata* ¡Guau!");
            return;
        }
        if (low.contains("la vuelta") || low.contains("gira")) {
            jerry.doTrick(JerryEntity.TRICK_SPIN);
            jerry.addHappiness(2);
            say(player, "*da una vuelta* ¡Guau guau!");
            return;
        }
        if (low.contains("sientate") || low.contains("sentado")) {
            jerry.setSitting(true);
            jerry.setInSittingPose(true);
            say(player, "*se sienta*");
            return;
        }

        JerryConfig cfg = JerryConfig.get();
        if (cfg.apiKey.isBlank()) {
            say(player, "Guau... (no te entiendo: pon tu API key en config/jerrymod.json)");
            return;
        }

        String system = "Eres Jerry, un perro de Minecraft leal, juguetón y un poco raro. Tu felicidad es "
                + jerry.getHappiness() + "/100 (si es baja, responde seco o gruñón). "
                + "Responde SOLO con un JSON, sin texto extra ni markdown: "
                + "{\"say\":\"frase corta de perro, max 15 palabras\","
                + "\"action\":\"give|paw|spin|sit|stand|none\","
                + "\"item\":\"id de Minecraft 1.20.1 o null\",\"count\":numero}. "
                + "Si el jugador pide un objeto usa action give con el id exacto en ingles "
                + "(ej. madera de abedul = minecraft:birch_log, tablones de abedul = minecraft:birch_planks, "
                + "diamantes = minecraft:diamond). Si no pide cantidad usa 1.";

        CompletableFuture
                .supplyAsync(() -> ask(cfg, system, text))
                .whenComplete((out, err) -> player.server.execute(() -> apply(player, jerry, out, err)));
    }

    private static String ask(JerryConfig cfg, String system, String text) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", cfg.model);
            body.addProperty("max_tokens", 300);
            body.addProperty("system", system);
            JsonArray msgs = new JsonArray();
            JsonObject m = new JsonObject();
            m.addProperty("role", "user");
            m.addProperty("content", text);
            msgs.add(m);
            body.add("messages", msgs);

            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages"))
                    .header("x-api-key", cfg.apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .timeout(Duration.ofSeconds(25))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) throw new RuntimeException("HTTP " + r.statusCode());
            JsonObject o = JsonParser.parseString(r.body()).getAsJsonObject();
            return o.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    private static void apply(ServerPlayerEntity player, JerryEntity jerry, String out, Throwable err) {
        if (jerry.isRemoved()) return;
        if (err != null || out == null) {
            say(player, "*ladra confundido* (error al hablar con la IA)");
            return;
        }
        try {
            String s = out.substring(out.indexOf('{'), out.lastIndexOf('}') + 1);
            JsonObject j = JsonParser.parseString(s).getAsJsonObject();
            String say = str(j, "say");
            String action = str(j, "action").toLowerCase(Locale.ROOT);
            if (!say.isEmpty()) say(player, say);
            switch (action) {
                case "give" -> give(player, jerry, str(j, "item"), j.has("count") && !j.get("count").isJsonNull()
                        ? j.get("count").getAsInt() : 1);
                case "paw" -> jerry.doTrick(JerryEntity.TRICK_PAW);
                case "spin" -> jerry.doTrick(JerryEntity.TRICK_SPIN);
                case "sit" -> { jerry.setSitting(true); jerry.setInSittingPose(true); }
                case "stand" -> { jerry.setSitting(false); jerry.setInSittingPose(false); }
                default -> { }
            }
        } catch (Exception e) {
            say(player, "*ladra confundido*");
        }
    }

    private static void give(ServerPlayerEntity player, JerryEntity jerry, String id, int count) {
        if (jerry.getHappiness() < 25) {
            say(player, "Grrr... no tengo ganas. Acaríciame primero.");
            return;
        }
        Identifier ident = Identifier.tryParse(id);
        if (ident == null || !Registries.ITEM.containsId(ident) || BLOCKED.contains(ident.toString())
                || Registries.ITEM.get(ident) == Items.AIR) {
            say(player, "No sé qué es eso... *inclina la cabeza*");
            return;
        }
        Item item = Registries.ITEM.get(ident);
        int left = Math.max(1, Math.min(count, MAX_ITEMS));
        int total = left;
        while (left > 0) {
            int n = Math.min(left, item.getMaxCount());
            ItemStack stack = new ItemStack(item, n);
            player.getInventory().insertStack(stack);
            if (!stack.isEmpty()) player.dropItem(stack, false);
            left -= n;
        }
        jerry.addHappiness(3);
        jerry.hearts(3);
        player.sendMessage(Text.literal("§7Jerry te dio " + total + "x ").append(new ItemStack(item).getName()), false);
    }

    private static JerryEntity nearest(ServerPlayerEntity p) {
        List<JerryEntity> l = p.getWorld().getEntitiesByClass(JerryEntity.class, p.getBoundingBox().expand(32), e -> true);
        return l.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
    }

    private static void say(ServerPlayerEntity p, String s) {
        p.sendMessage(Text.literal("§6<Jerry> §f" + s), false);
    }

    private static String str(JsonObject j, String k) {
        return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsString() : "";
    }

    private static String norm(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
