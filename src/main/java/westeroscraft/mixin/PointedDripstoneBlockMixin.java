package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockMixin {

    @Inject(method = "maybeTransferFluid", at = @At("HEAD"), cancellable = true)
    private static void doMaybeTransferFluid(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f, CallbackInfo ci) {
        if (WesterosCraftConfig.disableDripstoneTransfer) {
            ci.cancel();
        }
    }
}
