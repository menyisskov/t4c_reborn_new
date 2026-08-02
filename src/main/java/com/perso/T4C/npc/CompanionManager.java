package com.perso.T4C.npc;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.CompanionCastVfxHook;
import com.perso.T4C.ui.SystemMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

import static com.perso.T4C.config.GameConstants.ENTITY_COLLISION_CLEARANCE_TILES;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Owns the player's single ally companion: summoning it from a dialogue action,
 * relaying the player's current fight to it, and keeping it alive across map
 * transitions. The companion itself is a regular {@link BaseNPC} registered
 * with {@link NPCManager}, so rendering and per-frame updates need no special
 * casing here.
 */
@Slf4j
public class CompanionManager {
    /** Re-pointed on map changes, where the whole NPCManager is rebuilt. */
    @Setter
    private NPCManager npcManager;
    @Setter
    private XpCurve xpCurve;
    /** Feeds AGGRESSIVE mode its target candidates; see CompanionNPC. */
    @Setter
    private Supplier<List<BaseMonster>> monsterSupplier;
    private CompanionNPC companion;

    public CompanionManager(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    public boolean hasCompanion() {
        return companion != null && !companion.isDead();
    }

    public CompanionNPC getCompanion() {
        return companion;
    }

    /**
     * Summons the companion type next to {@code sourcePosition} (the NPC that
     * offered help). A player may only keep one companion at a time.
     */
    public void spawnCompanionFor(Player owner, String companionType, Vector2 sourcePosition) {
        if (owner == null || companionType == null || companionType.isBlank()) {
            return;
        }
        if (hasCompanion()) {
            SystemMessage.showShared(I18n.message("message.companion_already_present"));
            return;
        }
        CompanionDef def = CompanionRegistry.findById(companionType);
        if (def == null) {
            log.warn("No companion definition for id={}", companionType);
            return;
        }
        spawnCompanionFromDef(owner, def, sourcePosition, 0);
    }

    public boolean spawnCompanionFromDef(Player owner, CompanionDef def, Vector2 sourcePosition, int startingHp) {
        if (owner == null || def == null || sourcePosition == null) return false;
        if (hasCompanion()) {
            SystemMessage.showShared(I18n.message("message.companion_already_present"));
            return false;
        }
        try {
            CompanionNPC spawned = new CompanionNPC(def, owner, xpCurve);
            spawned.setCurrentHp(startingHp);
            spawned.setMonsterSupplier(monsterSupplier);
            spawned.setDismissRequestHandler(this::dismissByPlayer);
            Vector2 spawnPosition = findFreeSpawnNear(sourcePosition);
            spawned.setSpawnPosition(spawnPosition.x, spawnPosition.y);
            npcManager.addNPC(spawned);
            companion = spawned;
            SystemMessage.showShared(I18n.message("message.companion_joined", I18n.resolve(spawned.getName())));
            log.info("Companion {} summoned for player at ({}, {})",
                    spawned.getName(), spawned.getTileX(), spawned.getTileY());
            return true;
        } catch (Exception ex) {
            log.warn("Failed to summon companion type={}", def.getId(), ex);
            return false;
        }
    }

    /**
     * Relays the player's current melee/ranged target so the companion joins the
     * same fight. Wired to {@link com.perso.T4C.monster.MonsterManager}'s attack
     * notification.
     */
    public void onPlayerAttacked(BaseMonster monster) {
        if (hasCompanion()) {
            companion.setCombatTarget(monster);
        }
    }

    /**
     * Applies a monster blow to the companion, despawning it when the hit is
     * lethal.
     */
    public void damageCompanion(int damage) {
        if (!hasCompanion()) {
            return;
        }
        if (companion.takeDamage(damage)) {
            SystemMessage.showShared(I18n.message("message.companion_died", I18n.resolve(companion.getName())));
            dismiss();
        }
    }

    /** Removes the companion from the world without any farewell visuals. */
    public void dismiss() {
        if (companion == null) {
            return;
        }
        npcManager.removeNPC(companion);
        companion = null;
    }

    /**
     * Sends the companion away at the player's request: it vanishes in a burst of
     * light where it stood, the way a multi-part monster leaves the world.
     */
    public void dismissByPlayer() {
        if (companion == null) {
            return;
        }
        String name = I18n.resolve(companion.getName());
        Vector2 lastPosition = new Vector2(companion.getPosition());
        companion.endInteraction();
        dismiss();
        CompanionCastVfxHook.playVanish(lastPosition.x, lastPosition.y);
        SystemMessage.showShared(I18n.message("message.companion_dismissed", name));
        log.info("Companion {} dismissed by the player", name);
    }

    /**
     * Re-registers the companion after a map change: {@link NPCManager} clears
     * every NPC on load, so the companion is re-added next to the player instead
     * of being lost.
     */
    public void onMapChanged(Vector2 playerPosition) {
        if (!hasCompanion() || playerPosition == null) {
            return;
        }
        Vector2 spawnPosition = findFreeSpawnNear(playerPosition);
        // The MonsterManager was rebuilt with the map, so re-point the supplier.
        companion.setMonsterSupplier(monsterSupplier);
        companion.warpTo(spawnPosition.x, spawnPosition.y);
        npcManager.addNPC(companion);
        log.info("Companion {} followed the player to the new map", companion.getName());
    }

    /**
     * Picks a walkable tile around the anchor so the companion never materializes
     * inside a wall.
     */
    private Vector2 findFreeSpawnNear(Vector2 anchor) {
        Vector2 fallback = new Vector2(anchor);
        CollisionManager collisionManager = CollisionManager.getInstance();
        if (!collisionManager.isInitialized()) {
            return fallback;
        }
        for (int ring = 1; ring <= 3; ring++) {
            for (int step = 0; step < 8; step++) {
                double angle = step * Math.PI / 4d;
                float x = anchor.x + (float) Math.cos(angle) * ring * GRID_W;
                float y = anchor.y + (float) Math.sin(angle) * ring * GRID_W;
                if (!collisionManager.hasCollisionNear(x, y, ENTITY_COLLISION_CLEARANCE_TILES)) {
                    return new Vector2(x, y);
                }
            }
        }
        return fallback;
    }
}
