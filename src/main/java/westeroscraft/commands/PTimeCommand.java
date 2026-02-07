package westeroscraft.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.TimeArgument;
import westeroscraft.WesterosCraftEssentialsClient;

public class PTimeCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(ClientCommandManager.literal("ptime")
            .then(ClientCommandManager.literal("reset").executes((context) -> resetTime(context.getSource())))
            .then(ClientCommandManager.literal("set")
                .then(ClientCommandManager.literal("normal").executes((context) -> resetTime(context.getSource())))
                .then(ClientCommandManager.literal("default").executes((context) -> resetTime(context.getSource())))
                .then(ClientCommandManager.literal("sunrise").executes((context) -> setTime(context.getSource(), ETime.SUNRISE.time, false)))
                .then(ClientCommandManager.literal("day").executes((context) -> setTime(context.getSource(), ETime.DAY.time, false)))
                .then(ClientCommandManager.literal("morning").executes((context) -> setTime(context.getSource(), ETime.MORNING.time, false)))
                .then(ClientCommandManager.literal("noon").executes((context) -> setTime(context.getSource(), ETime.NOON.time, false)))
                .then(ClientCommandManager.literal("afternoon").executes((context) -> setTime(context.getSource(), ETime.AFTERNOON.time, false)))
                .then(ClientCommandManager.literal("sunset").executes((context) -> setTime(context.getSource(), ETime.SUNSET.time, false)))
                .then(ClientCommandManager.literal("midnight").executes((context) -> setTime(context.getSource(), ETime.MIDNIGHT.time, false)))
                .then(ClientCommandManager.argument("time", TimeArgument.time()).executes((context) -> setTime(context.getSource(), IntegerArgumentType.getInteger(context, "time"), false)))
            )
            .then(ClientCommandManager.literal("setrelative")
                .then(ClientCommandManager.literal("ahead")
                    .then(ClientCommandManager.argument("time", TimeArgument.time()).executes((context) -> setTime(context.getSource(), IntegerArgumentType.getInteger(context, "time"), true)))
                )
                .then(ClientCommandManager.literal("behind")
                    .then(ClientCommandManager.argument("time", TimeArgument.time()).executes((context) -> setTime(context.getSource(), -IntegerArgumentType.getInteger(context, "time"), true)))
                )
            )
        ));
    }

    public static int resetTime(FabricClientCommandSource source) {
        WesterosCraftEssentialsClient.INSTANCE.enabledTime = false;
        WesterosCraftEssentialsClient.INSTANCE.time = source.getWorld().getDayTime();
        return 1;
    }

    public static int setTime(FabricClientCommandSource source, long time, boolean relative) {
        if(relative) {
            if(WesterosCraftEssentialsClient.INSTANCE.enabledTime) {
                WesterosCraftEssentialsClient.INSTANCE.time += time;
            } else {
                WesterosCraftEssentialsClient.INSTANCE.time = source.getWorld().getDayTime() + time;
            }
        } else {
            WesterosCraftEssentialsClient.INSTANCE.time = time;
        }
        WesterosCraftEssentialsClient.INSTANCE.enabledTime = true;
        return 1;
    }
}

