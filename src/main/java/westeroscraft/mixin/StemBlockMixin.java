package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(StemBlock.class) 
public abstract class StemBlockMixin {
	protected StemBlockMixin() {}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		if (WesterosCraftConfig.disableStemGrowFade) {
			ci.cancel();
		}
	}
}
