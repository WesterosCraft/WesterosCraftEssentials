package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;


@Mixin(FrostedIceBlock.class)
public abstract class FrostedIceBlockMixin {
    protected FrostedIceBlockMixin() {}

    @Inject(method = "tick", at = @At("HEAD"), cancellable=true)
    private void doTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
        if (WesterosCraftConfig.disableIceMelt) {
            ci.cancel();
        }
    }

    @Inject(method = "slightlyMelt", at = @At("HEAD"), cancellable=true)
    private void doSlightlyMelt(BlockState bs, Level lvl, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (WesterosCraftConfig.disableIceMelt) {
            ci.setReturnValue(Boolean.FALSE);
        }
    }
}
