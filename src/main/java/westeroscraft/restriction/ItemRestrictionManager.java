package westeroscraft.restriction;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core manager for item use restrictions.
 *
 * Config is loaded from config/item-restrictions.json and indexed at startup (and on reload).
 * On each player interaction the lookup is O(1) per item ID.
 *
 * LuckPerms is a soft dependency — if absent, only wildcard ("*") denied_groups rules apply.
 */
public class ItemRestrictionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final List<String> ALL_MODES = List.of("use", "interact", "attack");

    private static final QueryOptions NON_CONTEXTUAL = QueryOptions.nonContextual();

    // mode → itemId → list of matching rules (volatile for safe publication on reload)
    private static volatile Map<String, Map<String, List<ItemRestrictionRule>>> modeIndex = new HashMap<>();

    public static void initialize() {
        loadAndIndex("loaded");
        LOGGER.info("ItemRestrictionManager initialized");
    }

    public static void reload() {
        loadAndIndex("reloaded");
    }

    private static void loadAndIndex(String verb) {
        ItemRestrictionConfig config = ItemRestrictionConfig.load();
        Map<String, Map<String, List<ItemRestrictionRule>>> newIndex = new HashMap<>();
        for (String mode : ALL_MODES) {
            newIndex.put(mode, new HashMap<>());
        }

        for (ItemRestrictionRule rule : config.rules) {
            List<String> modes = expandModes(rule.modes);
            for (String mode : modes) {
                Map<String, List<ItemRestrictionRule>> itemMap = newIndex.get(mode);
                if (itemMap == null) continue;
                for (String itemId : rule.items) {
                    itemMap.computeIfAbsent(itemId, k -> new ArrayList<>()).add(rule);
                }
            }
        }

        modeIndex = newIndex;
        LOGGER.info("ItemRestrictionManager {} ({} rule(s))", verb, config.rules.size());
    }

    private static List<String> expandModes(List<String> modes) {
        if (modes == null || modes.isEmpty() || modes.contains("all")) return ALL_MODES;
        return modes;
    }

    /**
     * Check if a player is restricted from using the given item in the given mode.
     * Sends the denial message to the player and returns true if the action should be cancelled.
     */
    public static boolean checkAndDeny(ServerPlayer player, String itemId, String mode) {
        ItemRestrictionRule matched = findMatchingRule(player, itemId, mode);
        if (matched == null) return false;
        String msg = matched.message != null ? matched.message : "You cannot use this item.";
        player.sendSystemMessage(Component.literal(msg));
        return true;
    }

    /**
     * Check if a player is restricted without sending any message.
     * Used for hotbar/cursor checks where sending a message on every tick would be spammy.
     */
    public static boolean isRestricted(ServerPlayer player, String itemId, String mode) {
        return findMatchingRule(player, itemId, mode) != null;
    }

    /**
     * Returns the first rule that restricts this player from using this item in this mode,
     * or null if no restriction applies.
     */
    private static ItemRestrictionRule findMatchingRule(ServerPlayer player, String itemId, String mode) {
        Map<String, List<ItemRestrictionRule>> itemMap = modeIndex.get(mode);
        if (itemMap == null) return null;
        List<ItemRestrictionRule> rules = itemMap.get(itemId);
        if (rules == null || rules.isEmpty()) return null;
        for (ItemRestrictionRule rule : rules) {
            if (isRuleApplied(player, rule)) return rule;
        }
        return null;
    }

    /**
     * Evaluate whether a rule restricts this player.
     * allowed_groups always takes precedence over denied_groups.
     */
    private static boolean isRuleApplied(ServerPlayer player, ItemRestrictionRule rule) {
        // Exemption check first — allowed_groups always win
        if (rule.allowed_groups != null && !rule.allowed_groups.isEmpty() && isInAnyGroup(player, rule.allowed_groups)) {
            return false;
        }
        if (rule.denied_groups == null || rule.denied_groups.isEmpty()) {
            return false;
        }
        // Wildcard — deny everyone regardless of LuckPerms
        if (rule.denied_groups.contains("*")) {
            return true;
        }
        // Group-specific deny — requires LuckPerms
        return isInAnyGroup(player, rule.denied_groups);
    }

    /**
     * Returns true if the player belongs to any of the named LuckPerms groups
     * (checked non-contextually so world/server context does not interfere).
     * Returns false if LuckPerms is not installed or the player's data is not cached.
     */
    private static boolean isInAnyGroup(ServerPlayer player, List<String> groups) {
        if (groups == null || groups.isEmpty()) return false;
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) return false;
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(player.getUUID());
            if (user == null) return false;
            return user.getInheritedGroups(NON_CONTEXTUAL)
                    .stream()
                    .anyMatch(g -> groups.contains(g.getName()));
        } catch (Exception e) {
            LOGGER.warn("LuckPerms group check failed for {}: {}", player.getGameProfile().getName(), e.getMessage());
            return false;
        }
    }
}
