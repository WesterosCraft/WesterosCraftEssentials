package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin {
    protected FarmBlockMixin() {}

    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void doTurnToDirt(Entity entity, BlockState blockState, Level level, BlockPos blockPos, CallbackInfo ci) {
        if (WesterosCraftConfig.disableFarmStomping) {
            ci.cancel();
        }
    }
}
