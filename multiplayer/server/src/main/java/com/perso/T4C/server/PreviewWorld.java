package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.world.MovementRules;

/** Owned exclusively by the simulation thread. Network clients supply directions, never positions. */
public final class PreviewWorld {
  public static final int MAX_PLAYERS = 20;
  public static final int TICK_RATE = 34;
  public static final long INPUT_TIMEOUT_TICKS = 12;
  public static final long SESSION_TIMEOUT_TICKS = TICK_RATE * 30;
  private static final double REQUESTS_PER_SECOND = 60;
  private static final double REQUEST_BURST = 60;
  public record Joined(String token, long id) {}
  /**
   * One actor in a snapshot. {@code name}, {@code gender} and {@code equipment} (worn slot to item key,
   * only items with a visible appearance) are the public look of the character, derived from its
   * committed state; they are null for an actor with no committed character.
   */
  public record PlayerView(long id, double x, double y, long sequence, long appearance, int hp, int maxHp,
      String name, String gender, java.util.Map<String,String> equipment) {
    public PlayerView(long id, double x, double y, long sequence, long appearance, int hp, int maxHp) {
      this(id,x,y,sequence,appearance,hp,maxHp,null,null,null);
    }
  }
  /**
   * {@code window} is the region-local rectangle the lists are complete for and {@code truncated}
   * says a cap cut a list inside it; both are null for a region that is not streamed (whole region).
   */
  public record Snapshot(long tick, List<PlayerView> players, List<CreatureView> creatures,
      AreaOfInterest.Window window, Boolean truncated) {
    public Snapshot(long tick, List<PlayerView> players, List<CreatureView> creatures) {
      this(tick,players,creatures,null,null);
    }
  }
  /** The adapter and client both accept at most this many creatures in one snapshot. */
  static final int MAX_SNAPSHOT_CREATURES = 256;
  record CreaturePosition(double x, double y) {}
  public static final class Rejected extends RuntimeException {
    public final int status;
    Rejected(int status, String code) { super(code); this.status = status; }
  }
  private static final class Player {
    long id, sequence = -1, lastSeen, lastInput, appearance;
    String name, gender;
    java.util.Map<String,String> equipment;
    int dx, dy;
    double x, y;
    int hp=1,maxHp=1;
    long hiddenUntil, detectInvisibleUntil, detectHiddenUntil;
    boolean frozen, movedThisTick;
    final ServerMovement.Glide glide = new ServerMovement.Glide();
    double requestCredits = REQUEST_BURST;
    long budgetTick;
  }
  final int worldId;
  private final PreviewMap map;
  private final ServerMovement.Terrain terrain;
  private MovementRules.Mode movement = ServerMovement.defaultMode();
  /** Live creatures by id, insertion ordered. A region that lists its creatures fills it once; a streamed one grows and shrinks it. */
  private final Map<Long,CreatureView> creatures = new LinkedHashMap<>();
  record CreatureSpawn(long id, MonsterContent.Spawn spawn, com.perso.T4C.monster.core.MonsterDef definition) {}
  private final List<CreatureSpawn> creatureSpawns = new ArrayList<>();
  private final List<CreatureSpawn> creatureSpawnsView = java.util.Collections.unmodifiableList(creatureSpawns);
  private final Map<Long,CreatureSpawn> spawnsById = new java.util.HashMap<>();
  /** Streamed regions only: every hosted spawn of the world (dormant content, ids are index + 1), shared and immutable. */
  private final List<CreatureSpawn> hostedSpawns;
  private final java.util.Set<Long> inactiveCreatures = new java.util.HashSet<>();
  private final Map<Long,Integer> creatureHealth = new java.util.HashMap<>();
  private final Map<Long,CreaturePosition> creaturePositions = new java.util.HashMap<>();
  private final Map<String, Player> players = new LinkedHashMap<>();
  private final SecureRandom random = new SecureRandom();
  private long tick, nextId = 1;

  public PreviewWorld(PreviewMap map) { this(map,null); }

  public PreviewWorld(PreviewMap map, MonsterContent content) { this(map,content,0); }

  PreviewWorld(PreviewMap map,MonsterContent content,int worldId) {
    this.map = map; this.worldId=worldId; this.terrain = new ServerMovement.Terrain(map);
    var keys=new java.util.HashSet<String>();
    // A streamed region lists no resident creatures: the whole world's spawn list is dormant content,
    // activated around players by PersistentCreatures (stage A part 2). The 256 cap below keeps
    // guarding every region that still lists its creatures up front.
    if (content!=null && map.streamed()) {
      var all=new ArrayList<CreatureSpawn>();
      for (var spawn:content.region(worldId,map.originX,map.originY,map.width,map.height)) {
        if (!keys.add(spawn.persistenceKey())) throw new IllegalArgumentException("Duplicate authored spawn identity");
        var definition=spawn.definition()==null ? null : content.monsters().get(spawn.definition());
        if (definition==null || definition.getWalkPattern()==null || definition.getWalkPattern().isBlank())
          throw new IllegalArgumentException("Unsupported creature in streamed region: "+spawn.type());
        all.add(new CreatureSpawn(all.size()+1L,spawn,definition));
      }
      hostedSpawns=java.util.Collections.unmodifiableList(all);
    } else hostedSpawns=null;
    if (content!=null && !map.streamed()) {
      var spawns=content.region(worldId,map.originX,map.originY,map.width,map.height);
      if (spawns.size()>256) throw new IllegalArgumentException("Creature preview capacity exceeded");
      for (var spawn:spawns) {
        if (!keys.add(spawn.persistenceKey())) throw new IllegalArgumentException("Duplicate authored spawn identity");
        var definition=spawn.definition()==null ? null : content.monsters().get(spawn.definition());
        if (definition==null || definition.getWalkPattern()==null || definition.getWalkPattern().isBlank())
          throw new IllegalArgumentException("Unsupported creature in online starting area: "+spawn.type());
        // Preserve authored tile anchors, as MonsterManager does. These actors are stationary:
        // monster movement/footprints, density rules and AI remain separate integration work.
        long id=creatures.size()+1L;
        creatures.put(id,new CreatureView(id,definition.getName(),
            spawn.x()-map.originX,spawn.y()-map.originY,definition.getHealth(),definition.getHealth(),
            definition.getWalkPattern(),definition.getAttackPattern(),definition.getDeathPattern()));
        var hosted=new CreatureSpawn(id,spawn,definition);
        creatureSpawns.add(hosted);spawnsById.put(id,hosted);
      }
    }
    for (var creature : creatures.values())
      creaturePositions.put(creature.id(), new CreaturePosition(creature.x(), creature.y()));
  }

  /** True when this world keeps its creatures dormant and activates them around players. */
  boolean streamedCreatures() { return hostedSpawns!=null; }
  /** Every hosted spawn including dormant ones (streamed), otherwise the same as {@link #creatureSpawns()}. */
  List<CreatureSpawn> hostedSpawns() { return hostedSpawns!=null ? hostedSpawns : creatureSpawnsView; }
  /** The live (active) creature with this id, or null. */
  CreatureSpawn creatureSpawn(long id) { return spawnsById.get(id); }
  int activeCreatureCount() { return creatures.size(); }

  /** Adds a dormant spawn to the live set at its home with full health; the caller then publishes its saved life. */
  void activateCreature(CreatureSpawn spawn) {
    if (hostedSpawns==null || spawnsById.containsKey(spawn.id())) throw new IllegalStateException("Cannot activate creature");
    var d=spawn.definition();
    double x=spawn.spawn().x()-map.originX,y=spawn.spawn().y()-map.originY;
    creatures.put(spawn.id(),new CreatureView(spawn.id(),d.getName(),x,y,d.getHealth(),d.getHealth(),
        d.getWalkPattern(),d.getAttackPattern(),d.getDeathPattern()));
    creaturePositions.put(spawn.id(),new CreaturePosition(x,y));
    creatureSpawns.add(spawn);spawnsById.put(spawn.id(),spawn);
  }

  /** Removes a creature from every live collection; its durable life is the caller's business. */
  void deactivateCreature(long id) {
    if (hostedSpawns==null) throw new IllegalStateException("Cannot deactivate creature");
    var spawn=spawnsById.remove(id);
    if (spawn!=null) creatureSpawns.remove(spawn);
    creatures.remove(id);creaturePositions.remove(id);creatureHealth.remove(id);inactiveCreatures.remove(id);
  }

  /** One simulation window per player (the region's activation interest), frozen arrivals included. */
  List<AreaOfInterest.Window> simulationWindows() {
    var windows=new ArrayList<AreaOfInterest.Window>(players.size());
    for (var p:players.values())
      windows.add(AreaOfInterest.window(p.x,p.y,AreaOfInterest.SIMULATION_HALF_EXTENT,map.width,map.height));
    return windows;
  }

  /** Region-local tile positions of every player here, arrivals still frozen included (nearest-first admission). */
  List<double[]> playerTiles() {
    var tiles=new ArrayList<double[]>(players.size());
    for (var p:players.values()) tiles.add(new double[] {p.x,p.y});
    return tiles;
  }

  /** Which movement rule steps players; defaults to single-player's grid reservation. */
  MovementRules.Mode movementMode() { return movement; }
  void movementMode(MovementRules.Mode mode) {
    movement = java.util.Objects.requireNonNull(mode);
    for (Player p : players.values()) p.glide.clear();
  }

  List<CreatureSpawn> creatureSpawns() { return creatureSpawnsView; }
  PreviewMap map() { return map; }
  CreaturePosition creaturePosition(long id) {
    var position = creaturePositions.get(id);
    if (position == null) throw new IllegalArgumentException("Unknown creature");
    return position;
  }
  /** Committed simulation state only; network commands never supply creature coordinates. */
  void creaturePosition(long id, double x, double y) {
    if (!creaturePositions.containsKey(id)) throw new IllegalArgumentException("Unknown creature");
    // Single-player's creature rule: static foot collision only, without the player's above-foot fold.
    if (!Double.isFinite(x) || !Double.isFinite(y) || map.blocksFoot((int) Math.floor(x), (int) Math.floor(y)))
      throw new IllegalArgumentException("Blocked creature position");
    creaturePositions.put(id, new CreaturePosition(x, y));
  }
  void creatureActive(long id, boolean active) {
    if (active) inactiveCreatures.remove(id); else inactiveCreatures.add(id);
  }
  void creatureHealth(long id, Integer health) {
    var initial=creatures.get(id);
    if (initial==null) throw new IllegalArgumentException("Unknown creature");
    if (health==null) creatureHealth.remove(id);
    else {
      if (health<0 || health>initial.maxHealth()) throw new IllegalArgumentException("Saved creature health exceeds authored health");
      creatureHealth.put(id,health);
    }
  }

  public Joined join() {
    return joinAt(map.spawnX, map.spawnY);
  }

  /** Server-loaded position only; never expose this as a client coordinate command. */
  Joined joinAt(double x, double y) {
    return joinAt(x,y,nextId);
  }

  Joined joinAt(double x, double y, long appearance) { return joinAt(x,y,appearance,nextId); }

  Joined joinAt(double x,double y,long appearance,long globalId) {
    if (players.size() >= MAX_PLAYERS) throw new Rejected(409, "full");
    // Positions saved before grid movement (or mid-glide) may sit between tiles: rest on the centre
    // of the tile they stand on, which was legal when it was saved.
    if (movement == MovementRules.Mode.GRID_RESERVATION) { x = ServerMovement.anchor(x); y = ServerMovement.anchor(y); }
    // Grid rule: any tile the movement rule lets a player rest on is a legal place to rejoin. Its wall-slide step can end on
    // a tile the stricter above-foot fold (canStand) refuses, and refusing it would lock that character out (T4C-0315).
    boolean legal = movement == MovementRules.Mode.GRID_RESERVATION ? map.canRestOnGrid(x,y) : map.canStand(x,y);
    if (!legal) throw new Rejected(409,"position");
    byte[] secret = new byte[32];
    random.nextBytes(secret);
    String token = HexFormat.of().formatHex(secret);
    var player = new Player();
    player.id = globalId;nextId=Math.max(nextId,globalId+1);
    player.appearance=appearance;
    player.x = x;
    player.y = y;
    player.lastSeen = tick;
    player.budgetTick = tick;
    players.put(token, player);
    return new Joined(token, player.id);
  }

  private Player authenticate(String token) {
    Player player = players.get(token);
    if (player == null) throw new Rejected(401, "session");
    player.lastSeen = tick;
    return player;
  }

  /** Network entry points only; internal simulation/storage reads do not spend client credits. */
  void admitRequest(String token) {
    Player player = players.get(token);
    if (player == null) throw new Rejected(401, "session");
    player.requestCredits = Math.min(REQUEST_BURST,
        player.requestCredits + (tick - player.budgetTick) * REQUESTS_PER_SECOND / TICK_RATE);
    player.budgetTick = tick;
    if (player.requestCredits < 1) throw new Rejected(429, "rate");
    player.requestCredits--;
  }

  public void input(String token, long sequence, int dx, int dy) {
    Player player = authenticate(token);
    if (sequence < 0 || dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new Rejected(400, "input");
    if (sequence <= player.sequence) throw new Rejected(409, "sequence");
    player.sequence = sequence;
    player.dx = player.frozen ? 0 : dx;
    player.dy = player.frozen ? 0 : dy;
    player.lastInput = tick;
  }

  public Snapshot snapshot(String token) {
    return snapshot(token,System.currentTimeMillis());
  }

  Snapshot snapshot(String token,long now) {
    authenticate(token);
    List<PlayerView> result = new ArrayList<>(players.size());
    Player observer=players.get(token);
    boolean detectsInvisible=observer.detectInvisibleUntil>now;
    boolean detectsHidden=observer.detectHiddenUntil>now;
    var window=map.streamed() ? AreaOfInterest.window(observer.x,observer.y,AreaOfInterest.SNAPSHOT_HALF_EXTENT,map.width,map.height) : null;
    for (Player p : players.values()) {
      boolean indefiniteHidden=p.hiddenUntil==Long.MAX_VALUE;
      boolean finiteInvisible=!indefiniteHidden && p.hiddenUntil>now;
      if(window!=null && p!=observer && !window.contains(p.x,p.y)) continue;
      if(p==observer || !indefiniteHidden && !finiteInvisible
          || indefiniteHidden && detectsHidden || finiteInvisible && detectsInvisible) result.add(view(p));
    }
    var visible=creatures.values().stream().filter(c -> !inactiveCreatures.contains(c.id()))
        .map(c -> {
          var position=creaturePosition(c.id());
          return new CreatureView(c.id(),c.definition(),position.x(),position.y(),
              creatureHealth.getOrDefault(c.id(),c.health()),c.maxHealth(),
              c.walkPattern(),c.attackPattern(),c.deathPattern());
        });
    if(window==null) return new Snapshot(tick, List.copyOf(result),visible.toList());
    // Streamed: only what is inside the viewer's window, nearest first, id as the stable tie-break.
    var inside=visible.filter(c->window.contains(c.x(),c.y()))
        .sorted(java.util.Comparator.<CreatureView>comparingDouble(c->AreaOfInterest.distance(c.x(),c.y(),observer.x,observer.y))
            .thenComparingLong(CreatureView::id)).toList();
    boolean truncated=inside.size()>MAX_SNAPSHOT_CREATURES;
    return new Snapshot(tick, List.copyOf(result),truncated ? inside.subList(0,MAX_SNAPSHOT_CREATURES) : inside,window,truncated);
  }

  public void leave(String token) { authenticate(token); players.remove(token); }

  PlayerView position(String token) {
    var p = players.get(token);
    return p == null ? null : view(p);
  }

  boolean moving(String token) {
    var p=players.get(token);
    return p!=null && p.movedThisTick;
  }

  int witnesses(String token) {
    var observer=players.get(token);
    if(observer==null) return 0;
    var center=OnlineSneak.WITNESS_RANGE
        * Math.max(com.perso.T4C.config.GameConstants.GRID_W,com.perso.T4C.config.GameConstants.GRID_H);
    int count=0;
    for(var creature:creatures.values()) {
      if(inactiveCreatures.contains(creature.id()) || creatureHealth.getOrDefault(creature.id(),creature.health())<=0) continue;
      var at=creaturePosition(creature.id());
      double dx=(at.x()-observer.x)*com.perso.T4C.config.GameConstants.GRID_W;
      double dy=(at.y()-observer.y)*com.perso.T4C.config.GameConstants.GRID_H;
      if(Math.hypot(dx,dy)<=center) count++;
    }
    return count;
  }

  private static PlayerView view(Player p) {
    return new PlayerView(p.id,p.x,p.y,p.sequence,p.appearance,p.hp,p.maxHp,p.name,p.gender,p.equipment);
  }

  /** Publishes the committed public look of one actor; the map is copied and never shared with the caller. */
  void look(String token,String name,String gender,java.util.Map<String,String> equipment) {
    var p=players.get(token);
    if (p!=null) { p.name=name; p.gender=gender; p.equipment=equipment==null ? null : java.util.Map.copyOf(equipment); }
  }

  void health(String token,int hp,int maxHp) {
    if (hp<0 || maxHp<=0 || hp>maxHp) throw new IllegalArgumentException("Invalid player health");
    var p=players.get(token); if (p!=null) { p.hp=hp; p.maxHp=maxHp; }
  }

  /** Publishes the committed concealment and detection deadlines for snapshot filtering. */
  void visibility(String token,long hiddenUntil,long detectInvisibleUntil,long detectHiddenUntil) {
    var p=players.get(token);
    if(p!=null) {
      p.hiddenUntil=hiddenUntil;
      p.detectInvisibleUntil=detectInvisibleUntil;
      p.detectHiddenUntil=detectHiddenUntil;
    }
  }

  void freeze(String token,boolean frozen) {
    var p=players.get(token);
    if (p!=null) { p.frozen=frozen; if (frozen) { p.dx=0; p.dy=0; } }
  }

  /** Committed server-owned relocation; never accepts network coordinates. */
  void relocate(String token,double x,double y) {
    if (!map.canStand(x,y)) throw new IllegalArgumentException("Blocked relocation");
    var p=players.get(token);
    if (p!=null) { p.x=x; p.y=y; p.dx=0; p.dy=0; p.glide.clear(); }
  }

  void transferTo(PreviewWorld destination,String token,double x,double y) {
    if(!destination.map.canStand(x,y)) throw new IllegalArgumentException("Blocked transfer destination");
    var player=players.get(token);if(player==null) return;
    if(destination!=this && destination.players.containsKey(token)) throw new IllegalStateException("Duplicate actor owner");
    players.remove(token);player.x=x;player.y=y;player.dx=0;player.dy=0;player.glide.clear();player.frozen=true;
    player.lastSeen=destination.tick;player.lastInput=destination.tick;player.budgetTick=destination.tick;
    destination.players.put(token,player);
  }

  void remove(String token) { players.remove(token); }

  public void step() {
    tick++;
    players.values().removeIf(p -> tick - p.lastSeen > SESSION_TIMEOUT_TICKS);
    // Fixed server-owned time and speed. Repeated input packets cannot add movement budget.
    boolean grid = movement == MovementRules.Mode.GRID_RESERVATION;
    double distance = PLAYER_SPEED / GRID_W / TICK_RATE;
    int steps = (int) Math.ceil(distance / 0.2);
    double amount = distance / steps;
    for (Player p : players.values()) {
      double beforeX=p.x,beforeY=p.y;
      p.movedThisTick=false;
      if (p.frozen || p.hp<=0) continue;
      if (tick - p.lastInput > INPUT_TIMEOUT_TICKS) { p.dx = 0; p.dy = 0; }
      if (grid) {
        var moved = ServerMovement.step(map, terrain, p.glide, p.x, p.y, p.dx, p.dy);
        p.x = moved.x(); p.y = moved.y();
        p.movedThisTick=p.x!=beforeX || p.y!=beforeY;
        continue;
      }
      for (int i = 0; i < steps; i++) {
        double x = p.x + p.dx * amount, y = p.y + p.dy * amount;
        // Swept substeps plus orthogonal checks prevent tunneling and diagonal corner cuts.
        if (map.canStand(x, y) && map.canStand(x, p.y) && map.canStand(p.x, y)) {
          p.x = x; p.y = y;
        } else break;
      }
      p.movedThisTick=p.x!=beforeX || p.y!=beforeY;
    }
  }
}
