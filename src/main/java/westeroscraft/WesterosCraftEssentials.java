package westeroscraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.adventure.GameModeEnforcer;
import westeroscraft.config.WesterosCraftConfig;
import westeroscraft.restore.AutoRestoreManager;

public class WesterosCraftEssentials implements ModInitializer {
	public static final String MOD_ID = "westeroscraft-essentials";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WesterosCraftConfig.load();

		// Initialize game mode enforcer
		GameModeEnforcer.init();

		// Register server tick event for auto-restore processing
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
