package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.PuppetNpc;
import com.perso.T4C.objects.GroundItem;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.online.model.OnlineRegion;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Online world driver, step R2: the world is whatever the server's snapshot says. It builds
 * <em>puppet</em> registries (a {@link MonsterManager} and an {@link NPCManager} in puppet mode),
 * fills them with the real {@link BaseMonster}, {@link BaseNPC} and {@link GroundItem} objects the
 * single-player renderer, hover, selection, health bars and tooltips already draw, and keeps them
 * equal to the latest snapshot. Nothing here simulates: no AI, pathing, loot, experience, quest,
 * sound, respawn or script runs, and no spawn is queued from the local map files.
 *
 * <p>Coordinates: creatures, NPCs and loot are authored tile anchors in region-local tiles (the
 * same convention as the single-player managers), so a position is {@code (origin + x) * tile}.
 * Players are tile centres, so they are placed half a tile up and left, like the own player in
 * {@link ProgressMirror}.
 *
 * <p>The screen owns the ground-item registry, so it is handed in. Other players have no type in the
 * screen yet; they are kept as remote {@link Player} puppets (default look: the snapshot carries
 * only an opaque appearance id) and exposed through {@link #otherPlayers()} for the render hook that
 * comes with wiring.
 */
public final class RemoteWorldDriver implements WorldDriver {
  private static final Logger log = LoggerFactory.getLogger(RemoteWorldDriver.class);
  private static final float PLAYER_EASE_SECONDS = 0.06f;
  private static final float PLAYER_SNAP_DISTANCE = 4f * 32f;

  private final Supplier<OnlineConnection.View> views;
  private final GroundItemManager ground;

  private MonsterManager monsterRegistry;
  private NPCManager npcRegistry;
  private final Map<Long, BaseMonster> monsters = new HashMap<>();
  private final Map<String, NpcEntry> npcs = new HashMap<>();
  private final Map<UUID, GroundItem> loot = new HashMap<>();
  private final Map<Long, PlayerEntry> players = new HashMap<>();
  private final Set<String> unknownDefinitions = new HashSet<>();
  private Long lastTick;
  private OnlineRegion lastRegion;

  private record NpcEntry(BaseNPC npc, String appearance) {}

  private static final class PlayerEntry {
    final Player player;
    final Vector2 target = new Vector2();

    PlayerEntry(Player player) {
      this.player = player;
    }
  }

  /**
   * @param views the connection's latest view (null while there is none)
   * @param ground the screen's ground-item registry, or null when loot is not drawn
   */
  public RemoteWorldDriver(Supplier<OnlineConnection.View> views, GroundItemManager ground) {
    this.views = Objects.requireNonNull(views, "views");
    this.ground = ground;
  }

  @Override
  public WorldEntities loadEntities(WorldEntityInputs inputs) {
    reset();
    monsterRegistry = new MonsterManager(inputs.outlineShader());
    monsterRegistry.setPuppetMode(true);
    npcRegistry = new NPCManager(inputs.outlineShader(), null);
    npcRegistry.setPuppetMode(true);
    OnlineConnection.View view = views.get();
    if (view != null && view.snapshot() != null && view.map() != null) {
      apply(view);
    }
    return new WorldEntities(monsterRegistry, npcRegistry);
  }

  @Override
  public void tickEntities(EntityTick tick) {
    MonsterManager monsterTarget = tick.monsters() != null ? tick.monsters() : monsterRegistry;
    NPCManager npcTarget = tick.npcs() != null ? tick.npcs() : npcRegistry;
    if (monsterTarget != monsterRegistry || npcTarget != npcRegistry) {
      reset();
      monsterRegistry = monsterTarget;
      npcRegistry = npcTarget;
    }
    OnlineConnection.View view = views.get();
    if (view != null && view.snapshot() != null && view.map() != null) {
      OnlineRegion region = view.snapshot().region();
      boolean regionChanged = !Objects.equals(region, lastRegion);
      if (regionChanged || !Objects.equals(view.snapshot().tick(), lastTick)) {
        apply(view);
      }
    }
    advance(tick.delta());
  }

  /** Remote players, in snapshot order; the own player is not among them. */
  public List<Player> otherPlayers() {
    List<Player> out = new ArrayList<>(players.size());
    players.values().forEach(entry -> out.add(entry.player));
    return out;
  }

  /** Definition names the server sent that this client has no monster class for (debug aid). */
  public Set<String> unknownDefinitions() {
    return Set.copyOf(unknownDefinitions);
  }

  // --- applying a snapshot ---

  private void reset() {
    if (monsterRegistry != null) {
      for (BaseMonster monster : monsters.values()) monsterRegistry.despawnMonster(monster);
    }
    if (npcRegistry != null) {
      for (NpcEntry entry : npcs.values()) npcRegistry.removeNPC(entry.npc());
    }
    if (ground != null) {
      for (GroundItem item : loot.values()) ground.removeRemoteItem(item);
    }
    monsters.clear();
    npcs.clear();
    loot.clear();
    players.clear();
    lastTick = null;
    lastRegion = null;
  }

  private void apply(OnlineConnection.View view) {
    OnlineConnection.Snapshot snapshot = view.snapshot();
    OnlineRegion region = snapshot.region();
    // Coordinates in a snapshot belong to its own region; one that is not the map's current epoch
    // is never mixed in (the previous state stays until the matching snapshot arrives).
    if (region != null && !region.equals(view.map().region())) {
      return;
    }
    if (lastRegion != null && !Objects.equals(region, lastRegion)) {
      // A different map generation: nothing of the old one may survive into the new coordinates.
      reset();
    }
    int originX = view.map().originX();
    int originY = view.map().originY();
    if (monsterRegistry != null) applyCreatures(snapshot.creatures(), originX, originY);
    if (npcRegistry != null) applyNpcs(snapshot.npcs(), originX, originY);
    applyLoot(snapshot.loot(), originX, originY);
    applyPlayers(snapshot.players(), view.ownId(), originX, originY);
    lastTick = snapshot.tick();
    lastRegion = region;
  }

  private void applyCreatures(List<CreatureView> views, int originX, int originY) {
    Set<Long> present = new HashSet<>();
    for (CreatureView view : views) {
      present.add(view.id());
      BaseMonster monster = monsters.get(view.id());
      if (monster == null) {
        monster = createMonster(view);
        if (monster == null) continue;
        monsters.put(view.id(), monster);
        monsterRegistry.addMonster(monster);
      }
      monster.puppetPlace((float) (originX + view.x()) * GRID_W, (float) (originY + view.y()) * GRID_H);
      monster.puppetHealth(view.health(), view.maxHealth());
    }
    monsters
        .entrySet()
        .removeIf(
            entry -> {
              if (present.contains(entry.getKey())) return false;
              monsterRegistry.despawnMonster(entry.getValue());
              return true;
            });
  }

  private BaseMonster createMonster(CreatureView view) {
    var definition = MonsterRegistry.findByName(view.definition());
    if (definition == null) {
      if (unknownDefinitions.add(view.definition())) {
        log.warn("No monster class for online creature definition {}", view.definition());
      }
      return null;
    }
    try {
      BaseMonster monster = MonsterRegistry.create(definition, 0f, 0f);
      monster.setPuppet(true);
      return monster;
    } catch (RuntimeException failure) {
      log.warn("Could not build online creature {}", view.definition(), failure);
      return null;
    }
  }

  private void applyNpcs(List<NpcView> views, int originX, int originY) {
    Set<String> present = new HashSet<>();
    for (NpcView view : views) {
      present.add(view.id());
      String appearance = view.spriteBase() + "|" + new java.util.TreeMap<>(view.parts());
      NpcEntry entry = npcs.get(view.id());
      if (entry != null && !entry.appearance().equals(appearance)) {
        npcRegistry.removeNPC(entry.npc());
        npcs.remove(view.id());
        entry = null;
      }
      if (entry == null) {
        BaseNPC npc = createNpc(view);
        if (npc == null) continue;
        entry = new NpcEntry(npc, appearance);
        npcs.put(view.id(), entry);
        npcRegistry.addNPC(npc);
      }
      entry.npc().puppetPlace((float) (originX + view.x()) * GRID_W, (float) (originY + view.y()) * GRID_H);
    }
    npcs.entrySet()
        .removeIf(
            e -> {
              if (present.contains(e.getKey())) return false;
              npcRegistry.removeNPC(e.getValue().npc());
              return true;
            });
  }

  private BaseNPC createNpc(NpcView view) {
    try {
      BaseNPC npc =
          NpcFactoryRegistry.find(view.id()) != null
              ? NpcFactoryRegistry.create(view.id(), new NpcContext(null))
              : null;
      if (npc == null) {
        npc = new PuppetNpc(view.id(), view.displayName(), view.spriteBase(), view.parts());
      }
      npc.setPuppet(true);
      return npc;
    } catch (RuntimeException failure) {
      log.warn("Could not build online NPC {}", view.id(), failure);
      return null;
    }
  }

  private void applyLoot(List<GroundLootView> views, int originX, int originY) {
    if (ground == null) return;
    Set<UUID> present = new HashSet<>();
    for (GroundLootView view : views) {
      present.add(view.id());
      GroundItem item = loot.get(view.id());
      if (item == null) {
        item = GroundItem.ofItem(view.key(), 0f, 0f);
        loot.put(view.id(), item);
        ground.addRemoteItem(item);
      }
      item.getPosition().set((float) (originX + view.x()) * GRID_W, (float) (originY + view.y()) * GRID_H);
    }
    loot.entrySet()
        .removeIf(
            e -> {
              if (present.contains(e.getKey())) return false;
              ground.removeRemoteItem(e.getValue());
              return true;
            });
  }

  private void applyPlayers(
      List<OnlineConnection.Actor> actors, long ownId, int originX, int originY) {
    Set<Long> present = new HashSet<>();
    for (OnlineConnection.Actor actor : actors) {
      if (actor.id() == ownId) continue;
      present.add(actor.id());
      float x = (float) (originX + actor.x() - 0.5) * GRID_W;
      float y = (float) (originY + actor.y() - 0.5) * GRID_H;
      PlayerEntry entry = players.get(actor.id());
      if (entry == null) {
        try {
          Player player = new Player();
          player.setRemote(true);
          entry = new PlayerEntry(player);
          player.getCoordinates().setX(x);
          player.getCoordinates().setY(y);
          entry.target.set(x, y);
          players.put(actor.id(), entry);
        } catch (RuntimeException failure) {
          log.warn("Could not build online player {}", actor.id(), failure);
          continue;
        }
      } else {
        entry.player.getMovement().faceToward(entry.target.x, entry.target.y, x, y);
        entry.target.set(x, y);
      }
      entry.player.setMaxHp(Math.max(1, actor.maxHp()));
      entry.player.setCurrentHp(Math.max(0, Math.min(actor.hp(), Math.max(1, actor.maxHp()))));
    }
    players.keySet().retainAll(present);
  }

  // --- per frame ---

  /** Eases and animates the puppets; the monsters and NPCs advance themselves in puppet mode. */
  private void advance(float delta) {
    if (monsterRegistry != null) {
      for (BaseMonster monster : monsters.values()) monster.update(delta, null, null);
    }
    if (npcRegistry != null) {
      for (NpcEntry entry : npcs.values()) entry.npc().update(delta, null);
    }
    for (PlayerEntry entry : players.values()) {
      Player player = entry.player;
      Vector2 position = player.getPositionVector();
      float distance = position.dst(entry.target);
      boolean moving = false;
      if (distance > PLAYER_SNAP_DISTANCE || distance <= 0.5f) {
        player.getCoordinates().setX(entry.target.x);
        player.getCoordinates().setY(entry.target.y);
      } else {
        moving = true;
        float alpha = 1f - (float) Math.exp(-Math.max(0f, delta) / PLAYER_EASE_SECONDS);
        player.getCoordinates().setX(position.x + (entry.target.x - position.x) * alpha);
        player.getCoordinates().setY(position.y + (entry.target.y - position.y) * alpha);
      }
      player.getAnimations().update(delta, moving);
    }
  }
}
