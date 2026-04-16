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

        // Example: block specific items. Inactive until items are added.
        ItemRestrictionRule itemExample = new ItemRestrictionRule();
        itemExample.items = new ArrayList<>();   // empty — inactive until items are added
        itemExample.modes = new ArrayList<>(List.of("use", "interact"));
        itemExample.denied_groups = new ArrayList<>(List.of("default"));
        itemExample.allowed_groups = new ArrayList<>(List.of("moderator", "admin"));
        itemExample.message = "You don't have permission to use this item.";
        config.rules.add(itemExample);

        // Example: block guests from opening any container (chest, furnace, barrel, etc.).
        // Set denied_groups to activate (e.g. ["guest"]).
        ItemRestrictionRule containerExample = new ItemRestrictionRule();
        containerExample.container_menu = true;
        containerExample.modes = new ArrayList<>(List.of("interact"));
        containerExample.denied_groups = new ArrayList<>();  // empty — inactive until groups are added
        containerExample.allowed_groups = new ArrayList<>();
        containerExample.message = "Guests cannot open containers.";
        config.rules.add(containerExample);

        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
            LOGGER.info("Created default item-restrictions.json");
        } catch (IOException e) {
            LOGGER.error("Failed to write default item-restrictions.json", e);
        }
        return config;
    }
}
