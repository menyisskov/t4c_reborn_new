package com.perso.T4C.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.perso.T4C.monster.loot.LootResult;
import com.perso.T4C.player.Player;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.entity.NameRenderer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Holds and manages items lying on the ground (monster loot drops).
 * Provides depth-sorted render items and click-based pickup.
 */
@Slf4j
public class GroundItemManager {

    /** Outcome of a pickup attempt, for caller messaging. */
    public enum PickupStatus {
        NONE,        // no ground item under the cursor
        TOO_FAR,     // an item was clicked but the player is out of range
        TOO_HEAVY,
        UNIQUE_ITEM,
        PICKED_UP    // item picked up successfully
    }

    /** Result of a pickup attempt. */
    public static final class PickupResult {
        public final PickupStatus status;
        public final String label; // describes what was picked up / clicked

        PickupResult(PickupStatus status, String label) {
            this.status = status;
            this.label = label;
        }

        static final PickupResult NONE = new PickupResult(PickupStatus.NONE, null);
    }

    private List<GroundItem> items = new ArrayList<>();
    private List<CorpseDrop> corpses = new ArrayList<>();
    private final Map<Integer, WorldDrops> dropsByWorld = new HashMap<>();
    private final Map<GroundItem, CorpseDrop> corpseOwners = new HashMap<>();
    private final Set<CorpseDrop> claimedCorpseGold = new HashSet<>();
    private int activeWorldZ = 0;
    private final Random random = new Random();

    /** Logical corpse container; its contents are rendered as the dropped item sprites. */
    public record CorpseDrop(float x, float y, int gold, List<String> itemNames, long createdAtMillis) {
    }

    private record WorldDrops(List<GroundItem> items, List<CorpseDrop> corpses) {
    }

    /** Keeps loot and corpses alive when the player changes world/Z. */
    public void setActiveWorld(int worldZ) {
        if (worldZ == activeWorldZ) return;
        dropsByWorld.put(activeWorldZ, new WorldDrops(items, corpses));
        WorldDrops target = dropsByWorld.computeIfAbsent(worldZ,
                ignored -> new WorldDrops(new ArrayList<>(), new ArrayList<>()));
        items = target.items();
        corpses = target.corpses();
        activeWorldZ = worldZ;
    }

    /**
     * Apply a loot roll: gold is granted immediately via {@code goldGained} (no gold
     * sprite exists, so it never appears on the ground), while items are dropped on the
     * ground around the given world position, scattered slightly so they don't fully overlap.
     *
     * @param goldGained invoked with the gold amount when {@code loot.gold() > 0}
     */
    public void spawnFromLoot(LootResult loot, float x, float y, IntConsumer goldGained) {
        if (loot == null || loot.isEmpty()) {
            return;
        }
        if (loot.gold() > 0 && goldGained != null) {
            goldGained.accept(loot.gold());
        }
        for (String itemName : loot.itemNames()) {
            items.add(GroundItem.ofItem(itemName, scatter(x), scatter(y)));
        }
        log.info("Loot at ({}, {}): {} gold (granted), {} item(s) on ground",
                (int) x, (int) y, loot.gold(), loot.itemNames().size());
    }

    private float scatter(float base) {
        return base + (random.nextFloat() - 0.5f) * 24f;
    }

    public CorpseDrop spawnCorpse(List<String> itemNames, int gold, float x, float y) {
        return spawnCorpse(itemNames, List.of(), gold, x, y);
    }

    public CorpseDrop spawnCorpse(List<String> itemNames, List<Integer> remainingCharges, int gold, float x, float y) {
        List<String> contents = itemNames == null ? List.of() : List.copyOf(itemNames);
        CorpseDrop corpse = new CorpseDrop(x, y, Math.max(0, gold), contents, System.currentTimeMillis());
        corpses.add(corpse);
        for (int i = 0; i < contents.size(); i++) {
            String itemName = contents.get(i);
            int charges = remainingCharges != null && i < remainingCharges.size() ? remainingCharges.get(i) : -1;
            if (itemName != null) {
                GroundItem groundItem = GroundItem.ofItem(itemName, scatter(x), scatter(y), charges);
                items.add(groundItem);
                corpseOwners.put(groundItem, corpse);
            }
        }
        return corpse;
    }

    public void dropItem(String itemKey, int remainingCharges, float x, float y) {
        if (itemKey != null) items.add(GroundItem.ofItem(itemKey, x, y, remainingCharges));
    }

    /**
     * Append depth-sorted render items for all ground items. Each obtained RenderItem
     * must already be reset by the supplier (as MainGameScreen#obtainRenderItem does).
     */
    public void addRenderItems(List<ObjectRenderer.RenderItem> out, Supplier<ObjectRenderer.RenderItem> pool,
                               SpriteBatch batch, ShaderProgram outlineShader) {
        for (GroundItem groundItem : items) {
            ObjectRenderer.RenderItem item = pool.get();
            item.isObject = false;
            item.y = groundItem.getDepthY();
            item.renderAction = () -> groundItem.render(batch, outlineShader);
            out.add(item);
        }
        for (CorpseDrop corpse : corpses) {
            if (!hasRemainingContents(corpse)) continue;
            ObjectRenderer.RenderItem item = pool.get();
            item.isObject = false;
            item.y = corpse.y() / com.perso.T4C.config.GameConstants.GRID_H;
            item.renderAction = () -> NameRenderer.renderName(batch, "Corpse",
                    corpse.x() - 16f, corpse.y() - 28f, 32f, 32f);
            out.add(item);
        }
    }

    /**
     * Update hover state for all ground items based on the mouse world position.
     */
    public void onMouseMove(float worldX, float worldY) {
        // Only the topmost matching item is highlighted, mirroring pickup precedence.
        boolean topHandled = false;
        for (int i = items.size() - 1; i >= 0; i--) {
            GroundItem groundItem = items.get(i);
            boolean hovered = !topHandled && groundItem.isMouseOver(worldX, worldY);
            groundItem.setHovered(hovered);
            if (hovered) {
                topHandled = true;
            }
        }
    }

    /** Whether any ground item is currently hovered. */
    public boolean isAnyHovered() {
        for (GroundItem groundItem : items) {
            if (groundItem.isHovered()) {
                return true;
            }
        }
        return false;
    }

    public boolean showNameAt(float worldX, float worldY) {
        for (int i = items.size() - 1; i >= 0; i--) {
            GroundItem groundItem = items.get(i);
            if (groundItem.isMouseOver(worldX, worldY)) {
                groundItem.showName();
                return true;
            }
        }
        for (CorpseDrop corpse : corpses) {
            if (hasRemainingContents(corpse) && distanceSquared(worldX, worldY, corpse.x(), corpse.y()) <= 32f * 32f) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempt to pick up a ground item at the given world coordinates.
     *
     * @param worldX        click X in world space
     * @param worldY        click Y in world space
     * @param player        the player picking up
     * @param tileDistance  distance in tiles between player and clicked point
     * @param maxTileDistance maximum allowed pickup distance in tiles
     */
    public PickupResult pickUpAt(float worldX, float worldY, Player player, int tileDistance, int maxTileDistance) {
        if (player == null || items.isEmpty()) {
            return PickupResult.NONE;
        }
        // Pick the topmost (last drawn) matching item.
        for (int i = items.size() - 1; i >= 0; i--) {
            GroundItem groundItem = items.get(i);
            if (!groundItem.isMouseOver(worldX, worldY)) {
                continue;
            }
            if (tileDistance > maxTileDistance) {
                return new PickupResult(PickupStatus.TOO_FAR, groundItem.describe());
            }
            String label = groundItem.describe();
            InventoryService.Result result = InventoryService.add(
                    player, groundItem.getItemName(), groundItem.getRemainingCharges());
            if (!result.success()) {
                if (result.failure() == InventoryService.Failure.TOO_HEAVY) {
                    return new PickupResult(PickupStatus.TOO_HEAVY, label);
                }
                if (result.failure() == InventoryService.Failure.UNIQUE_ITEM) {
                    return new PickupResult(PickupStatus.UNIQUE_ITEM, label);
                }
                return PickupResult.NONE;
            }
            items.remove(i);
            CorpseDrop corpse = corpseOwners.remove(groundItem);
            if (corpse != null && corpse.gold() > 0 && claimedCorpseGold.add(corpse)) {
                player.addGold(corpse.gold());
            }
            log.info("Player picked up {}", label);
            return new PickupResult(PickupStatus.PICKED_UP, label);
        }
        for (CorpseDrop corpse : corpses) {
            if (corpse.gold() <= 0 || claimedCorpseGold.contains(corpse)
                    || distanceSquared(worldX, worldY, corpse.x(), corpse.y()) > 32f * 32f) continue;
            if (tileDistance > maxTileDistance) return new PickupResult(PickupStatus.TOO_FAR, "corpse gold");
            claimedCorpseGold.add(corpse);
            player.addGold(corpse.gold());
            return new PickupResult(PickupStatus.PICKED_UP, corpse.gold() + " gold from your corpse");
        }
        return PickupResult.NONE;
    }

    public void clear() {
        items.clear();
        corpses.clear();
        dropsByWorld.clear();
        corpseOwners.clear();
        claimedCorpseGold.clear();
    }

    private boolean hasRemainingContents(CorpseDrop corpse) {
        return (corpse.gold() > 0 && !claimedCorpseGold.contains(corpse))
                || corpseOwners.containsValue(corpse);
    }

    private static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }
}
