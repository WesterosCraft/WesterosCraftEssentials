package westeroscraft.restore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import westeroscraft.config.WesterosCraftConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class AutoRestoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("westeroscraft-essentials");

    private static int tickCount = 0;
    private static long secondCount = 0;

    public enum BlockType {
        DOOR, GATE, TRAPDOOR
    }

    private static class PendingRestore {
        final Level level;
        final BlockPos pos;

        PendingRestore(Level level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(level, pos);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PendingRestore other) {
                return this.level == other.level && this.pos.equals(other.pos);
            }
            return false;
        }
    }

    private static class RestoreInfo {
        final long restoreAtSecond;
        final boolean originalOpen;

        RestoreInfo(long restoreAtSecond, boolean originalOpen) {
            this.restoreAtSecond = restoreAtSecond;
            this.originalOpen = originalOpen;
        }
    }

    private static final Map<PendingRestore, RestoreInfo> pendingDoors = new HashMap<>();
    private static final Map<PendingRestore, RestoreInfo> pendingGates = new HashMap<>();
    private static final Map<PendingRestore, RestoreInfo> pendingTrapDoors = new HashMap<>();

    public static boolean isAutoRestoreBlock(Block block, BlockType type) {
        if (!WesterosCraftConfig.autoRestore.enabled) {
            return false;
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();

        return switch (type) {
            case DOOR -> WesterosCraftConfig.autoRestore.allDoors ||
                    WesterosCraftConfig.autoRestore.doors.contains(blockId);
            case GATE -> WesterosCraftConfig.autoRestore.allGates ||
                    WesterosCraftConfig.autoRestore.gates.contains(blockId);
            case TRAPDOOR -> WesterosCraftConfig.autoRestore.allTrapDoors ||
                    WesterosCraftConfig.autoRestore.trapDoors.contains(blockId);
        };
    }

    public static void scheduleRestore(Level level, BlockPos pos, boolean originalOpen, BlockType type) {
        PendingRestore key = new PendingRestore(level, pos);
        Map<PendingRestore, RestoreInfo> map = getMapForType(type);

        RestoreInfo existing = map.get(key);
        if (existing == null) {
            // New restore - schedule it
            long restoreAt = secondCount + WesterosCraftConfig.autoRestore.delaySeconds;
            map.put(key, new RestoreInfo(restoreAt, originalOpen));
            LOGGER.debug("Scheduled {} restore at {} for {} (original={})", type, pos, restoreAt, originalOpen);
        } else {
            // Already pending - just reset the timer, keep original state
            long restoreAt = secondCount + WesterosCraftConfig.autoRestore.delaySeconds;
            map.put(key, new RestoreInfo(restoreAt, existing.originalOpen));
            LOGGER.debug("Reset {} restore timer at {} to {}", type, pos, restoreAt);
        }
    }

    public static void cancelRestore(Level level, BlockPos pos, BlockType type) {
        PendingRestore key = new PendingRestore(level, pos);
        Map<PendingRestore, RestoreInfo> map = getMapForType(type);

        if (map.remove(key) != null) {
            LOGGER.debug("Cancelled {} restore at {}", type, pos);
        }
    }

    private static Map<PendingRestore, RestoreInfo> getMapForType(BlockType type) {
        return switch (type) {
            case DOOR -> pendingDoors;
            case GATE -> pendingGates;
            case TRAPDOOR -> pendingTrapDoors;
        };
    }

    public static void tick() {
        tickCount++;
        if (tickCount >= 20) {
            tickCount = 0;
            secondCount++;
            processRestores(false);
        }
    }

    public static void forceAllRestores() {
        LOGGER.info("Forcing all pending restores on server shutdown");
        processRestores(true);
    }

    private static void processRestores(boolean force) {
        processDoorRestores(force);
        processGateRestores(force);
        processTrapDoorRestores(force);
    }

    private static void processDoorRestores(boolean force) {
        Iterator<Map.Entry<PendingRestore, RestoreInfo>> iter = pendingDoors.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<PendingRestore, RestoreInfo> entry = iter.next();
            PendingRestore pending = entry.getKey();
            RestoreInfo info = entry.getValue();

            if (force || info.restoreAtSecond <= secondCount) {
                BlockState state = pending.level.getBlockState(pending.pos);
                Block block = state.getBlock();

                if (block instanceof DoorBlock doorBlock && isAutoRestoreBlock(block, BlockType.DOOR)) {
                    boolean currentOpen = state.getValue(DoorBlock.OPEN);
                    if (currentOpen != info.originalOpen) {
                        doorBlock.setOpen(null, pending.level, state, pending.pos, info.originalOpen);
                        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                        LOGGER.info("Auto-restored door {} at {} to open={}", blockId, pending.pos, info.originalOpen);
                    }
                }
                iter.remove();
            }
        }
    }

    private static void processGateRestores(boolean force) {
        Iterator<Map.Entry<PendingRestore, RestoreInfo>> iter = pendingGates.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<PendingRestore, RestoreInfo> entry = iter.next();
            PendingRestore pending = entry.getKey();
            RestoreInfo info = entry.getValue();

            if (force || info.restoreAtSecond <= secondCount) {
                BlockState state = pending.level.getBlockState(pending.pos);
                Block block = state.getBlock();

                if (block instanceof FenceGateBlock && isAutoRestoreBlock(block, BlockType.GATE)) {
                    boolean currentOpen = state.getValue(FenceGateBlock.OPEN);
                    if (currentOpen != info.originalOpen) {
                        state = state.setValue(FenceGateBlock.OPEN, info.originalOpen);
                        pending.level.setBlock(pending.pos, state, 10);
                        pending.level.levelEvent(null, info.originalOpen ? 1008 : 1014, pending.pos, 0);
                        pending.level.gameEvent(null, info.originalOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pending.pos);
                        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                        LOGGER.info("Auto-restored fence gate {} at {} to open={}", blockId, pending.pos, info.originalOpen);
                    }
                }
                iter.remove();
            }
        }
    }

    private static void processTrapDoorRestores(boolean force) {
        Iterator<Map.Entry<PendingRestore, RestoreInfo>> iter = pendingTrapDoors.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<PendingRestore, RestoreInfo> entry = iter.next();
            PendingRestore pending = entry.getKey();
            RestoreInfo info = entry.getValue();

            if (force || info.restoreAtSecond <= secondCount) {
                BlockState state = pending.level.getBlockState(pending.pos);
                Block block = state.getBlock();

                if (block instanceof TrapDoorBlock && isAutoRestoreBlock(block, BlockType.TRAPDOOR)) {
                    boolean currentOpen = state.getValue(TrapDoorBlock.OPEN);
                    if (currentOpen != info.originalOpen) {
                        state = state.setValue(TrapDoorBlock.OPEN, info.originalOpen);
                        pending.level.setBlock(pending.pos, state, 10);
                        // Play trapdoor sound (levelEvent 1037 = wooden trapdoor open, 1007 = wooden trapdoor close)
                        pending.level.levelEvent(null, info.originalOpen ? 1037 : 1007, pending.pos, 0);
                        pending.level.gameEvent(null, info.originalOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pending.pos);
                        if (state.getValue(TrapDoorBlock.WATERLOGGED)) {
                            pending.level.scheduleTick(pending.pos, Fluids.WATER, Fluids.WATER.getTickDelay(pending.level));
                        }
                        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                        LOGGER.info("Auto-restored trapdoor {} at {} to open={}", blockId, pending.pos, info.originalOpen);
                    }
                }
                iter.remove();
            }
        }
    }
}
