package westeroscraft.mixin;

import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Mixin invoker to access the private setVariantAndMarkings method on Horse.
 */
@Mixin(Horse.class)
public interface HorseInvoker {

    @Invoker("setVariantAndMarkings")
    void invokeSetVariantAndMarkings(Variant variant, Markings markings);
}
