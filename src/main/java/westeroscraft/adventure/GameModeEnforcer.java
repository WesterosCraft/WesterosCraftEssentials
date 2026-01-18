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
 * Uses LuckPerms for permission checking (fail-closed - no effect if LuckPerms not installed).
 */
public class GameModeEnforcer {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final String PERMISSION = "wcessentials.forceadventuremode";

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!WesterosCraftConfig.forceAdventureMode) {
                return;
            }
            ServerPlayer player = handler.getPlayer();
            if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
                if (LuckPermsIntegration.hasPermissionStrict(player, PERMISSION)) {
                    LOGGER.info("Player {} forced to ADVENTURE mode", player.getDisplayName().getString());
                    player.setGameMode(GameType.ADVENTURE);
                }
            }
        });

        LOGGER.info("GameModeEnforcer initialized");
    }
}
