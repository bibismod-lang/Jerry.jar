package com.jerrymod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** config/jerrymod.json  ->  { "apiKey": "sk-ant-...", "model": "claude-haiku-5-5" } */
public class JerryConfig {
    public String apiKey = "";
    public String model = "claude-haiku-5-5";

    private static JerryConfig instance;

    public static synchronized JerryConfig get() {
        if (instance != null) return instance;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path path = FabricLoader.getInstance().getConfigDir().resolve("jerrymod.json");
        try {
            if (Files.exists(path)) {
                instance = gson.fromJson(Files.readString(path), JerryConfig.class);
            } else {
                instance = new JerryConfig();
                Files.writeString(path, gson.toJson(instance));
            }
        } catch (Exception e) {
            instance = new JerryConfig();
        }
        if (instance.apiKey == null) instance.apiKey = "";
        return instance;
    }
}
