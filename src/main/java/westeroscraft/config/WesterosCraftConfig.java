package westeroscraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WesterosCraftConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = Path.of("config/westeroscraft-essentials.json");

    public static boolean disableIceMelt = false;
    public static boolean disableSnowMelt = false;
    public static boolean snowLayerSurviveAny = false;
    public static boolean doPreventLeafDecay = false;
    public static boolean disableCropGrowth = false;
    public static boolean cropSurviveAny = false;
    public static AutoRestoreConfig autoRestore = new AutoRestoreConfig();

    public static class AutoRestoreConfig {
        public boolean enabled = true;
        public int delaySeconds = 30;
        public String permission = "westeroscraft.autorestore";
        public boolean allDoors = false;
        public boolean allGates = false;
        public boolean allTrapDoors = false;
        public List<String> doors = new ArrayList<>();
        public List<String> gates = new ArrayList<>();
        public List<String> trapDoors = new ArrayList<>();
    }

    private static class ConfigData {
        boolean disableIceMelt = true;
        boolean disableSnowMelt = true;
        boolean snowLayerSurviveAny = true;
        boolean doPreventLeafDecay = true;
        boolean disableCropGrowth = true;
        boolean cropSurviveAny = true;
        AutoRestoreConfig autoRestore = new AutoRestoreConfig();
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
                doPreventLeafDecay = data.doPreventLeafDecay;
                disableCropGrowth = data.disableCropGrowth;
                cropSurviveAny = data.cropSurviveAny;
                if (data.autoRestore != null) {
                    autoRestore = data.autoRestore;
                }
                LOGGER.info("Loaded config: disableIceMelt={}, disableSnowMelt={}, snowLayerSurviveAny={}, doPreventLeafDecay={}, disableCropGrowth={}, cropSurviveAny={}",
                    disableIceMelt, disableSnowMelt, snowLayerSurviveAny, doPreventLeafDecay, disableCropGrowth, cropSurviveAny);
                LOGGER.info("AutoRestore config: enabled={}, delaySeconds={}, permission={}",
                    autoRestore.enabled, autoRestore.delaySeconds, autoRestore.permission);
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
