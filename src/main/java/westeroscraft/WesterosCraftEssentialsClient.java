package westeroscraft;

import net.fabricmc.api.ClientModInitializer;
import westeroscraft.commands.NVCommand;
import westeroscraft.commands.PTimeCommand;
import westeroscraft.commands.PWeatherCommand;

public class WesterosCraftEssentialsClient implements ClientModInitializer {
    public static WesterosCraftEssentialsClient INSTANCE;
    public long time;
    public boolean enabledTime = false;

    public float rainLevel;
    public float thunderLevel;
    public boolean enabledWeather = false;

    @Override
    public void onInitializeClient() {
        PTimeCommand.register();
        PWeatherCommand.register();
        NVCommand.register();
        INSTANCE = this;
    }
}
