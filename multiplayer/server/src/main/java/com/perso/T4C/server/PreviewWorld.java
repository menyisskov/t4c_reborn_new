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

/** Owned exclusively by the simulation thread. Network clients supply directions, never positions. */
public final class PreviewWorld {
  public static final int MAX_PLAYERS = 20;
  public static final int TICK_RATE = 34;
  public static final long INPUT_TIMEOUT_TICKS = 12;
  public static final long SESSION_TIMEOUT_TICKS = TICK_RATE * 30;
  private static final double REQUESTS_PER_SECOND = 60;
  private static final double REQUEST_BURST = 60;
  public record Joined(String token, long id) {}
  public record PlayerView(long id, double x, double y, long sequence, long appearance) {}
  public record Snapshot(long tick, List<PlayerView> players, List<CreatureView> creatures) {}
  public static final class Rejected extends RuntimeException {
    public final int status;
    Rejected(int status, String code) { super(code); this.status = status; }
  }
  private static final class Player {
    long id, sequence = -1, lastSeen, lastInput, appearance;
    int dx, dy;
    double x, y;
    double requestCredits = REQUEST_BURST;
    long budgetTick;
  }
  private final PreviewMap map;
  private final List<CreatureView> creatures;
  record CreatureSpawn(long id, MonsterContent.Spawn spawn, com.perso.T4C.monster.core.MonsterDef definition) {}
  private final List<CreatureSpawn> creatureSpawns;
  private final java.util.Set<Long> inactiveCreatures = new java.util.HashSet<>();
  private final Map<String, Player> players = new LinkedHashMap<>();
  private final SecureRandom random = new SecureRandom();
  private long tick, nextId = 1;

  public PreviewWorld(PreviewMap map) { this(map,null); }

  public PreviewWorld(PreviewMap map, MonsterContent content) {
    this.map = map;
    var residents=new ArrayList<CreatureView>();
    var authored=new ArrayList<CreatureSpawn>();
    var keys=new java.util.HashSet<String>();
    if (content!=null) {
      var spawns=content.region(0,map.originX,map.originY,map.width,map.height);
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
  }

  List<CreatureSpawn> creatureSpawns() { return creatureSpawns; }
  void creatureActive(long id, boolean active) {
    if (active) inactiveCreatures.remove(id); else inactiveCreatures.add(id);
  }

  public Joined join() {
    return joinAt(map.spawnX, map.spawnY);
  }

  /** Server-loaded position only; never expose this as a client coordinate command. */
  Joined joinAt(double x, double y) {
    return joinAt(x,y,nextId);
  }

  Joined joinAt(double x, double y, long appearance) {
    if (players.size() >= MAX_PLAYERS) throw new Rejected(409, "full");
    if (!map.canStand(x,y)) throw new Rejected(409,"position");
    byte[] secret = new byte[32];
    random.nextBytes(secret);
    String token = HexFormat.of().formatHex(secret);
    var player = new Player();
    player.id = nextId++;
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
    player.dx = dx;
    player.dy = dy;
    player.lastInput = tick;
  }

  public Snapshot snapshot(String token) {
    authenticate(token);
    List<PlayerView> result = new ArrayList<>(players.size());
    for (Player p : players.values()) result.add(new PlayerView(p.id, p.x, p.y, p.sequence,p.appearance));
    return new Snapshot(tick, List.copyOf(result),creatures.stream().filter(c -> !inactiveCreatures.contains(c.id())).toList());
  }

  public void leave(String token) { authenticate(token); players.remove(token); }

  PlayerView position(String token) {
    var p = players.get(token);
    return p == null ? null : new PlayerView(p.id, p.x, p.y, p.sequence,p.appearance);
  }

  void remove(String token) { players.remove(token); }

  public void step() {
    tick++;
    players.values().removeIf(p -> tick - p.lastSeen > SESSION_TIMEOUT_TICKS);
    // Fixed server-owned time and speed. Repeated input packets cannot add movement budget.
    double distance = PLAYER_SPEED / GRID_W / TICK_RATE;
    int steps = (int) Math.ceil(distance / 0.2);
    double amount = distance / steps;
    for (Player p : players.values()) {
      if (tick - p.lastInput > INPUT_TIMEOUT_TICKS) { p.dx = 0; p.dy = 0; }
      for (int i = 0; i < steps; i++) {
        double x = p.x + p.dx * amount, y = p.y + p.dy * amount;
        // Swept substeps plus orthogonal checks prevent tunneling and diagonal corner cuts.
        if (map.canStand(x, y) && map.canStand(x, p.y) && map.canStand(p.x, y)) {
          p.x = x; p.y = y;
        } else break;
      }
    }
  }
}
