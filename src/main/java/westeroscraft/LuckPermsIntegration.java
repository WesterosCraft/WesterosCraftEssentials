package westeroscraft;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.platform.PlayerAdapter;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optional integration with LuckPerms for permission checks.
 * Gracefully handles the case when LuckPerms is not installed.
 * Uses the recommended PlayerAdapter API for online player permission checks.
 */
public class LuckPermsIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final String LUCKPERMS_MOD_ID = "luckperms";
    private static boolean initialized = false;
    private static PlayerAdapter<ServerPlayer> playerAdapter = null;

    /**
     * Check if LuckPerms is available on the server.
     * Uses FabricLoader to check if the mod is loaded, then lazily initializes
     * the PlayerAdapter on first use.
     */
    public static boolean isAvailable() {
        if (!FabricLoader.getInstance().isModLoaded(LUCKPERMS_MOD_ID)) {
            return false;
        }

        if (!initialized) {
            initialized = true;
            try {
                LuckPerms luckPerms = LuckPermsProvider.get();
                playerAdapter = luckPerms.getPlayerAdapter(ServerPlayer.class);
                LOGGER.info("LuckPerms integration enabled");
            } catch (IllegalStateException e) {
                LOGGER.warn("LuckPerms is loaded but not yet available: {}", e.getMessage());
                initialized = false; // Allow retry on next call
                return false;
            }
        }
        return playerAdapter != null;
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
        return checkPermission(player, permission);
    }

    /**
     * Check if a player has a specific permission using the PlayerAdapter API.
     * This is the recommended way to check permissions for online players.
     *
     * @param player     The server player to check
     * @param permission The permission node to check
     * @return true if player has permission, false otherwise
     */
    private static boolean checkPermission(ServerPlayer player, String permission) {
        try {
            CachedPermissionData permissionData = playerAdapter.getPermissionData(player);
            return permissionData.checkPermission(permission).asBoolean();
        } catch (Exception e) {
            LOGGER.warn("Error checking permission {} for {}: {}", permission, player.getGameProfile().getName(), e.getMessage());
            return false;
        }
    }
}
