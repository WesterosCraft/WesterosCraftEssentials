package westeroscraft.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(HangingEntity.class)
public abstract class HangingEntityMixin extends BlockAttachedEntity {
    protected HangingEntityMixin(EntityType<? extends HangingEntity> et, Level l) {
        super(et, l);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if ((!player.isCreative()) && WesterosCraftConfig.blockHangingItemChanges) {
            return InteractionResult.CONSUME;
        }
        return super.interact(player, hand);
    }
}
