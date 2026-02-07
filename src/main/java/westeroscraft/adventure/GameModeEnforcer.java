package westeroscraft.adventure;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.LuckPermsIntegration;
import westeroscraft.config.WesterosCraftConfig;

/**
 * Enforces Adventure mode for players with the wcessentials.forceadventuremode permission.
 * On login: Players are always forced to ADVENTURE mode.
 * On dimension change: Only CREATIVE is blocked; ADVENTURE, SURVIVAL, and SPECTATOR are allowed.
 * Uses LuckPerms for permission checking (fail-closed - no effect if LuckPerms not installed).
 */
public class GameModeEnforcer {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final String PERMISSION = "wcessentials.forceadventuremode";

    /**
     * Enforce adventure mode for a player if they have the permission and are in a blocked game mode.
     *
     * @param player The player to check and potentially enforce
     * @param context Description of when enforcement is happening (e.g., "JOIN", "DIMENSION_CHANGE")
     */
    public static void enforceGameMode(ServerPlayer player, String context) {
        if (!WesterosCraftConfig.forceAdventureMode) {
            return;
        }

        if (!LuckPermsIntegration.hasPermissionStrict(player, PERMISSION)) {
            return;
        }

        GameType currentMode = player.gameMode.getGameModeForPlayer();

        // On JOIN: always force to ADVENTURE
        // On DIMENSION_CHANGE: only force if in CREATIVE
        boolean shouldEnforce;
        if ("DIMENSION_CHANGE".equals(context)) {
            shouldEnforce = (currentMode == GameType.CREATIVE);
        } else {
            // LOGIN and other contexts: always enforce
            shouldEnforce = (currentMode != GameType.ADVENTURE);
        }

        if (shouldEnforce) {
            LOGGER.info("Player {} forced from {} to ADVENTURE mode ({})",
                player.getDisplayName().getString(),
                currentMode.getName(),
                context);
            player.setGameMode(GameType.ADVENTURE);
        }
    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            enforceGameMode(player, "JOIN");
        });

        LOGGER.info("GameModeEnforcer initialized");
    }
}
