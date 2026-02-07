package westeroscraft.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class NVCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(ClientCommandManager.literal("nv").executes((context) -> nightVision(context.getSource()))));
    }

    public static int nightVision(FabricClientCommandSource source) {
        //We check for both ServerPlayer and LocalPlayer to allow for it to be used in singleplayer.
        if(source.getEntity() instanceof ServerPlayer player){
            MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
            if(nv == null) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
                source.sendFeedback(Component.literal("Enabled Night Vision"));
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
                source.sendFeedback(Component.literal("Disabled Night Vision"));
            }
        } else if(source.getEntity() instanceof LocalPlayer player) {
            MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
            if(nv == null) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
                source.sendFeedback(Component.literal("Enabled Night Vision"));
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
                source.sendFeedback(Component.literal("Disabled Night Vision"));
            }
        }
        return 1;
    }
}
