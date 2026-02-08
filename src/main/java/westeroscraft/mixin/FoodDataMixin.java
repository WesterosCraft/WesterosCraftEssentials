package westeroscraft.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.config.WesterosCraftConfig;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Shadow
    private int foodLevel;

    protected FoodDataMixin() {}

    @Inject(method = "tick", at = @At("HEAD"))
    private void doTick(Player player, CallbackInfo ci) {
        if(WesterosCraftConfig.disableHunger) {
            foodLevel = 20;
        }
    }
}
