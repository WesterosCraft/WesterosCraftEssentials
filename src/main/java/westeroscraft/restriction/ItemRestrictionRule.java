package westeroscraft.restriction;

import java.util.ArrayList;
import java.util.List;

/**
 * One rule entry in item-restrictions.json.
 * Defines which items are restricted, for which groups, in which interaction modes.
 */
public class ItemRestrictionRule {
    /** Item IDs this rule applies to (e.g. "minecraft:flint_and_steel") */
    public List<String> items = new ArrayList<>();

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
