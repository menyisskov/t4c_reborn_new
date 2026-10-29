package com.perso.T4C.npc.core;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.entity.NameableEntityHandler;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SanctuaryCombatRules;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.companion.CompanionManager;
import com.perso.T4C.npc.companion.CompanionNPC;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.ui.SystemMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NPCManager {

  private final List<BaseNPC> npcs = new ArrayList<>();
  private final List<NpcSpawnEntry> pendingSpawns = new ArrayList<>();

  private final ShaderProgram outlineShader;

  private final QuestService questService;

  private BaseNPC activeConversationNpc;

  // Online puppet mode (T4C-0289): the registry only holds snapshot-driven puppets. Default false.
  private boolean puppetMode;
  /** Online sessions only: takes a click on an NPC as an intent for the server. */
  private java.util.function.Predicate<BaseNPC> remoteInteract;

  @Setter private CompanionManager companionManager;

  public NPCManager(ShaderProgram outlineShader) {

    this(outlineShader, null);
  }

  public NPCManager(ShaderProgram outlineShader, QuestService questService) {

    this.outlineShader = outlineShader;

    this.questService = questService;
  }

  /** Online sessions only (puppet mode): the hook receives the clicked NPC; null ignores clicks. */
  public void setRemoteInteract(java.util.function.Predicate<BaseNPC> remoteInteract) {
    this.remoteInteract = remoteInteract;
  }

  /**
   * Online puppet registry: NPCs added to it become puppets, and nothing here runs popup events,
   * summons, combat, conversations or per-frame logic; the server owns all of it.
   */
  public void setPuppetMode(boolean puppetMode) {

    this.puppetMode = puppetMode;
  }

  public boolean isPuppetMode() {

    return puppetMode;
  }

  public void addNPC(BaseNPC npc) {

    if (puppetMode) {

      npc.setPuppet(true);

      npcs.add(npc);

      return;
    }

    npcs.add(npc);

    npc.setDamageCallback(playerDamageCallback);

    // Hundreds of NPCs can be created during map loading; keep per-entity
    // diagnostics available without flooding the startup log.
    log.debug("Added NPC: {} at position ({}, {})", npc.getName(), npc.getTileX(), npc.getTileY());
  }

  public void triggerPopupEvents(Player player) {

    if (puppetMode) return;

    lifecyclePlayer = player;

    for (BaseNPC npc : List.copyOf(npcs)) {

      npc.onInitialise(player);

      npc.onPopup(player);
    }
  }

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

  private BaseNPC.DamageCallback playerDamageCallback;

  private Player lifecyclePlayer;

  public void setPlayerDamageCallback(BaseNPC.DamageCallback callback) {

    this.playerDamageCallback = callback;

    for (BaseNPC npc : List.copyOf(npcs)) {

      npc.setDamageCallback(callback);
    }
  }

  public void resetAllHostility() {

    for (BaseNPC npc : npcs) {

      npc.resetHostility();
    }
  }

  public boolean spawnNPC(String name, float worldX, float worldY) {

    if (puppetMode) return false;

    try {

      BaseNPC npc = NpcFactoryRegistry.create(name, npcContext());

      if (npc == null) return false;

      npc.setSpawnPosition(worldX, worldY);

      addNPC(npc);

      npc.onInitialise(lifecyclePlayer);

      npc.onPopup(lifecyclePlayer);

      return true;

    } catch (Exception ex) {

      log.warn("Failed to summon NPC type={} at ({}, {})", name, worldX, worldY, ex);

      return false;
    }
  }

  public void update(float delta, Vector2 playerPosition) {

    if (puppetMode) return;

    for (BaseNPC npc : List.copyOf(npcs)) {

      npc.update(delta, playerPosition);

      npc.checkPlayerRange(playerPosition);

      if (npc instanceof ScriptedNpc scripted && scripted.isNativeSelfDestructRequested()) {

        removeNPC(npc);
      }
    }

    clearInactiveConversation();
  }

  public void updateVisible(
      float delta, Vector2 playerPosition, int startX, int endX, int startY, int endY, int margin) {

    if (puppetMode) return;

    spawnPendingInView(startX, endX, startY, endY, margin);
    for (BaseNPC npc : List.copyOf(npcs)) {

      int tileX = npc.getTileX();

      int tileY = npc.getTileY();

      if (tileX < startX - margin
          || tileX > endX + margin
          || tileY < startY - margin
          || tileY > endY + margin) {

        continue;
      }

      npc.update(delta, playerPosition);

      npc.checkPlayerRange(playerPosition);

      if (npc instanceof ScriptedNpc scripted && scripted.isNativeSelfDestructRequested()) {

        removeNPC(npc);
      }
    }

    if (activeConversationNpc != null) {

      activeConversationNpc.checkPlayerRange(playerPosition);
    }

    clearInactiveConversation();
  }

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

    if (puppetMode) return false;

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

  public void showAllNames() {

    for (BaseNPC npc : npcs) {

      npc.showName();
    }
  }

  public boolean isNpcAt(float mouseX, float mouseY) {

    return findNpcAt(mouseX, mouseY) != null;
  }

  /**
   * The NPC under the cursor. Single-player takes the first NPC in list order. Online (puppet
   * mode) resolves overlap: when click boxes overlap (one NPC standing just behind another) the
   * NPC whose box centre is nearest the cursor wins, and the one drawn in front (lower on screen)
   * breaks a tie, so a click on a visible figure never lands on the NPC beside or behind it.
   */
  public BaseNPC findNpcAt(float mouseX, float mouseY) {

    if (!puppetMode) {

      // Single-player: the first NPC in list order, exactly as before.
      for (BaseNPC npc : npcs) {

        if (npc.isMouseOver(mouseX, mouseY)) {

          return npc;
        }
      }

      return null;
    }

    return findPuppetNpcAt(mouseX, mouseY);
  }

  private BaseNPC findPuppetNpcAt(float mouseX, float mouseY) {

    BaseNPC best = null;

    float bestDistance = Float.MAX_VALUE;

    for (BaseNPC npc : npcs) {

      if (!npc.isMouseOver(mouseX, mouseY)) continue;

      float distance = npc.mouseOverDistanceSquared(mouseX, mouseY);

      if (best == null
          || distance < bestDistance
          || (distance == bestDistance && npc.getPosition().y > best.getPosition().y)) {

        best = npc;

        bestDistance = distance;
      }
    }

    return best;
  }

  public void onMouseMove(float mouseX, float mouseY) {

    for (BaseNPC npc : npcs) {

      boolean wasHovered = npc.isHovered();

      boolean isNowHovered = npc.isMouseOver(mouseX, mouseY);

      npc.setHovered(isNowHovered);

      if (!wasHovered && isNowHovered) {

        log.info(
            "Mouse entered NPC: {} at position ({}, {}) - Mouse at ({}, {})",
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

  public boolean onClick(float mouseX, float mouseY, Player player) {

    if (puppetMode) {
      // Online: a click on an NPC is only an intent for the server; nothing starts here.
      if (remoteInteract == null) return false;
      BaseNPC clicked = findNpcAt(mouseX, mouseY);
      return clicked != null && remoteInteract.test(clicked);
    }

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

          // T4C-0063: this branch used to log and tell the player nothing at all, so clicking
          // someone you could not reach looked like the game ignoring the click. The sibling
          // no-line-of-sight branch above always did say why.
          SystemMessage.showShared(I18n.message("message.npc_too_far"));
        }

        return true;
      }
    }

    return false;
  }

  public boolean attackNpc(float mouseX, float mouseY, Player player, SystemMessage systemMessage) {

    if (puppetMode) return false;

    BaseNPC npc = findNpcAt(mouseX, mouseY);

    if (npc == null) {

      return false;
    }

    if (player == null
        || !SanctuaryCombatRules.canFight(player.getPositionVector(), npc.getPosition()))
      return false;

    if (npc instanceof CompanionNPC) {

      return false;
    }

    if (systemMessage != null) {

      systemMessage.show(I18n.message("message.attack_npc", I18n.resolve(npc.getName())));
    }

    onNpcAttacked(npc, player);

    int rawDamage = com.perso.T4C.helper.CombatMath.computeMeleeDamage(player);

    var result =
        com.perso.T4C.combat.CombatResolver.resolve(
            new com.perso.T4C.combat.PhysicalAttackRequest(
                com.perso.T4C.combat.CombatProfiles.fromPlayer(player),
                com.perso.T4C.combat.CombatProfiles.fromNpc(npc),
                rawDamage,
                0,
                false),
            java.util.concurrent.ThreadLocalRandom.current());

    if (result.hit() && result.damage() > 0) damageNpc(npc, result.damage(), player);

    return true;
  }

  public boolean damageNpc(BaseNPC npc, int damage, Player player) {

    if (puppetMode || npc == null || damage <= 0 || !npcs.contains(npc)) return false;
    if (!SanctuaryCombatRules.canFight(
        player == null ? null : player.getPositionVector(), npc.getPosition())) return false;

    npc.setCurrentHp(Math.max(0, npc.getCurrentHp() - damage));

    if (npc.getCurrentHp() > 0) return false;

    npc.onDeath(player);

    npc.onDestroy(player);

    removeNPC(npc);

    log.info("NPC {} was slain by the player", npc.getName());

    return true;
  }

  public void onNpcAttacked(BaseNPC npc) {

    onNpcAttacked(npc, null);
  }

  public void onNpcAttacked(BaseNPC npc, Player player) {

    if (puppetMode || npc == null || !npcs.contains(npc) || npc instanceof CompanionNPC) {

      return;
    }

    if (activeConversationNpc == npc) {

      npc.endInteraction();

      activeConversationNpc = null;
    }

    if (player != null) {

      npc.onAttacked(player);

      if (npc.getCurrentHp() <= 0) {

        npc.onDeath(player);

        npc.onDestroy(player);

        removeNPC(npc);

        return;
      }
    }

    List<String> fleeShouts = fleeShoutsOf(npc);

    if (!fleeShouts.isEmpty()) {

      npc.shout(fleeShouts.get((int) (Math.random() * fleeShouts.size())), 3000L);

    } else if (!isPassiveOnAttack(npc)) {

      npc.provoke();
    }

    log.info("Player attacked NPC: {}", npc.getName());
  }

  private List<String> fleeShoutsOf(BaseNPC npc) {

    List<String> fleeShouts = npc.getFleeShouts();

    if (fleeShouts.isEmpty()) {

      return List.of();
    }

    List<String> translated = new ArrayList<>(fleeShouts.size());

    for (String shout : fleeShouts) {

      translated.add(I18n.resolve(shout));
    }

    return translated;
  }

  private boolean isPassiveOnAttack(BaseNPC npc) {

    return npc.isPassiveOnAttack();
  }

  public boolean onRightClick(float mouseX, float mouseY) {

    return NameableEntityHandler.handleRightClick(mouseX, mouseY, npcs);
  }

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
    pendingSpawns.clear();
  }

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

        try {

          pendingSpawns.add(entry);
          loaded++;
          continue;

        } catch (Exception ex) {

          log.warn(
              "Failed to load NPC spawn type={} at ({}, {}, {})",
              entry.type,
              entry.x,
              entry.y,
              entry.z,
              ex);
        }
      }

      log.info("Loaded {} NPC spawn(s) from {}", loaded, source.path());

    } catch (Exception e) {

      throw new GameException("Failed to load NPC spawns for " + mapPath, e);
    }
  }

  private void spawnPendingInView(int startX, int endX, int startY, int endY, int margin) {
    var it = pendingSpawns.iterator();
    while (it.hasNext()) {
      NpcSpawnEntry entry = it.next();
      if (entry.x < startX - margin
          || entry.x > endX + margin
          || entry.y < startY - margin
          || entry.y > endY + margin) continue;
      it.remove();
      try {
        BaseNPC npc = NpcFactoryRegistry.create(entry.type, npcContext());
        if (npc == null) continue;
        npc.setSpawnPosition(entry.x * GRID_W, entry.y * GRID_H);
        npc.setStationary(entry.stationary || "SUNDIAL".equalsIgnoreCase(entry.type));
        addNPC(npc);
        npc.onInitialise(lifecyclePlayer);
        npc.onPopup(lifecyclePlayer);
      } catch (Exception ex) {
        log.warn(
            "Failed to load visible NPC spawn type={} at ({}, {})",
            entry.type,
            entry.x,
            entry.y,
            ex);
      }
    }
  }

  private SpawnSource readSpawnsForMap(String mapPath) throws Exception {

    int z = resolveMapZ(mapPath);

    List<NpcSpawnEntry> filtered = new ArrayList<>();

    for (NpcSpawnEntry entry : readJavaSpawns(SpawnRegistry.npcs())) {

      if ("SUNDIAL".equalsIgnoreCase(entry.type) && entry.x == 0 && entry.y == 0 && entry.z == 0) {

        continue;
      }

      if (entry.z == z) {

        filtered.add(entry);
      }
    }
    for (NpcSpawnEntry entry : readJavaSpawns(SpawnRegistry.monsters())) {

      if (entry.z == z && NpcSpawnOwnership.owns(entry.type)) {

        filtered.add(entry);
      }
    }

    return new SpawnSource("java:SpawnRegistry", filtered);
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

  private List<NpcSpawnEntry> readJavaSpawns(
      List<com.perso.T4C.spawn.SpawnDefinition> definitions) {
    List<NpcSpawnEntry> entries = new ArrayList<>(definitions.size());
    for (com.perso.T4C.spawn.SpawnDefinition definition : definitions) {

      NpcSpawnEntry entry = new NpcSpawnEntry();

      entry.type = definition.type();

      entry.x = definition.x();

      entry.y = definition.y();

      entry.z = definition.z();

      entry.stationary = definition.stationary();

      entries.add(entry);
    }

    return entries;
  }

  private NpcContext npcContext() {

    return new NpcContext(questService, () -> companionManager);
  }

  private static final class NpcSpawnEntry {

    String type;

    int x;

    int y;

    int z;

    boolean stationary;
  }

  private record SpawnSource(String path, List<NpcSpawnEntry> entries) {}
}
