package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(SugarCaneBlock.class) 
public abstract class SugarCaneBlockMixin {
	protected SugarCaneBlockMixin() {}

	@Inject(method = "tick", at = @At("HEAD"), cancellable=true)
    private void doTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		if (WesterosCraftConfig.disableSugarCaneGrowFade) {
			ci.cancel();
		}
	}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		if (WesterosCraftConfig.disableSugarCaneGrowFade) {
			ci.cancel();
		}
	}
	
	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable=true)
    private void doCanSurvive(BlockState bs, LevelReader lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftConfig.sugarCaneSurviveAny) {
			ci.setReturnValue(true);
		}
	}
}
