package westeroscraft.restriction;

import java.util.ArrayList;
import java.util.List;

/**
 * One rule entry in item-restrictions.json.
 * A rule can restrict items (by ID), specific blocks (by ID), or any block that opens a
 * container menu — mix and match as needed. All three fields are optional; a rule needs at
 * least one of items, blocks, or container_menu = true to be valid.
 */
public class ItemRestrictionRule {
    /** Item IDs this rule applies to (e.g. "minecraft:flint_and_steel"). Prefix with "!" for regex. */
    public List<String> items = new ArrayList<>();

    /**
     * Block IDs this rule applies to on right-click (e.g. "minecraft:chest").
     * Prefix with "!" for regex. Fires regardless of what the player is holding.
     * Only evaluated for "interact" mode.
     */
    public List<String> blocks = new ArrayList<>();

    /**
     * If true, this rule fires when the player right-clicks any block that would open a
     * container menu (chest, furnace, barrel, hopper, shulker box, dispenser, dropper, etc.).
     * Works with an empty hand. Only evaluated for "interact" mode.
     */
    public boolean container_menu = false;

    /**
     * Interaction modes to restrict: "use" (right-click in air/on entity),
     * "interact" (right-click on block), "attack" (left-click entity), or "all".
     */
    public List<String> modes = new ArrayList<>(List.of("all"));

    /**
     * LuckPerms group names that are denied. Use "*" to deny all players.
     * Checked after allowed_groups — allowed_groups always wins.
     */
    public List<String> denied_groups = new ArrayList<>(List.of("*"));

    /**
     * LuckPerms group names that are exempt from this restriction.
     * If a player is in any of these groups they are always allowed, regardless of denied_groups.
     */
    public List<String> allowed_groups = new ArrayList<>();

    /** Message sent to the player when the action is denied. */
    public String message = "You cannot use this item.";
}
