package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(VineBlock.class) 
public abstract class VineBlockMixin {
	protected VineBlockMixin() {}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		if (WesterosCraftConfig.disableVineGrowFade) {
			ci.cancel();
		}
	}

	@Inject(method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable=true)	
    private void doCanSurvive(BlockState bs, LevelReader lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (WesterosCraftConfig.vineSurviveAny) {
			ci.setReturnValue(true);
		}
	}
}
