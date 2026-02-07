package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.LuckPermsIntegration;
import westeroscraft.config.WesterosCraftConfig;
import westeroscraft.restore.AutoRestoreManager;

@Mixin(FenceGateBlock.class)
public class FenceGateBlockMixin {

    @Unique
    private static final ThreadLocal<Boolean> originalOpenState = new ThreadLocal<>();

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void captureOriginalState(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide && player instanceof ServerPlayer) {
            originalOpenState.set(state.getValue(FenceGateBlock.OPEN));
        }
    }

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void scheduleRestoreIfNeeded(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Boolean originalOpen = originalOpenState.get();
        originalOpenState.remove();

        if (originalOpen == null) {
            return;
        }

        FenceGateBlock gate = (FenceGateBlock) (Object) this;
        if (!AutoRestoreManager.isAutoRestoreBlock(gate, AutoRestoreManager.BlockType.GATE)) {
            return;
        }

        // Creative mode players' changes are permanent
        if (serverPlayer.isCreative()) {
            // Cancel any pending restore for this position
            AutoRestoreManager.cancelRestore(level, pos, AutoRestoreManager.BlockType.GATE);
            return;
        }

        // Check if player has the auto-restore permission
        String permission = WesterosCraftConfig.autoRestore.permission;
        if (LuckPermsIntegration.hasPermissionStrict(serverPlayer, permission)) {
            AutoRestoreManager.scheduleRestore(level, pos, originalOpen, AutoRestoreManager.BlockType.GATE);
        }
    }
}
