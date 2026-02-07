package westeroscraft;

import net.fabricmc.api.ClientModInitializer;
import westeroscraft.commands.PTimeCommand;

public class WesterosCraftEssentialsClient implements ClientModInitializer {
    public static WesterosCraftEssentialsClient INSTANCE;
    public long time;
    public boolean enabledTime = false;

    @Override
    public void onInitializeClient() {
        PTimeCommand.register();
        INSTANCE = this;
    }
}
