package westeroscraft.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.adventure.GameModeEnforcer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void onDimensionChange(DimensionTransition transition,
                                  CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer returnedPlayer = cir.getReturnValue();
        if (returnedPlayer != null) {
            // Enforce game mode after successful dimension change
            GameModeEnforcer.enforceGameMode(returnedPlayer, "DIMENSION_CHANGE");
        }
    }
}
