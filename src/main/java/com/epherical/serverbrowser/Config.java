package com.epherical.serverbrowser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Config {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path = CommonPlatform.platform.getRootConfigPath().resolve("serverbrowser.json");

    public ConfigSettings loadConfig() {
        ConfigSettings settings = null;

        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                settings = GSON.fromJson(json, ConfigSettings.class);
                return settings;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.createDirectories(CommonPlatform.platform.getRootConfigPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            settings = new ConfigSettings();
        }

        try {
            saveFile(settings);
            return settings;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public void saveFile(ConfigSettings settings) throws IOException {
        Files.writeString(path, GSON.toJson(settings), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

}
