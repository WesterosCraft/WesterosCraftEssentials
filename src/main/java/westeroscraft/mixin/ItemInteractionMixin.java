package westeroscraft.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
                                   CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (stack.isEmpty()) return;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (ItemRestrictionManager.checkAndDeny(player, itemId, "use")) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    /**
     * Right-click on a block — "interact" mode.
     */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void wce$checkItemInteract(ServerPlayer player, Level level, ItemStack stack,
                                        InteractionHand hand, BlockHitResult hitResult,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (stack.isEmpty()) return;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (ItemRestrictionManager.checkAndDeny(player, itemId, "interact")) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
