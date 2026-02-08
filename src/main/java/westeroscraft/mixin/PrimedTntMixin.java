package westeroscraft.mixin;

import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(PrimedTnt.class) 
public abstract class PrimedTntMixin {
	protected PrimedTntMixin() {}

	@Inject(method = "explode", at = @At("HEAD"), cancellable=true)
	private void doExplode(CallbackInfo ci) {
		if (WesterosCraftConfig.disableTNTExplode) {
			ci.cancel();
		}
	}

}
