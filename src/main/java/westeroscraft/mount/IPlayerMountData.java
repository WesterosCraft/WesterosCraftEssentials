package westeroscraft.mount;

import java.util.UUID;

/**
 * Interface for accessing mount data stored on players via mixin.
 */
public interface IPlayerMountData {

    boolean westeroscraft$hasMount();

    void westeroscraft$setHasMount(boolean value);

    UUID westeroscraft$getMountUuid();

    void westeroscraft$setMountUuid(UUID uuid);
}
