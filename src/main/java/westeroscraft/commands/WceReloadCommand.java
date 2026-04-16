package westeroscraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import westeroscraft.restriction.ItemRestrictionManager;

public class WceReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("wce")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(context -> {
                        ItemRestrictionManager.reload();
                        context.getSource().sendSuccess(
                            () -> Component.literal("[WCE] Item restriction rules reloaded."),
                            true
                        );
                        return 1;
                    })
                )
        );
    }
}
