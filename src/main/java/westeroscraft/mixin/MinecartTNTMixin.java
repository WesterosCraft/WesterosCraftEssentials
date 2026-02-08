package westeroscraft.mixin;

import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(MinecartTNT.class)
public abstract class MinecartTNTMixin {
    protected MinecartTNTMixin() {}

    @Inject(method = "explode*", at = @At("HEAD"), cancellable=true)
    private void doExplode(double radius, CallbackInfo ci) {
        if (WesterosCraftConfig.disableTNTExplode) {
            ci.cancel();
        }
    }
}
