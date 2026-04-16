package westeroscraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.adventure.GameModeEnforcer;
import westeroscraft.commands.WceReloadCommand;
import westeroscraft.config.WesterosCraftConfig;
import westeroscraft.restore.AutoRestoreManager;
import westeroscraft.restriction.ItemRestrictionManager;

public class WesterosCraftEssentials implements ModInitializer {
	public static final String MOD_ID = "westeroscraft-essentials";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WesterosCraftConfig.load();

		ItemRestrictionManager.initialize();

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) ItemRestrictionManager.reload();
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			WceReloadCommand.register(dispatcher)
		);

		GameModeEnforcer.init();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			AutoRestoreManager.tick();
		});

		// Force all restores when server is stopping
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			AutoRestoreManager.forceAllRestores();
		});

		LOGGER.info("WesterosCraft Essentials initialized");
	}
}
