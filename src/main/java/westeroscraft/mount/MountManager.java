package westeroscraft.mount;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.config.WesterosCraftConfig;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages horse lifecycle and player-horse tracking for the /mount command.
 */
public class MountManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");
    private static final ConcurrentHashMap<UUID, UUID> playerToHorse = new ConcurrentHashMap<>();

    /**
     * Initialize event handlers for mount cleanup.
     */
    public static void init() {
        // Despawn horse when player disconnects
        // Must schedule on main thread since DISCONNECT fires on Netty IO thread
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerUuid = handler.getPlayer().getUUID();
            ServerLevel level = handler.getPlayer().serverLevel();
            server.execute(() -> despawnMount(playerUuid, level));
        });

        // Despawn horse when player changes dimension
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            // Only despawn if player actually changed to a different dimension
            if (origin != destination) {
                UUID playerUuid = player.getUUID();
                despawnMount(playerUuid, origin);
            }
        });

        LOGGER.info("Mount system initialized");
    }

    /**
     * Check if a player has an active mount.
     */
    public static boolean hasMount(ServerPlayer player) {
        return playerToHorse.containsKey(player.getUUID());
    }

    /**
     * Spawn a tamed, saddled horse for the player and mount them on it.
     */
    public static void spawnMount(ServerPlayer player) {
        if (!WesterosCraftConfig.mount.enabled) {
            return;
        }

        ServerLevel level = player.serverLevel();

        // Create horse entity using constructor
        Horse horse = new Horse(EntityType.HORSE, level);

        // Position horse at player's location with player's rotation
        horse.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);

        // Tame the horse and set owner
        horse.setTamed(true);
        horse.setOwnerUUID(player.getUUID());

        // Equip saddle
        horse.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.NEUTRAL);

        // Add horse to the world
        level.addFreshEntity(horse);

        // Track the horse
        playerToHorse.put(player.getUUID(), horse.getUUID());

        // Mount the player on the horse
        player.startRiding(horse);

        LOGGER.info("Spawned mount for player {} (horse UUID: {})", player.getName().getString(), horse.getUUID());
    }

    /**
     * Despawn a player's mount from any level.
     */
    public static void despawnMount(UUID playerUuid, ServerLevel level) {
        UUID horseUuid = playerToHorse.remove(playerUuid);
        if (horseUuid == null) {
            return;
        }

        // Find and remove the horse entity
        if (level.getEntity(horseUuid) instanceof Horse horse) {
            horse.discard();
            LOGGER.info("Despawned mount {} for player {}", horseUuid, playerUuid);
        }
    }

    /**
     * Despawn a player's mount using their ServerPlayer reference.
     */
    public static void despawnMount(ServerPlayer player) {
        despawnMount(player.getUUID(), player.serverLevel());
    }
}
