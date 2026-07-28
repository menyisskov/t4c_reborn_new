package com.perso.T4C.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.BiPredicate;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Manages collision detection for the game world.
 */
public class CollisionManager {
    private static final Logger log = LoggerFactory.getLogger(CollisionManager.class);
    private static CollisionManager instance;
    private static final long DYNAMIC_LOG_COOLDOWN_MS = 500L;

    private CollisionReader collisionReader;
    private DynamicCollisionProvider dynamicProvider;
    private BiPredicate<Integer, Integer> playerPassabilityProvider;
    private long lastDynamicLogAt = 0L;

    /**
     * Isometric walls are authored as diagonal bands that are two or more
     * collision rows thick. The upper row of such a band is the visible foot of
     * the wall, where the puppet is drawn standing, so it must stay walkable —
     * only the rows behind it block.
     *
     * <p>Two conditions keep this from opening holes in ordinary obstacles. The
     * band must be at least two rows thick at this column (a lone blocking row
     * is a real barrier and keeps blocking), and the wall front must continue
     * sideways. Because these bands run diagonally, the neighbouring column
     * carries its own foot one row higher or lower, so both columns are checked
     * across that vertical slack. A staircase corner or an isolated blocking
     * cell has no such continuation and stays solid.
     *
     * @return {@code true} when the blocking tile is a wall foot, hence walkable.
     */
    private boolean isWalkableWallFoot(int gridX, int gridY) {
        return isWallFootColumn(gridX, gridY)
                && (continuesWallFront(gridX - 1, gridY) || continuesWallFront(gridX + 1, gridY));
    }

    /** True when the neighbouring column carries a wall foot within one row. */
    private boolean continuesWallFront(int gridX, int gridY) {
        return isWallFootColumn(gridX, gridY)
                || isWallFootColumn(gridX, gridY - 1)
                || isWallFootColumn(gridX, gridY + 1);
    }

    /** Top edge of a band at least two rows thick in this single column. */
    private boolean isWallFootColumn(int gridX, int gridY) {
        return rawBlocks(gridX, gridY)
                && rawBlocks(gridX, gridY + 1)
                && !rawBlocks(gridX, gridY - 1);
    }

    private boolean rawBlocks(int gridX, int gridY) {
        if (collisionReader == null) {
            return false;
        }
        if (!isWithinCollisionMap(gridX, gridY)) {
            return true;
        }
        return collisionReader.hasCollision(gridX, gridY);
    }

    /** Static tile test with the walkable wall-foot rule applied. */
    private boolean staticBlocksAtGrid(int gridX, int gridY) {
        return rawBlocks(gridX, gridY) && !isWalkableWallFoot(gridX, gridY);
    }
/**
 * Interface for DynamicCollisionProvider.
 */

    @FunctionalInterface
    public interface DynamicCollisionProvider {
        boolean blocks(float worldX, float worldY);
    }

    private CollisionManager() {
    }

    public static CollisionManager getInstance() {
        if (instance == null) {
            instance = new CollisionManager();
        }
        return instance;
    }

    public void initialize(CollisionReader reader) {
        this.collisionReader = reader;
        log.info("CollisionManager initialized with {}x{} map", reader.getWidth(), reader.getHeight());
    }

    public void clear() {
        this.collisionReader = null;
        this.dynamicProvider = null;
        this.playerPassabilityProvider = null;
        log.info("CollisionManager cleared");
    }

    public void setDynamicProvider(DynamicCollisionProvider provider) {
        this.dynamicProvider = provider;
    }

    /** Supplies player-only exceptions to static collision (e.g. teleport source tiles). */
    public void setPlayerPassabilityProvider(BiPredicate<Integer, Integer> provider) {
        this.playerPassabilityProvider = provider;
    }

    /**
     * Checks if world position (in pixels) has collision
     */
    public boolean hasCollision(float worldX, float worldY) {
        if (dynamicProvider != null && dynamicProvider.blocks(worldX, worldY)) {
            maybeLogDynamicBlock(worldX, worldY);
            return true;
        }
        if (collisionReader == null) {
            return false; // No collision data loaded
        }

        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);

        return staticBlocksAtGrid(gridX, gridY);
    }

    /**
     * Checks if grid coordinates have collision
     */
    public boolean hasCollisionAtGrid(int gridX, int gridY) {
        if (dynamicProvider != null) {
            float worldX = gridX * GRID_W + (GRID_W * 0.5f);
            float worldY = gridY * GRID_H + (GRID_H * 0.5f);
            if (dynamicProvider.blocks(worldX, worldY)) {
                maybeLogDynamicBlock(worldX, worldY);
                return true;
            }
        }
        return staticBlocksAtGrid(gridX, gridY);
    }

    public boolean hasStaticCollision(float worldX, float worldY) {
        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);
        return staticBlocksAtGrid(gridX, gridY);
    }

    public boolean hasStaticCollisionAtGrid(int gridX, int gridY) {
        return staticBlocksAtGrid(gridX, gridY);
    }

    public boolean hasStaticCollisionAtGrid(long gridX, long gridY) {
        return hasStaticCollisionAtGrid((int) gridX, (int) gridY);
    }

    public boolean hasStaticCollisionNearGrid(int gridX, int gridY, int clearanceTiles) {
        if (clearanceTiles <= 0) {
            return hasStaticCollisionAtGrid(gridX, gridY);
        }
        for (int y = gridY - clearanceTiles; y <= gridY + clearanceTiles; y++) {
            for (int x = gridX - clearanceTiles; x <= gridX + clearanceTiles; x++) {
                if (hasStaticCollisionAtGrid(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasCollisionNearGrid(int gridX, int gridY, int clearanceTiles) {
        if (clearanceTiles <= 0) {
            return hasCollisionAtGrid(gridX, gridY);
        }
        for (int y = gridY - clearanceTiles; y <= gridY + clearanceTiles; y++) {
            for (int x = gridX - clearanceTiles; x <= gridX + clearanceTiles; x++) {
                if (hasCollisionAtGrid(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasCollisionNear(float worldX, float worldY, int clearanceTiles) {
        if (clearanceTiles <= 0) {
            return hasCollision(worldX, worldY);
        }
        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);
        return hasCollisionNearGrid(gridX, gridY, clearanceTiles);
    }

    /**
     * Gets collision value at world position
     */
    public int getCollisionValue(float worldX, float worldY) {
        if (collisionReader == null) {
            return 0;
        }

        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);

        if (!isWithinCollisionMap(gridX, gridY)) {
            return CollisionType.ABSOLUTE.getValue();
        }

        return collisionReader.getCollision(gridX, gridY);
    }

    public boolean blocksLineOfSight(float worldX, float worldY) {
        if (dynamicProvider != null && dynamicProvider.blocks(worldX, worldY)) {
            return true;
        }
        if (collisionReader == null) {
            return false;
        }
        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);
        return !isWithinCollisionMap(gridX, gridY) || collisionReader.blocksLineOfSight(gridX, gridY);
    }

    /**
     * Checks if a movement from (x1, y1) to (x2, y2) is valid
     */
    public boolean canMove(float fromX, float fromY, float toX, float toY) {
        return canMoveInternal(fromX, fromY, toX, toY, false);
    }

    public boolean canMoveForPlayer(float fromX, float fromY, float toX, float toY) {
        int targetX = (int) (toX / GRID_W);
        int targetY = (int) (toY / GRID_H);
        if (playerPassabilityProvider == null || !playerPassabilityProvider.test(targetX, targetY)) {
            return canMove(fromX, fromY, toX, toY);
        }
        return canMoveInternal(fromX, fromY, toX, toY, true);
    }

    public boolean isPlayerPassableTile(int gridX, int gridY) {
        return playerPassabilityProvider != null && playerPassabilityProvider.test(gridX, gridY);
    }

    private boolean canMoveInternal(float fromX, float fromY, float toX, float toY, boolean player) {
        if (collisionReader == null) {
            return true; // No collision data = allow movement
        }

        // Check destination
        if ((player ? hasPlayerCollision(toX, toY) : hasCollision(toX, toY))) {
            return false;
        }

        // Check intermediate grid positions when a frame crosses one or more
        // cells. X and Y have different pixel sizes in T4C (32x16), so using
        // GRID_W as a single Euclidean threshold can tunnel through a complete
        // collision row during a slow frame.
        float dx = toX - fromX;
        float dy = toY - fromY;
        int steps = Math.max(
                (int) Math.ceil(Math.abs(dx) / GRID_W),
                (int) Math.ceil(Math.abs(dy) / GRID_H));
        for (int i = 1; i < steps; i++) {
            float t = (float) i / steps;
            float checkX = fromX + dx * t;
            float checkY = fromY + dy * t;
            if ((player ? hasPlayerCollision(checkX, checkY) : hasCollision(checkX, checkY))) {
                return false;
            }
        }

        return true;
    }

    public boolean hasPlayerCollision(float worldX, float worldY) {
        if (dynamicProvider != null && dynamicProvider.blocks(worldX, worldY)) return true;
        if (collisionReader == null) return false;
        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);
        if (playerPassabilityProvider != null && playerPassabilityProvider.test(gridX, gridY)) return false;
        return staticBlocksAtGrid(gridX, gridY);
    }

    public boolean isInitialized() {
        return collisionReader != null;
    }

    public int getCollisionWidth() {
        return collisionReader == null ? 0 : collisionReader.getWidth();
    }

    public int getCollisionHeight() {
        return collisionReader == null ? 0 : collisionReader.getHeight();
    }

    private boolean isWithinCollisionMap(int gridX, int gridY) {
        return gridX >= 0 && gridY >= 0
                && gridX < collisionReader.getWidth()
                && gridY < collisionReader.getHeight();
    }

    private void maybeLogDynamicBlock(float worldX, float worldY) {
        long now = System.currentTimeMillis();
        if (now - lastDynamicLogAt < DYNAMIC_LOG_COOLDOWN_MS) {
            return;
        }
        lastDynamicLogAt = now;
        int gridX = (int) (worldX / GRID_W);
        int gridY = (int) (worldY / GRID_H);
        log.debug("Dynamic collision block at world=({},{}) tile=({},{}).", (int) worldX, (int) worldY, gridX, gridY);
    }
}
