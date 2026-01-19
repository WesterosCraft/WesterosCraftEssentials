package westeroscraft.mount;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.config.WesterosCraftConfig;
import westeroscraft.mixin.HorseInvoker;

import java.util.UUID;

/**
 * Manages horse lifecycle and player-horse tracking for the /mount command.
 * Mount data is persisted on players via mixin (survives server restarts).
 */
public class MountManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");

    /**
     * Initialize event handlers for mount cleanup.
     */
    public static void init() {
        // Despawn horse when player disconnects
        // Must schedule on main thread since DISCONNECT fires on Netty IO thread
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            IPlayerMountData mountData = (IPlayerMountData) player;
            UUID horseUuid = mountData.westeroscraft$getMountUuid();
            ServerLevel level = player.serverLevel();

            if (horseUuid != null) {
                server.execute(() -> {
                    if (level.getEntity(horseUuid) instanceof Horse horse) {
                        horse.discard();
                        LOGGER.info("Despawned mount {} for disconnecting player {}", horseUuid, player.getName().getString());
                    }
                    // Clear the mount data
                    mountData.westeroscraft$setHasMount(false);
                    mountData.westeroscraft$setMountUuid(null);
                });
            }
        });

        // Despawn horse when player changes dimension
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            // Only despawn if player actually changed to a different dimension
            if (origin != destination) {
                despawnMount(player, origin);
            }
        });

        LOGGER.info("Mount system initialized");
    }

    /**
     * Check if a player has an active mount.
     */
    public static boolean hasMount(ServerPlayer player) {
        return ((IPlayerMountData) player).westeroscraft$hasMount();
    }

    /**
     * Spawn a tamed, saddled horse near the player.
     */
    public static void spawnMount(ServerPlayer player) {
        if (!WesterosCraftConfig.mount.enabled) {
            return;
        }

        ServerLevel level = player.serverLevel();

        // Create horse entity using constructor
        Horse horse = new Horse(EntityType.HORSE, level);

        // Randomize the horse's appearance
        RandomSource random = level.getRandom();
        Variant[] variants = Variant.values();
        Markings[] markings = Markings.values();
        ((HorseInvoker) horse).invokeSetVariantAndMarkings(
                variants[random.nextInt(variants.length)],
                markings[random.nextInt(markings.length)]
        );

        // Position horse at player's location with player's rotation
        horse.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);

        // Tame the horse and set owner
        horse.setTamed(true);
        horse.setOwnerUUID(player.getUUID());

        // Equip saddle
        horse.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.NEUTRAL);

        // Add horse to the world
        level.addFreshEntity(horse);

        // Track the horse on player data (persisted)
        IPlayerMountData mountData = (IPlayerMountData) player;
        mountData.westeroscraft$setHasMount(true);
        mountData.westeroscraft$setMountUuid(horse.getUUID());

        LOGGER.info("Spawned mount for player {} (horse UUID: {})", player.getName().getString(), horse.getUUID());
    }

    /**
     * Despawn a player's mount from a specific level.
     */
    public static void despawnMount(ServerPlayer player, ServerLevel level) {
        IPlayerMountData mountData = (IPlayerMountData) player;
        UUID horseUuid = mountData.westeroscraft$getMountUuid();

        if (horseUuid == null) {
            return;
        }

        // Find and remove the horse entity
        if (level.getEntity(horseUuid) instanceof Horse horse) {
            horse.discard();
            LOGGER.info("Despawned mount {} for player {}", horseUuid, player.getName().getString());
        }

        // Clear the mount data
        mountData.westeroscraft$setHasMount(false);
        mountData.westeroscraft$setMountUuid(null);
    }

    /**
     * Despawn a player's mount using their current level.
     */
    public static void despawnMount(ServerPlayer player) {
        despawnMount(player, player.serverLevel());
    }
}
