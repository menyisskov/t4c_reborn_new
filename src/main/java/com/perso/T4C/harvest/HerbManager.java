package com.perso.T4C.harvest;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.render.ObjectRenderer;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/** Deterministic one-per-viewport herb spawning and lifecycle. */
public final class HerbManager {
    // Temporary fixed spawn used to validate harvesting end-to-end.
    private static final int TEST_HERB_TILE_X = 240;
    private static final int TEST_HERB_TILE_Y = 652;
    private static final int TEST_HERB_WORLD_Z = 0;
    private static final int VIEW_MARGIN_TILES = 2;
    private final Set<Long> harvestedCells = new HashSet<>();
    private HerbNode active;
    private long activeCell = Long.MIN_VALUE;
    private int activeWorldZ;
    private String lastSpawnDiagnostic = "not attempted";
    private boolean testHerbHarvested;

    public HerbNode getActive() { return active; }

    public void setActiveWorld(int worldZ) {
        if (activeWorldZ == worldZ) return;
        activeWorldZ = worldZ;
        active = null;
        activeCell = Long.MIN_VALUE;
    }

    public void updateViewport(int startX, int endX, int startY, int endY, MapReader reader, int playerTileX, int playerTileY) {
        if (reader == null || endX < startX || endY < startY) return;

        // The viewport bounds follow the camera and can change by one tile while the
        // player walks. Keep the current node alive for as long as it is still on
        // screen; otherwise every small boundary change would replace it and make it
        // appear to blink/disappear during movement.
        if (active != null && isInsideViewport(active, startX, endX, startY, endY)) return;

        // The previous node genuinely left the viewport. A new viewport may map to
        // the same deterministic cell, so invalidate activeCell as well to allow a
        // replacement to be selected immediately.
        if (active != null) {
            active = null;
            activeCell = Long.MIN_VALUE;
        }

        if (!testHerbHarvested && activeWorldZ == TEST_HERB_WORLD_Z
                && TEST_HERB_TILE_X >= startX && TEST_HERB_TILE_X <= endX
                && TEST_HERB_TILE_Y >= startY && TEST_HERB_TILE_Y <= endY) {
            HerbDefinition definition = firstEnabledDefinition();
            if (definition != null && (active == null || active.getTileX() != TEST_HERB_TILE_X
                    || active.getTileY() != TEST_HERB_TILE_Y)) {
                active = new HerbNode(definition, TEST_HERB_TILE_X, TEST_HERB_TILE_Y,
                        (TEST_HERB_TILE_X + .5f) * GameConstants.GRID_W,
                        (TEST_HERB_TILE_Y + 1f) * GameConstants.GRID_H);
                lastSpawnDiagnostic = "fixed test spawn";
            }
            return;
        }
        int width = Math.max(1, endX - startX + 1);
        int height = Math.max(1, endY - startY + 1);
        int cellX = Math.floorDiv((startX + endX) / 2, width);
        int cellY = Math.floorDiv((startY + endY) / 2, height);
        long cell = cellKey(activeWorldZ, cellX, cellY);
        if (cell == activeCell) return;
        activeCell = cell;
        active = null;
        if (harvestedCells.contains(cell)) {
            lastSpawnDiagnostic = "cell already harvested";
            return;
        }

        int minX = Math.max(0, startX + VIEW_MARGIN_TILES);
        int maxX = Math.min(reader.getWidth() - 1, endX - VIEW_MARGIN_TILES);
        int minY = Math.max(0, startY + VIEW_MARGIN_TILES);
        int maxY = Math.min(reader.getHeight() - 1, endY - VIEW_MARGIN_TILES);
        if (maxX < minX || maxY < minY) {
            lastSpawnDiagnostic = "viewport too small";
            return;
        }

        Random random = new Random(cell ^ 0x6A09E667F3BCC909L);
        int columns = maxX - minX + 1;
        int rows = maxY - minY + 1;
        int candidateCount = columns * rows;
        int start = random.nextInt(candidateCount);
        int rejectedGround = 0;
        int rejectedCollision = 0;
        int rejectedDistance = 0;
        HerbDefinition definition = weightedDefinition(random);
        if (definition == null) {
            lastSpawnDiagnostic = "no enabled definition";
            return;
        }
        for (int offset = 0; offset < candidateCount; offset++) {
            int index = (start + offset) % candidateCount;
            int x = minX + index % columns;
            int y = minY + index / columns;
            if (!isAllowedGround(reader.getGroundSpriteName(x, y))) { rejectedGround++; continue; }
            if (CollisionManager.getInstance().hasStaticCollisionAtGrid(x, y)) { rejectedCollision++; continue; }
            if (Math.max(Math.abs(x - playerTileX), Math.abs(y - playerTileY)) < 3) { rejectedDistance++; continue; }
            active = new HerbNode(definition, x, y,
                    (x + .5f) * GameConstants.GRID_W, (y + 1f) * GameConstants.GRID_H);
            lastSpawnDiagnostic = "spawned after " + (offset + 1) + "/" + candidateCount + " checks";
            return;
        }
        lastSpawnDiagnostic = "no candidate: ground=" + rejectedGround + ", collision="
                + rejectedCollision + ", nearPlayer=" + rejectedDistance;
    }

    public static boolean isAllowedGround(String name) {
        if (name == null) return false;
        String family = name.trim().replaceFirst("\\s*\\([^)]*\\)\\s*$", "");
        return "Grass".equalsIgnoreCase(family) || "64kNormalGrass".equalsIgnoreCase(family);
    }

    static boolean isInsideViewport(HerbNode node, int startX, int endX, int startY, int endY) {
        return node != null && node.getTileX() >= startX && node.getTileX() <= endX
                && node.getTileY() >= startY && node.getTileY() <= endY;
    }

    public void forceRefresh() {
        active = null;
        activeCell = Long.MIN_VALUE;
    }

    public String debugSummary(int startX, int endX, int startY, int endY, MapReader reader) {
        int eligible = 0;
        if (reader != null) {
            for (int y = Math.max(0, startY); y <= Math.min(reader.getHeight() - 1, endY); y++) {
                for (int x = Math.max(0, startX); x <= Math.min(reader.getWidth() - 1, endX); x++) {
                    if (isAllowedGround(reader.getGroundSpriteName(x, y))) eligible++;
                }
            }
        }
        String activeDescription = active == null ? "aucune"
                : active.getDefinition().getId() + " @ " + active.getTileX() + "," + active.getTileY();
        return "Herbes: " + HerbRegistry.load().size() + " definitions, " + eligible
                + " sols valides, active=" + activeDescription + " (" + lastSpawnDiagnostic + ")";
    }

    public void onMouseMove(float worldX, float worldY) {
        if (active != null) active.setHovered(active.isMouseOver(worldX, worldY));
    }

    public HerbNode findAt(float worldX, float worldY) {
        return active != null && active.isMouseOver(worldX, worldY) ? active : null;
    }

    public boolean showNameAt(float worldX, float worldY) {
        HerbNode node = findAt(worldX, worldY);
        if (node == null) return false;
        node.showName();
        return true;
    }

    public boolean complete(HerbNode node) {
        if (node == null || node != active || node.getState() != HerbNode.State.HARVESTING) return false;
        node.setState(HerbNode.State.HARVESTED);
        if (activeWorldZ == TEST_HERB_WORLD_Z && node.getTileX() == TEST_HERB_TILE_X
                && node.getTileY() == TEST_HERB_TILE_Y) testHerbHarvested = true;
        harvestedCells.add(activeCell);
        active = null;
        return true;
    }

    public void cancel(HerbNode node) {
        if (node != null && node == active && node.getState() == HerbNode.State.HARVESTING) {
            node.setState(HerbNode.State.AVAILABLE);
        }
    }

    public void addRenderItems(List<ObjectRenderer.RenderItem> out, Supplier<ObjectRenderer.RenderItem> pool,
                               SpriteBatch batch, ShaderProgram outlineShader) {
        if (active == null) return;
        HerbNode node = active;
        ObjectRenderer.RenderItem item = pool.get();
        item.isObject = false;
        item.y = node.getDepthY();
        item.renderAction = () -> node.render(batch, outlineShader);
        out.add(item);
    }

    private static HerbDefinition weightedDefinition(Random random) {
        List<HerbDefinition> definitions = HerbRegistry.load().stream()
                .filter(definition -> definition != null && definition.getSpawnWeight() > 0
                        && !definition.getItemKey().isBlank() && !definition.getWorldSprite().isBlank())
                .toList();
        int totalWeight = definitions.stream().mapToInt(HerbDefinition::getSpawnWeight).sum();
        if (totalWeight <= 0) return null;
        int roll = random.nextInt(totalWeight);
        for (HerbDefinition definition : definitions) {
            roll -= definition.getSpawnWeight();
            if (roll < 0) return definition;
        }
        return definitions.get(definitions.size() - 1);
    }

    private static HerbDefinition firstEnabledDefinition() {
        return HerbRegistry.load().stream()
                .filter(definition -> definition != null && definition.getSpawnWeight() > 0
                        && !definition.getItemKey().isBlank() && !definition.getWorldSprite().isBlank())
                .findFirst().orElse(null);
    }

    private static long cellKey(int z, int x, int y) {
        long key = ((long) z & 0xFFFFL) << 48;
        key ^= ((long) x & 0xFFFFFFL) << 24;
        key ^= (long) y & 0xFFFFFFL;
        return key;
    }
}
