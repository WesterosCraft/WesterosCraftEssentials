package westeroscraft.mount;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import westeroscraft.LuckPermsIntegration;
import westeroscraft.config.WesterosCraftConfig;

/**
 * Registers and handles the /mount command.
 */
public class MountCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mount")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();

                    // Check if command is run by a player
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(Component.literal("This command can only be used by players."));
                        return 0;
                    }

                    // Check if mount system is enabled
                    if (!WesterosCraftConfig.mount.enabled) {
                        source.sendFailure(Component.literal("The mount command is disabled."));
                        return 0;
                    }

                    // Check permission (fail-open)
                    if (!LuckPermsIntegration.hasPermission(player, WesterosCraftConfig.mount.permission)) {
                        source.sendFailure(Component.literal("You don't have permission to use this command."));
                        return 0;
                    }

                    // If player already has a mount, despawn it first
                    if (MountManager.hasMount(player)) {
                        MountManager.despawnMount(player);
                    }

                    // Spawn the mount
                    MountManager.spawnMount(player);

                    source.sendSuccess(() -> Component.literal("Your mount has been summoned!"), false);
                    return 1;
                }));
    }
}
