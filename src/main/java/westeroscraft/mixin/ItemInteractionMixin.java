package westeroscraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.restriction.ItemRestrictionManager;

/**
 * Intercepts item use and block interaction on the server side.
 * Delegates to ItemRestrictionManager for group-aware permission checks.
 */
@Mixin(ServerPlayerGameMode.class)
public class ItemInteractionMixin {

    /**
     * Right-click in air or on an entity — "use" mode.
     */
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void wce$checkItemUse(ServerPlayer player, Level level, ItemStack stack,
                                   InteractionHand hand,
                                   CallbackInfoReturnable<InteractionResult> cir) {
        if (stack.isEmpty()) return;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (ItemRestrictionManager.checkAndDeny(player, itemId, "use")) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    /**
     * Right-click on a block — "interact" mode.
     * Checks both the held item and the block being clicked (for container_menu / block rules).
     */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void wce$checkItemInteract(ServerPlayer player, Level level, ItemStack stack,
                                        InteractionHand hand, BlockHitResult hitResult,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        // Item restriction (only relevant when holding something)
        if (!stack.isEmpty()) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (ItemRestrictionManager.checkAndDeny(player, itemId, "interact")) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
        }

        // Block restriction — works with an empty hand too
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        String blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
        boolean hasMenu = blockState.getMenuProvider(level, pos) != null;
        if (ItemRestrictionManager.checkAndDenyBlock(player, blockId, hasMenu)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
