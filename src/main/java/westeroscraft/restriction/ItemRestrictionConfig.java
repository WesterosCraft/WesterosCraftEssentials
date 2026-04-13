package westeroscraft.restriction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Root POJO for config/item-restrictions.json.
 * Loaded and reloaded by ItemRestrictionManager.
 */
public class ItemRestrictionConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/item-restrictions.json");

    public List<ItemRestrictionRule> rules = new ArrayList<>();

    public static ItemRestrictionConfig load() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                ItemRestrictionConfig config = GSON.fromJson(json, ItemRestrictionConfig.class);
                if (config == null) config = new ItemRestrictionConfig();
                if (config.rules == null) config.rules = new ArrayList<>();
                return config;
            } else {
                return createDefault();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load item-restrictions.json", e);
            return new ItemRestrictionConfig();
        }
    }

    private static ItemRestrictionConfig createDefault() {
        ItemRestrictionConfig config = new ItemRestrictionConfig();

        // Write an example rule with an empty items list so it has no effect until the admin fills it in.
        // Copy this block and populate "items" to activate a restriction.
        ItemRestrictionRule example = new ItemRestrictionRule();
        example.items = new ArrayList<>();   // empty — rule is inactive until items are added
        example.modes = new ArrayList<>(List.of("use", "interact"));
        example.denied_groups = new ArrayList<>(List.of("default"));
        example.allowed_groups = new ArrayList<>(List.of("moderator", "admin"));
        example.message = "You don't have permission to use this item.";
        config.rules.add(example);

        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
            LOGGER.info("Created default item-restrictions.json");
        } catch (IOException e) {
            LOGGER.error("Failed to write default item-restrictions.json", e);
        }
        return config;
    }
}
