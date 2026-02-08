package westeroscraft.mixin;

import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin {

    protected FallingBlockMixin() {}

    @Inject(method = "isFree", at = @At("HEAD"), cancellable=true)
    private static void doIsFree(BlockState blockState, CallbackInfoReturnable<Boolean> ci) {
        if (WesterosCraftConfig.disableFallingBlocks) {
            ci.setReturnValue(Boolean.FALSE);
        }
    }
}
