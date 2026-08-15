package com.perso.T4C.npc;

import com.perso.T4C.i18n.I18n;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.config.Paths;
import com.perso.T4C.entity.NameableEntityHandler;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.ui.SystemMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Manages all NPCs in the game world.
 */
@Slf4j
public class NPCManager {
    private final List<BaseNPC> npcs = new ArrayList<>();
    private final ShaderProgram outlineShader;
    private final QuestService questService;
    private BaseNPC activeConversationNpc;
    /** Set after construction: DataNpc needs it to run SUMMON_COMPANION actions. */
    @Setter
    private CompanionManager companionManager;

    /**
     * Create NPC manager.
     */
    public NPCManager(ShaderProgram outlineShader) {
        this(outlineShader, null);
    }

    public NPCManager(ShaderProgram outlineShader, QuestService questService) {
        this.outlineShader = outlineShader;
        this.questService = questService;
    }

    /**
     * Add an NPC to the manager.
     */
    public void addNPC(BaseNPC npc) {
        npcs.add(npc);
        npc.setDamageCallback(playerDamageCallback);
        log.info("Added NPC: {} at position ({}, {})", npc.getName(), npc.getTileX(), npc.getTileY());
    }

    /** Fires the original spawn/popup hooks once the player and map are both ready. */
    public void triggerPopupEvents(Player player) {
        for (BaseNPC npc : List.copyOf(npcs)) if (npc instanceof DataNpc dataNpc) {
            dataNpc.triggerScriptEvent("OnInitialise", player);
            dataNpc.triggerScriptEvent("OnPopup", player);
        }
    }

    /** Removes a single NPC from the world (companion dismissal, death). */
    public void removeNPC(BaseNPC npc) {
        if (npc == null) {
            return;
        }
        if (activeConversationNpc == npc) {
            npc.endInteraction();
            activeConversationNpc = null;
        }
        npcs.remove(npc);
    }

    private NpcDamageCallback playerDamageCallback;

    /** Wires the callback invoked when a hostile NPC hits the player, mirroring MonsterManager. */
    public void setPlayerDamageCallback(NpcDamageCallback callback) {
        this.playerDamageCallback = callback;
        for (BaseNPC npc : npcs) {
            npc.setDamageCallback(callback);
        }
    }

    /** Stops every hostile NPC from chasing/attacking (e.g. on player death/respawn). */
    public void resetAllHostility() {
        for (BaseNPC npc : npcs) {
            npc.resetHostility();
        }
    }

    public boolean spawnNPC(String name, float worldX, float worldY) {
        NpcDef def = NpcRegistry.findByName(name);
        if (def == null) {
            return false;
        }
        try {
            BaseNPC npc = new DataNpc(def, questService, () -> companionManager);
            npc.setSpawnPosition(worldX, worldY);
            addNPC(npc);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to summon NPC type={} at ({}, {})", name, worldX, worldY, ex);
            return false;
        }
    }

    /**
     * Update all NPCs.
     */
    public void update(float delta, Vector2 playerPosition) {
        for (BaseNPC npc : npcs) {
            npc.update(delta, playerPosition);
            npc.checkPlayerRange(playerPosition);
        }
        clearInactiveConversation();
    }

    public void updateVisible(float delta, Vector2 playerPosition, int startX, int endX, int startY, int endY, int margin) {
        for (BaseNPC npc : npcs) {
            int tileX = npc.getTileX();
            int tileY = npc.getTileY();
            if (tileX < startX - margin || tileX > endX + margin || tileY < startY - margin || tileY > endY + margin) {
                continue;
            }
            npc.update(delta, playerPosition);
            npc.checkPlayerRange(playerPosition);
        }
        if (activeConversationNpc != null) {
            activeConversationNpc.checkPlayerRange(playerPosition);
        }
        clearInactiveConversation();
    }

    /**
     * Render all NPCs.
     */
    public void render(SpriteBatch batch) {
        for (BaseNPC npc : npcs) {
            npc.render(batch, outlineShader);
        }
    }

    public void renderDialogOverlay(SpriteBatch batch) {
        for (BaseNPC npc : npcs) {
            npc.renderDialogOverlay(batch);
        }
    }

    public void renderNameOverlay(SpriteBatch batch) {
        for (BaseNPC npc : npcs) {
            npc.renderNameOverlay(batch);
        }
    }

    public boolean advanceDialog() {
        return activeConversationNpc != null
                && activeConversationNpc.isInteracting()
                && activeConversationNpc.advanceDialog();
    }

    public boolean handleDialogClick(float mouseX, float mouseY, Player player) {
        return activeConversationNpc != null
                && activeConversationNpc.isInteracting()
                && activeConversationNpc.handleDialogClick(mouseX, mouseY, player);
    }

    public boolean hasActiveConversation() {
        clearInactiveConversation();
        return activeConversationNpc != null;
    }

    public boolean talkToActiveNpc(String text, Player player) {
        clearInactiveConversation();
        if (activeConversationNpc == null) {
            return false;
        }
        boolean handled = activeConversationNpc.talk(text, player);
        clearInactiveConversation();
        return handled;
    }

    public boolean endActiveConversation() {
        clearInactiveConversation();
        if (activeConversationNpc == null) {
            return false;
        }
        activeConversationNpc.endInteraction();
        activeConversationNpc = null;
        return true;
    }

    public boolean isNpcAt(float mouseX, float mouseY) {
        return findNpcAt(mouseX, mouseY) != null;
    }

    /** Returns the NPC under the supplied world position, if any. */
    public BaseNPC findNpcAt(float mouseX, float mouseY) {
        for (BaseNPC npc : npcs) {
            if (npc.isMouseOver(mouseX, mouseY)) {
                return npc;
            }
        }
        return null;
    }

    /**
     * Handle mouse movement for hover detection.
     */
    public void onMouseMove(float mouseX, float mouseY) {
        for (BaseNPC npc : npcs) {
            boolean wasHovered = npc.isHovered();
            boolean isNowHovered = npc.isMouseOver(mouseX, mouseY);
            npc.setHovered(isNowHovered);

            if (!wasHovered && isNowHovered) {
                log.info("Mouse entered NPC: {} at position ({}, {}) - Mouse at ({}, {})",
                    npc.getName(),
                    String.format("%.0f", npc.getPosition().x),
                    String.format("%.0f", npc.getPosition().y),
                    String.format("%.0f", mouseX),
                    String.format("%.0f", mouseY));
            } else if (wasHovered && !isNowHovered) {
                log.info("Mouse left NPC: {}", npc.getName());
            }
        }
    }

    /**
     * Handle click on NPCs.
     */
    public boolean onClick(float mouseX, float mouseY, Player player) {
        for (BaseNPC npc : npcs) {
            if (npc.isMouseOver(mouseX, mouseY)) {
                if (activeConversationNpc != null && activeConversationNpc != npc) {
                    activeConversationNpc.endInteraction();
                    activeConversationNpc = null;
                }
                boolean interactionStarted = npc.onClick(player);
                if (interactionStarted) {
                    activeConversationNpc = npc;
                    log.info("Player interacting with NPC: {}", npc.getName());
                } else if (!com.perso.T4C.combat.CombatGeometry.hasTalkLineOfSight(
                        player.getPositionVector(), npc.getPosition())) {
                    log.info("NPC {} is not in line of sight for interaction", npc.getName());
                    SystemMessage.showShared(I18n.message("message.target_no_line_of_sight"));
                } else {
                    log.info("NPC {} is too far away for interaction", npc.getName());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Handle an attack click on an NPC while the player is in combat mode
     *
     * @return true if an NPC was found and "attacked" at this position.
     */
    public boolean attackNpc(float mouseX, float mouseY, Player player, SystemMessage systemMessage) {
        BaseNPC npc = findNpcAt(mouseX, mouseY);
        if (npc == null) {
            return false;
        }
        if (npc instanceof CompanionNPC) {
            // The player's own ally is never a valid attack target.
            return false;
        }
        if (systemMessage != null) {
            systemMessage.show(I18n.message("message.attack_npc",  I18n.resolve(npc.getName())));
        }
        onNpcAttacked(npc, player);
        int rawDamage = com.perso.T4C.helper.CombatMath.computeMeleeDamage(player);
        var result = com.perso.T4C.combat.CombatResolver.resolve(
                new com.perso.T4C.combat.PhysicalAttackRequest(
                        com.perso.T4C.combat.CombatProfiles.fromPlayer(player),
                        com.perso.T4C.combat.CombatProfiles.fromNpc(npc), rawDamage, 0, false),
                java.util.concurrent.ThreadLocalRandom.current());
        if (result.hit() && result.damage() > 0) damageNpc(npc, result.damage(), player);
        return true;
    }

    public boolean damageNpc(BaseNPC npc, int damage, Player player) {
        if (npc == null || damage <= 0 || !npcs.contains(npc)) return false;
        npc.setCurrentHp(Math.max(0, npc.getCurrentHp() - damage));
        if (npc.getCurrentHp() > 0) return false;
        if (npc instanceof DataNpc dataNpc) {
            dataNpc.triggerScriptEvent("OnDeath", player);
            dataNpc.triggerScriptEvent("OnDestroy", player);
        }
        removeNPC(npc);
        log.info("NPC {} was slain by the player", npc.getName());
        return true;
    }

    public void onNpcAttacked(BaseNPC npc) {
        onNpcAttacked(npc, null);
    }

    public void onNpcAttacked(BaseNPC npc, Player player) {
        if (npc == null || !npcs.contains(npc) || npc instanceof CompanionNPC) {
            return;
        }
        if (activeConversationNpc == npc) {
            npc.endInteraction();
            activeConversationNpc = null;
        }
        if (player != null && npc instanceof DataNpc dataNpc) {
            dataNpc.triggerScriptEvent("OnAttacked", player);
            if (npc.getCurrentHp() <= 0) {
                dataNpc.triggerScriptEvent("OnDeath", player);
                dataNpc.triggerScriptEvent("OnDestroy", player);
                removeNPC(npc);
                return;
            }
        }
        List<String> fleeShouts = fleeShoutsOf(npc);
        if (!fleeShouts.isEmpty()) {
            npc.shout(fleeShouts.get((int) (Math.random() * fleeShouts.size())), 3000L);
        } else if (!isPassiveOnAttack(npc)) {
            // Original client behavior: guards/regular NPCs fight back when attacked.
            npc.provoke();
        }
        log.info("Player attacked NPC: {}", npc.getName());
    }

    /**
     * Flee lines declared by the NPC's definition, translated for display.
     * A non-empty list is what marks an NPC as fleeing instead of retaliating.
     */
    private List<String> fleeShoutsOf(BaseNPC npc) {
        NpcDef def = NpcRegistry.findByName(npc.getName());
        if (def == null || def.getFleeShouts().isEmpty()) {
            return List.of();
        }
        List<String> translated = new ArrayList<>(def.getFleeShouts().size());
        for (String shout : def.getFleeShouts()) {
            translated.add(I18n.resolve(shout));
        }
        return translated;
    }

    private boolean isPassiveOnAttack(BaseNPC npc) {
        NpcDef def = NpcRegistry.findByName(npc.getName());
        return def != null && "true".equalsIgnoreCase(def.getSourceEvents().get("@combat.passiveOnAttack"));
    }

    /**
     * Handle right-click on NPCs (show name above head).
     */
    public boolean onRightClick(float mouseX, float mouseY) {
        return NameableEntityHandler.handleRightClick(mouseX, mouseY, npcs);
    }

    /**
     * Reload NPC resources.
     */
    public void onResourcesReloaded() {
        for (BaseNPC npc : npcs) {
            npc.onResourcesReloaded();
        }
    }

    public void dispose() {
        activeConversationNpc = null;
        for (BaseNPC npc : npcs) {
            npc.dispose();
        }
        npcs.clear();
    }

    /**
     * Get all NPCs.
     */
    public List<BaseNPC> getNPCs() {
        return npcs;
    }

    public void clear() {
        activeConversationNpc = null;
        npcs.clear();
        log.info("Cleared all NPCs");
    }

    private void clearInactiveConversation() {
        if (activeConversationNpc != null && !activeConversationNpc.isInteracting()) {
            activeConversationNpc = null;
        }
    }

    /**
     * Load NPCs from the global binary spawn file, falling back to map-side files, then
     * instantiate types from com.perso.T4C.npc.named and place them with
     * setSpawnPosition.
     */
    public void initializeNpcsFromMap(String mapPath) throws GameException {
        if (mapPath == null || mapPath.isBlank()) {
            throw new GameException("Map path is null/empty for NPC spawn loading");
        }
        clear();

        try {
            SpawnSource source = readSpawnsForMap(mapPath);
            if (source == null) {
                log.warn("NPC spawn file not found for map {}. No NPCs loaded.", mapPath);
                return;
            }
            List<NpcSpawnEntry> entries = source.entries();
            if (entries == null || entries.isEmpty()) {
                log.info("NPC spawn file is empty: {}", source.path());
                return;
            }

            int loaded = 0;
            for (NpcSpawnEntry entry : entries) {
                if (entry == null || entry.type == null || entry.type.isBlank()) {
                    continue;
                }
                NpcDef def = NpcRegistry.findByName(entry.type);
                if (def == null) {
                    log.warn("No NPC definition for spawn type={} at ({}, {}, {})", entry.type, entry.x, entry.y, entry.z);
                    continue;
                }
                try {
                    BaseNPC npc = new DataNpc(def, questService, () -> companionManager);
                    npc.setSpawnPosition(entry.x * GRID_W, entry.y * GRID_H);
                    npc.setStationary(entry.stationary);
                    addNPC(npc);
                    loaded++;
                } catch (Exception ex) {
                    log.warn("Failed to load NPC spawn type={} at ({}, {}, {})", entry.type, entry.x, entry.y, entry.z, ex);
                }
            }
            log.info("Loaded {} NPC spawn(s) from {}", loaded, source.path());
        } catch (Exception e) {
            throw new GameException("Failed to load NPC spawns for " + mapPath, e);
        }
    }

    private SpawnSource readSpawnsForMap(String mapPath) throws Exception {
        File globalSpawnFile = new File(Paths.NPC_SPAWNS_BIN);
        if (!globalSpawnFile.exists()) {
            return null;
        }
        int z = resolveMapZ(mapPath);
        List<NpcSpawnEntry> filtered = new ArrayList<>();
        for (NpcSpawnEntry entry : readBinarySpawns(globalSpawnFile)) {
            if (entry.z == z) {
                filtered.add(entry);
            }
        }
        return new SpawnSource(globalSpawnFile.getPath(), filtered);
    }

    private int resolveMapZ(String mapPath) {
        String normalized = new File(mapPath).getPath().replace('\\', '/');
        for (MapDefinition map : MapDefinition.values()) {
            if (new File(map.getMapPath()).getPath().replace('\\', '/').equals(normalized)) {
                return map.getZ();
            }
        }
        return 0;
    }

    private List<NpcSpawnEntry> readBinarySpawns(File spawnFile) throws Exception {
        List<SpawnBinaryIO.Entry> binaryEntries = SpawnBinaryIO.read(spawnFile);
        List<NpcSpawnEntry> entries = new ArrayList<>(binaryEntries.size());
        for (SpawnBinaryIO.Entry binaryEntry : binaryEntries) {
            NpcSpawnEntry entry = new NpcSpawnEntry();
            entry.type = binaryEntry.type;
            entry.x = binaryEntry.x;
            entry.y = binaryEntry.y;
            entry.z = binaryEntry.z;
            entry.stationary = binaryEntry.stationary;
            entries.add(entry);
        }
        return entries;
    }

    /**
     * Class representing NpcSpawnEntry.
     */
    private static final class NpcSpawnEntry {
        String type;
        int x;
        int y;
        int z;
        boolean stationary;
    }

    private record SpawnSource(String path, List<NpcSpawnEntry> entries) {
    }
}

