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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Core manager for item and block interaction restrictions.
 *
 * Config is loaded from config/item-restrictions.json and indexed at startup (and on reload).
 * On each player interaction the lookup is O(1) per item/block ID.
 *
 * LuckPerms is a soft dependency — if absent, only wildcard ("*") denied_groups rules apply.
 */
public class ItemRestrictionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final List<String> ALL_MODES = List.of("use", "interact", "attack");

    // Lazily resolved — QueryOptions.nonContextual() calls LuckPermsProvider.get(),
    // which throws if invoked before LuckPerms is loaded.
    private static QueryOptions getNonContextual() {
        return QueryOptions.nonContextual();
    }

    /** Items/blocks whose IDs begin with "!" are treated as regex patterns. */
    private static final String PATTERN_PREFIX = "!";

    private static final String MODE_ALL = "all";

    private record PatternEntry(Pattern pattern, ItemRestrictionRule rule) {}

    /**
     * A container_menu rule plus its precompiled exception matchers. A clicked block matching
     * any exact ID or pattern is exempt from the rule (e.g. chests allowed, everything else blocked).
     */
    private record ContainerMenuEntry(ItemRestrictionRule rule, Set<String> exceptExact, List<Pattern> exceptPatterns) {
        boolean isExcepted(String blockId) {
            if (exceptExact.contains(blockId)) return true;
            for (Pattern p : exceptPatterns) {
                if (p.matcher(blockId).matches()) return true;
            }
            return false;
        }
    }

    /**
     * Immutable snapshot of all lookup tables. Held in a single volatile field so that
     * a reload swaps all tables atomically.
     */
    private record IndexSnapshot(
        // Item restrictions — keyed by mode then item ID
        Map<String, Map<String, List<ItemRestrictionRule>>> exact,
        Map<String, List<PatternEntry>> patterns,
        // Block restrictions — keyed by block ID (interact-mode only)
        Map<String, List<ItemRestrictionRule>> blockExact,
        List<PatternEntry> blockPatterns,
        // container_menu rules — fire whenever the clicked block has a MenuProvider
        List<ContainerMenuEntry> containerMenuRules
    ) {}

    private static volatile IndexSnapshot index = new IndexSnapshot(
        new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new ArrayList<>()
    );

    public static void initialize() {
        loadAndIndex("loaded");
        LOGGER.info("ItemRestrictionManager initialized");
    }

    public static void reload() {
        loadAndIndex("reloaded");
    }

    private static void loadAndIndex(String verb) {
        ItemRestrictionConfig config = ItemRestrictionConfig.load();

        Map<String, Map<String, List<ItemRestrictionRule>>> newExact = new HashMap<>();
        Map<String, List<PatternEntry>> newPatterns = new HashMap<>();
        for (String mode : ALL_MODES) {
            newExact.put(mode, new HashMap<>());
            newPatterns.put(mode, new ArrayList<>());
        }

        Map<String, List<ItemRestrictionRule>> newBlockExact = new HashMap<>();
        List<PatternEntry> newBlockPatterns = new ArrayList<>();
        List<ContainerMenuEntry> newContainerMenuRules = new ArrayList<>();

        for (ItemRestrictionRule rule : config.rules) {
            boolean hasItems = rule.items != null && !rule.items.isEmpty();
            boolean hasBlocks = rule.blocks != null && !rule.blocks.isEmpty();
            if (!hasItems && !hasBlocks && !rule.container_menu) {
                LOGGER.warn("Item restriction rule has no items, blocks, or container_menu defined, skipping.");
                continue;
            }

            List<String> modes = expandModes(rule.modes);

            // --- item rules ---
            if (hasItems) {
                for (String item : rule.items) {
                    if (item.startsWith(PATTERN_PREFIX)) {
                        String regex = item.substring(PATTERN_PREFIX.length());
                        Pattern compiled;
                        try {
                            compiled = Pattern.compile(regex);
                        } catch (PatternSyntaxException e) {
                            LOGGER.warn("Invalid item pattern '{}' in item-restrictions.json, skipping: {}", item, e.getMessage());
                            continue;
                        }
                        PatternEntry entry = new PatternEntry(compiled, rule);
                        for (String mode : modes) {
                            List<PatternEntry> list = newPatterns.get(mode);
                            if (list != null) list.add(entry);
                        }
                    } else {
                        for (String mode : modes) {
                            Map<String, List<ItemRestrictionRule>> itemMap = newExact.get(mode);
                            if (itemMap != null) {
                                itemMap.computeIfAbsent(item, k -> new ArrayList<>()).add(rule);
                            }
                        }
                    }
                }
            }

            // --- block rules (interact-mode only) ---
            if (hasBlocks && modes.contains("interact")) {
                for (String block : rule.blocks) {
                    if (block.startsWith(PATTERN_PREFIX)) {
                        String regex = block.substring(PATTERN_PREFIX.length());
                        Pattern compiled;
                        try {
                            compiled = Pattern.compile(regex);
                        } catch (PatternSyntaxException e) {
                            LOGGER.warn("Invalid block pattern '{}' in item-restrictions.json, skipping: {}", block, e.getMessage());
                            continue;
                        }
                        newBlockPatterns.add(new PatternEntry(compiled, rule));
                    } else {
                        newBlockExact.computeIfAbsent(block, k -> new ArrayList<>()).add(rule);
                    }
                }
            }

            // --- container_menu rules ---
            if (rule.container_menu && modes.contains("interact")) {
                Set<String> exceptExact = new HashSet<>();
                List<Pattern> exceptPatterns = new ArrayList<>();
                if (rule.container_menu_except != null) {
                    for (String block : rule.container_menu_except) {
                        if (block.startsWith(PATTERN_PREFIX)) {
                            String regex = block.substring(PATTERN_PREFIX.length());
                            try {
                                exceptPatterns.add(Pattern.compile(regex));
                            } catch (PatternSyntaxException e) {
                                LOGGER.warn("Invalid container_menu_except pattern '{}' in item-restrictions.json, skipping: {}", block, e.getMessage());
                            }
                        } else {
                            exceptExact.add(block);
                        }
                    }
                }
                newContainerMenuRules.add(new ContainerMenuEntry(rule, exceptExact, exceptPatterns));
            }
        }

        index = new IndexSnapshot(newExact, newPatterns, newBlockExact, newBlockPatterns, newContainerMenuRules);
        LOGGER.info("ItemRestrictionManager {} ({} rule(s))", verb, config.rules.size());
    }

    private static List<String> expandModes(List<String> modes) {
        if (modes == null || modes.isEmpty() || modes.contains(MODE_ALL)) return ALL_MODES;
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
     * Check if a player is restricted from right-clicking a block.
     * Evaluates both explicit block ID rules and container_menu rules.
     * Sends the denial message and returns true if the interaction should be cancelled.
     *
     * @param blockId  registry ID of the block being clicked (e.g. "minecraft:chest")
     * @param hasMenu  true if the block has a container MenuProvider (would open a GUI)
     */
    public static boolean checkAndDenyBlock(ServerPlayer player, String blockId, boolean hasMenu) {
        ItemRestrictionRule matched = findMatchingBlockRule(player, blockId, hasMenu);
        if (matched == null) return false;
        String msg = matched.message != null ? matched.message : "You cannot interact with this block.";
        player.sendSystemMessage(Component.literal(msg));
        return true;
    }

    /**
     * Returns the first rule that restricts this player from using this item in this mode,
     * or null if no restriction applies.
     */
    private static ItemRestrictionRule findMatchingRule(ServerPlayer player, String itemId, String mode) {
        // Capture snapshot once so both tables come from the same reload generation.
        IndexSnapshot snap = index;

        // --- exact match ---
        Map<String, List<ItemRestrictionRule>> itemMap = snap.exact().get(mode);
        if (itemMap != null) {
            List<ItemRestrictionRule> rules = itemMap.get(itemId);
            if (rules != null) {
                for (ItemRestrictionRule rule : rules) {
                    if (isRuleApplied(player, rule)) return rule;
                }
            }
        }

        // --- pattern match ---
        List<PatternEntry> patterns = snap.patterns().get(mode);
        if (patterns != null) {
            for (PatternEntry entry : patterns) {
                if (entry.pattern().matcher(itemId).matches() && isRuleApplied(player, entry.rule())) {
                    return entry.rule();
                }
            }
        }

        return null;
    }

    /**
     * Returns the first block restriction rule that applies to this player and block,
     * or null if no restriction applies.
     */
    private static ItemRestrictionRule findMatchingBlockRule(ServerPlayer player, String blockId, boolean hasMenu) {
        IndexSnapshot snap = index;

        // --- container_menu rules (fired when block opens a GUI) ---
        if (hasMenu) {
            for (ContainerMenuEntry entry : snap.containerMenuRules()) {
                if (entry.isExcepted(blockId)) continue;
                if (isRuleApplied(player, entry.rule())) return entry.rule();
            }
        }

        // --- exact block ID match ---
        List<ItemRestrictionRule> rules = snap.blockExact().get(blockId);
        if (rules != null) {
            for (ItemRestrictionRule rule : rules) {
                if (isRuleApplied(player, rule)) return rule;
            }
        }

        // --- block pattern match ---
        for (PatternEntry entry : snap.blockPatterns()) {
            if (entry.pattern().matcher(blockId).matches() && isRuleApplied(player, entry.rule())) {
                return entry.rule();
            }
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
            return user.getInheritedGroups(getNonContextual())
                    .stream()
                    .anyMatch(g -> groups.contains(g.getName()));
        } catch (Exception e) {
            LOGGER.warn("LuckPerms group check failed for {}: {}", player.getGameProfile().getName(), e.getMessage());
            return false;
        }
    }
}
