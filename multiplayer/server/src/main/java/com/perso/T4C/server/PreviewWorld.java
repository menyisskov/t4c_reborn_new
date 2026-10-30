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
  public record Snapshot(long tick, List<PlayerView> players, List<CreatureView> creatures) {}
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
  private final List<CreatureView> creatures;
  record CreatureSpawn(long id, MonsterContent.Spawn spawn, com.perso.T4C.monster.core.MonsterDef definition) {}
  private final List<CreatureSpawn> creatureSpawns;
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
    var residents=new ArrayList<CreatureView>();
    var authored=new ArrayList<CreatureSpawn>();
    var keys=new java.util.HashSet<String>();
    if (content!=null) {
      var spawns=content.region(worldId,map.originX,map.originY,map.width,map.height);
      if (spawns.size()>256) throw new IllegalArgumentException("Creature preview capacity exceeded");
      for (var spawn:spawns) {
        if (!keys.add(spawn.persistenceKey())) throw new IllegalArgumentException("Duplicate authored spawn identity");
        var definition=spawn.definition()==null ? null : content.monsters().get(spawn.definition());
        if (definition==null || definition.getWalkPattern()==null || definition.getWalkPattern().isBlank())
          throw new IllegalArgumentException("Unsupported creature in online starting area: "+spawn.type());
        // Preserve authored tile anchors, as MonsterManager does. These actors are stationary:
        // monster movement/footprints, density rules and AI remain separate integration work.
        residents.add(new CreatureView(residents.size()+1L,definition.getName(),
            spawn.x()-map.originX,spawn.y()-map.originY,definition.getHealth(),definition.getHealth(),
            definition.getWalkPattern(),definition.getAttackPattern(),definition.getDeathPattern()));
        authored.add(new CreatureSpawn(residents.size(),spawn,definition));
      }
    }
    creatures=List.copyOf(residents);
    creatureSpawns=List.copyOf(authored);
    for (var creature : creatures)
      creaturePositions.put(creature.id(), new CreaturePosition(creature.x(), creature.y()));
  }

  /** Which movement rule steps players; defaults to single-player's grid reservation. */
  MovementRules.Mode movementMode() { return movement; }
  void movementMode(MovementRules.Mode mode) {
    movement = java.util.Objects.requireNonNull(mode);
    for (Player p : players.values()) p.glide.clear();
  }

  List<CreatureSpawn> creatureSpawns() { return creatureSpawns; }
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
    var initial=creatures.get(Math.toIntExact(id-1));
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
    for (Player p : players.values()) {
      boolean indefiniteHidden=p.hiddenUntil==Long.MAX_VALUE;
      boolean finiteInvisible=!indefiniteHidden && p.hiddenUntil>now;
      if(p==observer || !indefiniteHidden && !finiteInvisible
          || indefiniteHidden && detectsHidden || finiteInvisible && detectsInvisible) result.add(view(p));
    }
    return new Snapshot(tick, List.copyOf(result),creatures.stream().filter(c -> !inactiveCreatures.contains(c.id()))
        .map(c -> {
          var position=creaturePosition(c.id());
          return new CreatureView(c.id(),c.definition(),position.x(),position.y(),
              creatureHealth.getOrDefault(c.id(),c.health()),c.maxHealth(),
              c.walkPattern(),c.attackPattern(),c.deathPattern());
        }).toList());
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
    for(var creature:creatures) {
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
