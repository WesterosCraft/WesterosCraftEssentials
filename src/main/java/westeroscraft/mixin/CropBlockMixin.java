package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (WesterosCraftConfig.disableCropGrowth) {
            ci.cancel();
        }
    }

    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void onIsRandomlyTicking(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (WesterosCraftConfig.disableCropGrowth) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void onCanSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WesterosCraftConfig.cropSurviveAny) {
            cir.setReturnValue(true);
        }
    }
}
