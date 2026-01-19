package westeroscraft.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import westeroscraft.mount.IPlayerMountData;

import java.util.UUID;

/**
 * Mixin to persist mount data on players across server restarts.
 */
@Mixin(Player.class)
public abstract class PlayerMountDataMixin implements IPlayerMountData {

    @Unique
    private boolean westeroscraft_hasMount = false;

    @Unique
    private UUID westeroscraft_mountUuid = null;

    @Override
    public boolean westeroscraft$hasMount() {
        return westeroscraft_hasMount;
    }

    @Override
    public void westeroscraft$setHasMount(boolean value) {
        this.westeroscraft_hasMount = value;
    }

    @Override
    public UUID westeroscraft$getMountUuid() {
        return westeroscraft_mountUuid;
    }

    @Override
    public void westeroscraft$setMountUuid(UUID uuid) {
        this.westeroscraft_mountUuid = uuid;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void westeroscraft_writeData(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("westeroscraft.hasMount", westeroscraft_hasMount);
        if (westeroscraft_mountUuid != null) {
            compound.putUUID("westeroscraft.mountUuid", westeroscraft_mountUuid);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void westeroscraft_readData(CompoundTag compound, CallbackInfo ci) {
        westeroscraft_hasMount = compound.getBoolean("westeroscraft.hasMount");
        if (compound.hasUUID("westeroscraft.mountUuid")) {
            westeroscraft_mountUuid = compound.getUUID("westeroscraft.mountUuid");
        } else {
            westeroscraft_mountUuid = null;
        }
    }
}
