package westeroscraft;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Optional integration with LuckPerms for permission checks.
 * Gracefully handles the case when LuckPerms is not installed.
 */
public class LuckPermsIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static boolean available = false;
    private static boolean checked = false;

    /**
     * Check if LuckPerms is available on the server.
     */
    public static boolean isAvailable() {
        if (!checked) {
            checked = true;
            try {
                Class.forName("net.luckperms.api.LuckPermsProvider");
                LuckPermsProvider.get();
                available = true;
                LOGGER.info("LuckPerms integration enabled");
            } catch (ClassNotFoundException | IllegalStateException e) {
                available = false;
                LOGGER.info("LuckPerms not found, permission integration disabled");
            }
        }
        return available;
    }

    /**
     * Check if a player has a specific permission.
     * Returns true if LuckPerms is not available (fail-open).
     *
     * @param player     The server player to check
     * @param permission The permission node to check
     * @return true if player has permission or LuckPerms is unavailable
     */
    public static boolean hasPermission(ServerPlayer player, String permission) {
        if (!isAvailable()) {
            return true; // Fail-open when LuckPerms not installed
        }
        return hasPermissionStrict(player.getUUID(), permission);
    }

    /**
     * Check if a player has a specific permission.
     * Returns false if LuckPerms is not available (fail-closed).
     *
     * @param player     The server player to check
     * @param permission The permission node to check
     * @return true only if player explicitly has the permission
     */
    public static boolean hasPermissionStrict(ServerPlayer player, String permission) {
        if (!isAvailable()) {
            return false; // Fail-closed when LuckPerms not installed
        }
        return hasPermissionStrict(player.getUUID(), permission);
    }

    /**
     * Check if a player (by UUID) has a specific permission.
     *
     * @param playerUuid The player's UUID
     * @param permission The permission node to check
     * @return true if player has permission, false otherwise
     */
    public static boolean hasPermissionStrict(UUID playerUuid, String permission) {
        if (!isAvailable()) {
            return false;
        }
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(playerUuid);
            if (user == null) {
                return false;
            }
            return user.getCachedData()
                    .getPermissionData(QueryOptions.nonContextual())
                    .checkPermission(permission)
                    .asBoolean();
        } catch (Exception e) {
            LOGGER.warn("Error checking permission {} for {}: {}", permission, playerUuid, e.getMessage());
            return false;
        }
    }
}
