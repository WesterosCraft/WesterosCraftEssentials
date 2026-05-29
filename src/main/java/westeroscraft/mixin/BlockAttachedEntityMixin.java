package westeroscraft.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import westeroscraft.restriction.ItemRestrictionManager;

/**
 * Group-aware protection for painting removal.
 *
 * When a player attacks a painting, the same item-restrictions.json rule that governs the
 * minecraft:painting item (evaluated in "attack" mode) decides whether the removal is allowed.
 * This means a single rule covers both placing a painting (caught as the held item in "interact"
 * mode by {@link ItemInteractionMixin}) and breaking one (caught here in "attack" mode), even when
 * the player's hand is empty.
 *
 * Targets BlockAttachedEntity because that is where hurt() is declared — neither Painting nor
 * HangingEntity overrides it. The logic is gated to Painting so item frames (which have their own
 * ItemFrameMixin) and leash knots are unaffected.
 */
@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin extends Entity {
    protected BlockAttachedEntityMixin(EntityType<?> et, Level l) {
        super(et, l);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void wce$checkPaintingRemoval(DamageSource src, float damage,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!((Entity) this instanceof Painting)) return;
        Entity attacker = src.getEntity();
        if (!(attacker instanceof ServerPlayer player)) return;

        String paintingId = BuiltInRegistries.ITEM.getKey(Items.PAINTING).toString();
        if (ItemRestrictionManager.checkAndDeny(player, paintingId, "attack")) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
