package westeroscraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.adventure.GameModeEnforcer;
import westeroscraft.config.WesterosCraftConfig;
import westeroscraft.mount.MountCommand;
import westeroscraft.mount.MountManager;
import westeroscraft.restore.AutoRestoreManager;

public class WesterosCraftEssentials implements ModInitializer {
	public static final String MOD_ID = "westeroscraft-essentials";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WesterosCraftConfig.load();

		GameModeEnforcer.init();
		MountManager.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			MountCommand.register(dispatcher);
		});

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
