package westeroscraft.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import westeroscraft.WesterosCraftEssentialsClient;

public class PWeatherCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(ClientCommandManager.literal("pweather")
            .then(ClientCommandManager.literal(EWeather.CLEAR.string).executes((context) -> setWeather( 0f, 0f)))
            .then(ClientCommandManager.literal(EWeather.RAIN.string).executes((context) -> setWeather(1f, 0f)))
            .then(ClientCommandManager.literal(EWeather.THUNDER.string).executes((context) -> setWeather(1f, 1f)))
            .then(ClientCommandManager.literal(EWeather.RESET.string).executes((context) -> resetWeather()))
        ));
    }

    public static int resetWeather() {
        WesterosCraftEssentialsClient.INSTANCE.enabledWeather = false;
        return 1;
    }

    public static int setWeather(float rainLevel, float thunderLevel) {
        WesterosCraftEssentialsClient.INSTANCE.enabledWeather = true;
        WesterosCraftEssentialsClient.INSTANCE.rainLevel = rainLevel;
        WesterosCraftEssentialsClient.INSTANCE.thunderLevel = thunderLevel;
        return 1;
    }
}
