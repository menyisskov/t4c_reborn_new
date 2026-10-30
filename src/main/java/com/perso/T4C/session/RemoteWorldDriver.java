package com.perso.T4C.session;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

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
 * screen yet; they are kept as remote {@link Player} puppets, dressed from the public look the
 * snapshot carries (gender and visible worn items, see {@link RemotePlayerLook}), and exposed through {@link #otherPlayers()} for the render hook that
 * comes with wiring.
 */
public final class RemoteWorldDriver implements WorldDriver {
  private static final Logger log = LoggerFactory.getLogger(RemoteWorldDriver.class);
  /** A component of the drawn direction under this share of the other counts as zero (see facing). */
  private static final float FACING_DEAD_ZONE = 0.25f;

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
  private RemoteIntents intents;
  /** The corpse list last drawn (identity), so an unchanged view costs nothing per frame. */
  private List<OnlineConnection.Corpse> lastCorpses;
  private OnlineConnection.MapInfo lastCorpseMap;

  private record NpcEntry(BaseNPC npc, String appearance) {}

  private static final class PlayerEntry {
    final Player player;

    /** The server's route for this player, played back at the pace the server moved it. */
    final RemotePlayerPlayback route = new RemotePlayerPlayback();

    /** What the model was built from; a change rebuilds it. */
    final String look;

    PlayerEntry(Player player, String look) {
      this.player = player;
      this.look = look;
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
    attachIntents();
    OnlineConnection.View view = views.get();
    if (view != null && view.snapshot() != null && view.map() != null) {
      apply(view);
    }
    applyCorpses(view);
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
      attachIntents();
    }
    OnlineConnection.View view = views.get();
    if (view != null && view.snapshot() != null && view.map() != null) {
      OnlineRegion region = view.snapshot().region();
      boolean regionChanged = !Objects.equals(region, lastRegion);
      if (regionChanged || !Objects.equals(view.snapshot().tick(), lastTick)) {
        apply(view);
      }
    }
    applyCorpses(view);
    advance(tick.delta());
  }

  /** Remote players, in snapshot order; the own player is not among them. */
  public List<Player> otherPlayers() {
    List<Player> out = new ArrayList<>(players.size());
    players.values().forEach(entry -> out.add(entry.player));
    return out;
  }

  /** The server actor id of a rendered remote player, or -1 when the puppet is no longer present. */
  public long playerId(Player player) {
    for(var entry:players.entrySet()) if(entry.getValue().player==player) return entry.getKey();
    return -1L;
  }

  /** The server id of a puppet monster of this driver, or -1 (step R3 sends intents by id). */
  public long creatureId(BaseMonster monster) {
    for (Map.Entry<Long, BaseMonster> entry : monsters.entrySet()) {
      if (entry.getValue() == monster) return entry.getKey();
    }
    return -1L;
  }

  /** The server id of a puppet NPC of this driver, or null. */
  public String npcId(BaseNPC npc) {
    for (Map.Entry<String, NpcEntry> entry : npcs.entrySet()) {
      if (entry.getValue().npc() == npc) return entry.getKey();
    }
    return null;
  }

  /** The server id of a puppet loot item of this driver, or null (step R3 sends pickups by id). */
  public UUID lootId(GroundItem item) {
    for (Map.Entry<UUID, GroundItem> entry : loot.entrySet()) {
      if (entry.getValue() == item) return entry.getKey();
    }
    return null;
  }

  /**
   * Routes clicks on this driver's loot and NPCs to the session as intents by server id (the
   * ground-item and NPC click hooks); until bound, a click on a puppet does nothing.
   */
  public void bindIntents(RemoteIntents intents) {
    this.intents = intents;
    attachIntents();
  }

  private void attachIntents() {
    if (intents == null) return;
    if (ground != null) {
      ground.setRemotePickup(
          item -> {
            UUID id = lootId(item);
            return id != null && intents.pickUp(id);
          });
    }
    if (ground != null) {
      ground.setRemoteRecover(id -> id != null && intents.recover(id));
    }
    if (npcRegistry != null) {
      npcRegistry.setRemoteInteract(
          npc -> {
            String id = npcId(npc);
            return id != null && intents.interact(id);
          });
    }
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
      ground.setRemoteCorpses(List.of());
    }
    lastCorpses = null;
    lastCorpseMap = null;
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
    applyPlayers(snapshot.players(), snapshot.tick(), view.ownId(), originX, originY);
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

  /**
   * The character's own corpses in the region it stands in, drawn where it fell. The connection
   * lists only this character's corpses; positions are absolute tiles like the legacy client's
   * recovery marker, and the pixel anchor is the player's (tile centre minus half a tile).
   */
  private void applyCorpses(OnlineConnection.View view) {
    if (ground == null) return;
    List<OnlineConnection.Corpse> corpses = view == null ? null : view.corpses();
    OnlineConnection.MapInfo map = view == null ? null : view.map();
    if (corpses == lastCorpses && map == lastCorpseMap) return;
    lastCorpses = corpses;
    lastCorpseMap = map;
    if (corpses == null || map == null) {
      ground.setRemoteCorpses(List.of());
      return;
    }
    List<GroundItemManager.RemoteCorpse> drawn = new ArrayList<>();
    for (OnlineConnection.Corpse corpse : corpses) {
      if (corpse.world() != map.world()) continue;
      drawn.add(
          new GroundItemManager.RemoteCorpse(
              corpse.id(),
              (float) (corpse.x() - 0.5) * GRID_W,
              (float) (corpse.y() - 0.5) * GRID_H,
              corpse.itemCount() > 0 || corpse.gold() > 0));
    }
    ground.setRemoteCorpses(drawn);
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
      List<OnlineConnection.Actor> actors, long tick, long ownId, int originX, int originY) {
    Set<Long> present = new HashSet<>();
    for (OnlineConnection.Actor actor : actors) {
      if (actor.id() == ownId) continue;
      present.add(actor.id());
      float x = (float) (originX + actor.x() - 0.5) * GRID_W;
      float y = (float) (originY + actor.y() - 0.5) * GRID_H;
      PlayerEntry entry = players.get(actor.id());
      String look = RemotePlayerLook.signature(actor);
      if (entry != null && !entry.look.equals(look)) {
        // Worn items or name changed on the server: rebuild the model, keeping place and walking.
        try {
          Player rebuilt = RemotePlayerLook.build(actor);
          rebuilt.getCoordinates().setX(entry.player.getCoordinates().getX());
          rebuilt.getCoordinates().setY(entry.player.getCoordinates().getY());
          PlayerEntry replacement = new PlayerEntry(rebuilt, look);
          replacement.route.copyFrom(entry.route);
          players.put(actor.id(), replacement);
          entry = replacement;
        } catch (RuntimeException failure) {
          log.warn("Could not rebuild online player {}", actor.id(), failure);
        }
      }
      if (entry == null) {
        try {
          // The look comes from the snapshot's public data (gender, visible worn items, name) and
          // goes through the same appearance pipeline single-player uses; an actor without it
          // keeps the default naked look (female for even ids).
          Player player = RemotePlayerLook.build(actor);
          entry = new PlayerEntry(player, look);
          player.getCoordinates().setX(x);
          player.getCoordinates().setY(y);
          entry.route.accept(tick, x, y);
          players.put(actor.id(), entry);
        } catch (RuntimeException failure) {
          log.warn("Could not build online player {}", actor.id(), failure);
          continue;
        }
      } else {
        entry.route.accept(tick, x, y);
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
      // The puppet walks the server's recorded route at the server's pace (see
      // RemotePlayerPlayback): position, walking flag and facing all come from the drawn motion,
      // so the legs run exactly while the picture moves, with no restart between polls.
      RemotePlayerPlayback route = entry.route;
      route.advance(delta);
      player.getCoordinates().setX(route.x());
      player.getCoordinates().setY(route.y());
      boolean moving = route.moving();
      if (moving) {
        // Facing follows the drawn direction like single-player's 8 directions: a component far
        // smaller than the other (in tile units; the map is drawn at 2:1) is a rounding of the
        // path, not a turn.
        float dx = route.directionX() / GRID_W;
        float dy = route.directionY() / GRID_H;
        if (Math.abs(dy) < FACING_DEAD_ZONE * Math.abs(dx)) dy = 0f;
        if (Math.abs(dx) < FACING_DEAD_ZONE * Math.abs(dy)) dx = 0f;
        player.getMovement().faceToward(0f, 0f, dx, dy);
      }
      player.getMovement().puppetMoving(moving);
      player.getAnimations().update(delta, moving);
    }
  }
}
