package westeroscraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WesterosCraftConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = Path.of("config/westeroscraft-essentials.json");

    public static boolean disableIceMelt = false;
    public static boolean disableSnowMelt = false;
    public static boolean snowLayerSurviveAny = false;

    private static class ConfigData {
        boolean disableIceMelt = false;
        boolean disableSnowMelt = false;
        boolean snowLayerSurviveAny = false;
    }

    public static void load() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());

            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                disableIceMelt = data.disableIceMelt;
                disableSnowMelt = data.disableSnowMelt;
                snowLayerSurviveAny = data.snowLayerSurviveAny;
                LOGGER.info("Loaded config: disableIceMelt={}, disableSnowMelt={}, snowLayerSurviveAny={}", disableIceMelt, disableSnowMelt, snowLayerSurviveAny);
            } else {
                saveDefaults();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    private static void saveDefaults() {
        try {
            ConfigData data = new ConfigData();
            String json = GSON.toJson(data);
            Files.writeString(CONFIG_FILE, json);
            LOGGER.info("Created default config file");
        } catch (IOException e) {
            LOGGER.error("Failed to create default config", e);
        }
    }
}
