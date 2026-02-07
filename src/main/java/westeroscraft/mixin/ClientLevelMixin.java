package westeroscraft.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.WesterosCraftEssentialsClient;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelMixin {

    @Inject(at = @At("RETURN"), method = "getDayTime", cancellable = true)
    @Environment(EnvType.CLIENT)
    public void getDayTime(CallbackInfoReturnable<Long> cir) {
        if(WesterosCraftEssentialsClient.INSTANCE.enabledTime) {
            cir.setReturnValue(WesterosCraftEssentialsClient.INSTANCE.time);
        } else cir.cancel();
    }
}
