package westeroscraft.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import westeroscraft.WesterosCraftEssentialsClient;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    @Override
    public float getRainLevel(float d) {
        if(WesterosCraftEssentialsClient.INSTANCE.enabledWeather) {
            return WesterosCraftEssentialsClient.INSTANCE.rainLevel;
        }
        return super.getRainLevel(d);
    }

    @Override
    public float getThunderLevel(float d) {
        if(WesterosCraftEssentialsClient.INSTANCE.enabledWeather) {
            return WesterosCraftEssentialsClient.INSTANCE.thunderLevel;
        }
        return super.getThunderLevel(d);
    }
}
