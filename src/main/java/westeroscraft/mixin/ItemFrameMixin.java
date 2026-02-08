package westeroscraft.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {
    protected ItemFrameMixin() {}


    @Inject(method = "interact", at = @At("HEAD"), cancellable=true)
    private void doInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        if ((!player.isCreative()) && WesterosCraftConfig.blockHangingItemChanges) {
            ci.setReturnValue(InteractionResult.CONSUME);
        }
    }
    @Inject(method = "hurt", at = @At("HEAD"), cancellable=true)
    private void doHurt(DamageSource src, float damage, CallbackInfoReturnable<Boolean> ci) {
        if ((!src.isCreativePlayer()) && WesterosCraftConfig.blockHangingItemChanges) {
            ci.setReturnValue(Boolean.FALSE);
        }
    }
}
