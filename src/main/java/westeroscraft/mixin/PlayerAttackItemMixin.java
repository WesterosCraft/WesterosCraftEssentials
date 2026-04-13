package westeroscraft.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.restriction.ItemRestrictionManager;

/**
 * Intercepts left-click attacks to enforce item restrictions ("attack" mode).
 * Targets ServerPlayer directly because it overrides Player.attack() — injecting
 * into Player.attack() would not fire for server-side players.
 */
@Mixin(ServerPlayer.class)
public abstract class PlayerAttackItemMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void wce$checkAttack(Entity target, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (ItemRestrictionManager.checkAndDeny(player, itemId, "attack")) {
            ci.cancel();
        }
    }
}
