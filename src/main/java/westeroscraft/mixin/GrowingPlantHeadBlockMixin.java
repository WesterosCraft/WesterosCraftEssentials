package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;

import java.util.Random;

@Mixin(GrowingPlantHeadBlock.class)
public abstract class GrowingPlantHeadBlockMixin {
    protected GrowingPlantHeadBlockMixin() {}


    @Inject(method = "randomTick", at = @At("HEAD"), cancellable=true)
    private void doRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
        if (WesterosCraftConfig.disablePlantGrowFade) {
            ci.cancel();
        }
    }

    @Inject(method = "isRandomlyTicking(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable=true)
    private void doIsRandomlyTicking(BlockState bs, CallbackInfoReturnable<Boolean> ci) {
        if (WesterosCraftConfig.disablePlantGrowFade) {
            ci.setReturnValue(false);
        }
    }
}
