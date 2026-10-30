package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_SPEED;
import static com.perso.T4C.config.GameConstants.ENTITY_COLLISION_CLEARANCE_TILES;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_RADIUS;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_PAUSE_MIN;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_PAUSE_MAX;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterPursuitRules;
import com.perso.T4C.online.model.MonsterContent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.UUID;

/** One simulation-thread-owned physical creature. Coordinates are local fractional tiles. */
final class OnlineMonsterPursuit {
  enum Mode { IDLE, PATROL, NATURAL, RETALIATION, CLAN, RETURN }

  record TargetView(UUID character, long epoch, int world, String region, double x, double y,
                    boolean alive, boolean ready, boolean leaseValid) {
    TargetView {
      Objects.requireNonNull(character);
      Objects.requireNonNull(region);
      if (epoch < 0 || !Double.isFinite(x) || !Double.isFinite(y))
        throw new IllegalArgumentException("Invalid target view");
    }
  }

  record ClanView(String key, UUID life, int world, String region, double x, double y,
                  boolean alive, MonsterContent.Clan clan) {
    ClanView {
      Objects.requireNonNull(key);Objects.requireNonNull(life);Objects.requireNonNull(region);
      Objects.requireNonNull(clan);
      if(!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Invalid clan view");
    }
  }

  record Decision(double x, double y, UUID life, Mode mode, UUID target, long targetEpoch,
                  String targetSpawnKey, UUID targetLife,
                  boolean suspended, boolean attackReady, boolean needsReplan) {}

  /** Validated committed state is supplied by the persistence owner after restart or reload. */
  record Seed(UUID life, double x, double y, Mode mode, UUID target, long targetEpoch,
              String targetSpawnKey, UUID targetLife) {
    Seed(UUID life,double x,double y,Mode mode,UUID target,long targetEpoch) {
      this(life,x,y,mode,target,targetEpoch,null,null);
    }
    Seed {
      Objects.requireNonNull(life);
      Objects.requireNonNull(mode);
      if (!Double.isFinite(x) || !Double.isFinite(y)
          || (target == null && targetEpoch >= 0)
          || ((mode == Mode.NATURAL || mode == Mode.RETALIATION) != (target != null))
          || (mode == Mode.CLAN) != (targetSpawnKey!=null)
          || (targetSpawnKey==null)!=(targetLife==null) || target!=null && targetSpawnKey!=null)
        throw new IllegalArgumentException("Invalid committed pursuit state");
    }
  }

  private record Cell(int x, int y) {}
  private record Node(int index, double score) {}
  private enum SearchStatus { FOUND, UNREACHABLE, DEFERRED }
  private record SearchResult(SearchStatus status, List<Cell> path) {}
  private static final int MAX_EXPANSIONS = 8000;
  private static final int DENSE_LIMIT = 1 << 20;
  /** Per-solve search bookkeeping by flat tile index. */
  private interface SearchState {
    double best(int index);
    void best(int index, double cost);
    int parent(int index);
    void parent(int index, int from);
    boolean closed(int index);
    void close(int index);
  }
  private static final class DenseState implements SearchState {
    private final double[] best; private final int[] parent; private final boolean[] closed;
    DenseState(int count) {
      best = new double[count]; Arrays.fill(best, Double.POSITIVE_INFINITY);
      parent = new int[count]; Arrays.fill(parent, -1);
      closed = new boolean[count];
    }
    public double best(int index) { return best[index]; }
    public void best(int index, double cost) { best[index] = cost; }
    public int parent(int index) { return parent[index]; }
    public void parent(int index, int from) { parent[index] = from; }
    public boolean closed(int index) { return closed[index]; }
    public void close(int index) { closed[index] = true; }
  }
  private static final class HashedState implements SearchState {
    private final java.util.HashMap<Integer, Double> best = new java.util.HashMap<>();
    private final java.util.HashMap<Integer, Integer> parent = new java.util.HashMap<>();
    private final java.util.HashSet<Integer> closed = new java.util.HashSet<>();
    public double best(int index) { var cost = best.get(index); return cost == null ? Double.POSITIVE_INFINITY : cost; }
    public void best(int index, double cost) { best.put(index, cost); }
    public int parent(int index) { var from = parent.get(index); return from == null ? -1 : from; }
    public void parent(int index, int from) { parent.put(index, from); }
    public boolean closed(int index) { return closed.contains(index); }
    public void close(int index) { closed.add(index); }
  }
  private static final long MAX_SEARCH_NANOS = 16_000_000L;
  private static final double MAX_STEP_SECONDS = 1.0 / PreviewWorld.TICK_RATE;
  private static final double UNREACHABLE_SECONDS = .75;
  private static final double EPSILON = 1e-7;

  private final PreviewMap map;
  private final MonsterDef definition;
  private final boolean aggressive;
  private final int world;
  private final String region;
  private final MonsterContent.Clan clan;
  private final java.util.Set<MonsterContent.Clan> enemyClans;
  private final boolean canClanAttack;
  private final double homeX, homeY;
  private UUID life, target;
  private String targetSpawnKey;
  private UUID targetLife;
  private long targetEpoch = -1;
  private double x, y, unreachable;
  private Mode mode = Mode.IDLE;
  private List<Cell> path = List.of();
  private int waypoint;
  private Cell pathGoal;
  private boolean pathFailed;
  private double retryDelay;
  private final Random patrolRandom;
  private Double patrolX, patrolY;
  private double patrolPause;

  OnlineMonsterPursuit(PreviewMap map, MonsterContent.Spawn spawn, MonsterDef definition,
                       UUID life, String region, double currentX, double currentY) {
    this(map, spawn, definition, region, new Seed(life, currentX, currentY, Mode.IDLE, null, -1));
  }

  OnlineMonsterPursuit(PreviewMap map, MonsterContent.Spawn spawn, MonsterDef definition,
                       String region, Seed seed) {
    this(map,spawn,definition,region,seed,MonsterContent.Clan.NEUTRAL,java.util.Set.of(),false);
  }

  OnlineMonsterPursuit(PreviewMap map, MonsterContent.Spawn spawn, MonsterDef definition,
                       String region, Seed seed, MonsterContent.Clan clan,
                       java.util.Set<MonsterContent.Clan> enemyClans, boolean attackFramePresent) {
    this.map = Objects.requireNonNull(map);
    this.definition = Objects.requireNonNull(definition);
    this.clan=Objects.requireNonNull(clan);this.enemyClans=java.util.Set.copyOf(enemyClans);
    this.life = seed.life();
    patrolRandom = new Random(seed.life().getMostSignificantBits() ^ seed.life().getLeastSignificantBits());
    this.region = Objects.requireNonNull(region);
    this.world = spawn.world();
    // The authored anchor is the exact integer tile coordinate already published by PreviewWorld.
    homeX = spawn.x() - map.originX;
    homeY = spawn.y() - map.originY;
    if (!map.canStand(homeX, homeY) || !open(seed.x(), seed.y()))
      throw new IllegalArgumentException("Creature is outside walkable region");
    x = seed.x();
    y = seed.y();
    mode = seed.mode();
    target = seed.target();
    targetEpoch = seed.targetEpoch();
    targetSpawnKey=seed.targetSpawnKey();targetLife=seed.targetLife();
    aggressive = !spawn.stationary() && spawn.aggressive();
    canClanAttack=aggressive && definition.isCanAttack() && attackFramePresent;
    if (spawn.stationary() && (Math.abs(x - homeX) > EPSILON || Math.abs(y - homeY) > EPSILON))
      throw new IllegalArgumentException("Stationary creature moved from anchor");
    stationary = spawn.stationary();
    if (stationary && mode != Mode.IDLE)
      throw new IllegalArgumentException("Stationary creature has pursuit state");
    if (mode == Mode.PATROL) pickPatrolTarget();
  }

  private final boolean stationary;

  Decision current() { return decision(false,false,pathGoal==null || pathFailed); }

  /** Only a confirmed player hit may call this; persistence integration owns the commit fence. */
  void provokeCommitted(UUID expectedLife, TargetView attacker) {
    if (!life.equals(expectedLife)) throw new IllegalArgumentException("Stale creature life");
    if (stationary || !definition.isCanAttack() || !eligible(attacker)) return;
    target = attacker.character();
    targetEpoch = attacker.epoch();
    targetSpawnKey=null;targetLife=null;
    mode = Mode.RETALIATION;
    patrolX=null;patrolY=null;patrolPause=0;
    clearNavigation();
  }

  /** A new durable life is the only operation that restores home and clears target/deadlines. */
  void respawn(UUID newLife) {
    life = Objects.requireNonNull(newLife);
    x = homeX;
    y = homeY;
    target = null;
    targetEpoch = -1;
    targetSpawnKey=null;targetLife=null;
    mode = Mode.IDLE;
    patrolX=null;patrolY=null;patrolPause=0;
    clearNavigation();
  }

  /** Simulation grants at most one replan permit globally per fixed step. */
  Decision step(List<TargetView> targets, double seconds,
                boolean allowReplan, boolean frozen) {
    return step(targets,List.of(),seconds,allowReplan,frozen);
  }

  Decision step(List<TargetView> targets, List<ClanView> creatures, double seconds,
                boolean allowReplan, boolean frozen) {
    Objects.requireNonNull(targets);
    Objects.requireNonNull(creatures);
    if (!Double.isFinite(seconds) || seconds <= 0 || seconds > MAX_STEP_SECONDS + EPSILON)
      throw new IllegalArgumentException("Expected one fixed simulation step");
    if (stationary || frozen) return decision(false, false, false);

    TargetView followed = null;
    ClanView clanFollowed = null;
    boolean resetThisStep = false;
    if (targetSpawnKey != null) {
      for (ClanView candidate:creatures) if(candidate.key().equals(targetSpawnKey)) {
        clanFollowed=candidate;break;
      }
      if (clanFollowed==null || !targetLife.equals(clanFollowed.life())
          || !eligibleClan(clanFollowed)
          || !MonsterPursuitRules.withinNaturalLeash((float)distance(clanFollowed.x(),clanFollowed.y()))) {
        resetToReturn();clanFollowed=null;resetThisStep=true;
      }
    }
    if (target != null) {
      for (TargetView candidate : targets) if (candidate.character().equals(target)) {
        followed = candidate;
        break;
      }
      // Absence and a temporarily unready lease retain the durable target without using stale XY.
      if (followed == null || ((!followed.ready() || !followed.leaseValid())
          && (targetEpoch<0 || followed.epoch() == targetEpoch)
          && followed.world() == world && followed.region().equals(region)
          && followed.alive()))
        return decision(true, false, false);
      if (targetEpoch<0 && eligible(followed)) targetEpoch=followed.epoch();
      if (followed.epoch() != targetEpoch || !eligible(followed)
          || !map.combatEndpointsSafe(x, y, followed.x(), followed.y(), false)) {
        resetToReturn();
        followed = null;
        resetThisStep = true;
      } else if (!MonsterPursuitRules.withinPlayerLeash((float) distance(followed.x(), followed.y()),
          mode == Mode.RETALIATION)) {
        resetToReturn();
        followed = null;
        resetThisStep = true;
      }
    }

    if (followed == null && clanFollowed==null && target == null && targetSpawnKey==null
        && (mode != Mode.RETURN || !resetThisStep)) {
      // A reset walks home; a later fixed step may interrupt it with fresh natural aggro.
      clanFollowed=acquireClan(creatures);
      if(clanFollowed!=null) {
        targetSpawnKey=clanFollowed.key();targetLife=clanFollowed.life();mode=Mode.CLAN;
      } else {
        followed = acquire(targets);
      }
      if (clanFollowed!=null || followed != null) {
        if(followed!=null) {
        target = followed.character();
        targetEpoch = followed.epoch();
        mode = Mode.NATURAL;
        }
        patrolX=null;patrolY=null;patrolPause=0;
        clearNavigation();
      }
    }

    boolean attacking = followed != null && attackAllowed(followed)
        || clanFollowed!=null && attackAllowed(clanFollowed);
    if (attacking) return decision(false, true, false);
    if (followed == null && clanFollowed==null && mode == Mode.IDLE) { mode=Mode.PATROL;pickPatrolTarget(); }
    if (followed == null && clanFollowed==null && mode == Mode.PATROL && patrolPause > 0) {
      patrolPause=Math.max(0,patrolPause-seconds);
      return decision(false, false, false);
    }
    if (followed == null && clanFollowed==null && mode == Mode.PATROL && patrolX == null) pickPatrolTarget();
    if (followed == null && clanFollowed==null && mode == Mode.PATROL && patrolX == null) return decision(false,false,false);
    double goalX = clanFollowed!=null ? clanFollowed.x() : followed != null ? followed.x() : mode == Mode.RETURN ? homeX : patrolX;
    double goalY = clanFollowed!=null ? clanFollowed.y() : followed != null ? followed.y() : mode == Mode.RETURN ? homeY : patrolY;
    if (followed == null && clanFollowed==null && mode == Mode.RETURN && distance(goalX, goalY) <= EPSILON) {
      x = homeX; y = homeY; mode = Mode.PATROL;pickPatrolTarget();clearNavigation();
      return decision(false, false, false);
    }
    if (followed == null && clanFollowed==null && mode == Mode.PATROL && distance(goalX,goalY)<2.0) {
      patrolX=null;patrolY=null;
      patrolPause=MONSTER_PATROL_PAUSE_MIN+patrolRandom.nextDouble()*(MONSTER_PATROL_PAUSE_MAX-MONSTER_PATROL_PAUSE_MIN);
      clearNavigation();return decision(false,false,false);
    }
    // An in-range actor waits for its authored cooldown rather than closing to melee distance.
    if (followed != null && inAttackRange(followed) && map.allowsCombat(x, y, goalX, goalY, false))
      return decision(false, false, false);
    if (clanFollowed!=null && inAttackRange(clanFollowed)
        && map.hasLineOfSight(x,y,goalX,goalY)) return decision(false,false,false);
    if(retryDelay>0) retryDelay=Math.max(0,retryDelay-seconds);
    boolean needsReplan = navigationGoalChanged(goalX, goalY) || pathFailed;
    if(pathFailed && retryDelay>0) needsReplan=false;
    if (needsReplan && allowReplan) {
      var search = findPath(new Cell((int) x, (int) y), new Cell((int) goalX, (int) goalY));
      if (search.status() == SearchStatus.DEFERRED) {
        pathGoal = null; path = List.of(); pathFailed = false;
      } else {
        path = search.path();
        pathGoal = new Cell((int) goalX, (int) goalY);
        waypoint = 0;
        pathFailed = search.status() == SearchStatus.UNREACHABLE;
        retryDelay=pathFailed ? UNREACHABLE_SECONDS : 0;
        needsReplan = false;
      }
    }
    boolean progressed = false;
    if (!needsReplan && !pathFailed) progressed = walkPath(goalX, goalY, seconds);
    if (followed != null) {
      if (progressed) unreachable = 0;
      else if (pathFailed) unreachable += seconds;
      if (unreachable > UNREACHABLE_SECONDS) resetToReturn();
    } else if (mode == Mode.PATROL && pathFailed) {
      patrolX=null;patrolY=null;clearNavigation();
    }
    if (mode == Mode.RETURN && distance(homeX, homeY) <= EPSILON) {
      x = homeX; y = homeY; mode = Mode.PATROL;pickPatrolTarget();clearNavigation();
    }
    return decision(false, false, needsReplan || (pathFailed && retryDelay<=0));
  }

  private TargetView acquire(List<TargetView> targets) {
    if (!aggressive || !definition.isCanAttack()) return null;
    return targets.stream().filter(this::eligible)
        .filter(t -> MonsterPursuitRules.inNaturalAcquisitionRange(true,
            (float) distance(t.x(), t.y()), false))
        .filter(t -> map.allowsCombat(x, y, t.x(), t.y(), false))
        .min(Comparator.comparingDouble((TargetView t) -> distance(t.x(), t.y()))
            .thenComparing(TargetView::character)).orElse(null);
  }

  private ClanView acquireClan(List<ClanView> creatures) {
    if(!canClanAttack) return null;
    return creatures.stream().filter(this::eligibleClan)
        .filter(view -> MonsterPursuitRules.withinNaturalLeash((float)distance(view.x(),view.y())))
        .filter(view -> MonsterPursuitRules.inNaturalAcquisitionRange(true,
            (float)distance(view.x(),view.y()),false))
        .filter(view -> map.hasLineOfSight(x,y,view.x(),view.y()))
        .min(Comparator.comparingDouble((ClanView view)->distance(view.x(),view.y()))
            .thenComparing(ClanView::key)).orElse(null);
  }

  private boolean eligibleClan(ClanView view) {
    return view.alive() && view.world()==world && region.equals(view.region())
        && enemyClans.contains(view.clan()) && open(view.x(),view.y());
  }

  private boolean eligible(TargetView view) {
    return view.alive() && view.ready() && view.leaseValid() && view.world() == world
        && region.equals(view.region()) && map.canRestOnGrid(view.x(), view.y());
  }

  private boolean inAttackRange(TargetView view) {
    return inAttackRange((float) distance(view.x(), view.y()));
  }

  private boolean inAttackRange(ClanView view) {
    return inAttackRange((float) distance(view.x(),view.y()));
  }

  private boolean inAttackRange(float pixels) {
    var attacks=definition.getAttacks();
    int tiles=MonsterPursuitRules.tileRange(pixels);
    if(attacks==null || attacks.isEmpty()) return tiles<2;
    return MonsterPursuitRules.withinAttackReach(pixels,attacks)
        && attacks.stream().anyMatch(attack -> attack!=null && attack.isInRange(tiles));
  }

  private boolean attackAllowed(TargetView view) {
    return definition.isCanAttack() && inAttackRange(view)
        && map.allowsCombat(x, y, view.x(), view.y(), false);
  }

  private boolean attackAllowed(ClanView view) {
    return canClanAttack && inAttackRange(view)
        && map.hasLineOfSight(x,y,view.x(),view.y());
  }

  /**
   * Single-player's creature rule: monsters and their A* look at the static foot collision of a tile only
   * ({@code CollisionManager.hasStaticCollisionAtGrid}); the player's one-tile-above footprint fold ({@code canStand})
   * does not apply to them, and players themselves rest by the same foot rule ({@code canRestOnGrid}).
   */
  private boolean open(double px, double py) {
    return Double.isFinite(px) && Double.isFinite(py) && !map.blocksFoot((int) Math.floor(px), (int) Math.floor(py));
  }

  private double distance(double otherX, double otherY) {
    return Math.hypot((otherX - x) * GRID_W, (otherY - y) * GRID_H);
  }

  private boolean navigationGoalChanged(double goalX, double goalY) {
    return pathGoal == null || pathGoal.x() != (int) goalX || pathGoal.y() != (int) goalY;
  }

  private boolean walkPath(double goalX, double goalY, double seconds) {
    double remaining = MONSTER_SPEED * seconds;
    boolean progressed = false;
    for (int steps = 0; steps < 16 && remaining > EPSILON; steps++) {
      double waypointX = waypoint < path.size() ? path.get(waypoint).x() + .5 : goalX;
      double waypointY = waypoint < path.size() ? path.get(waypoint).y() + .5 : goalY;
      double dx = (waypointX - x) * GRID_W, dy = (waypointY - y) * GRID_H;
      double length = Math.hypot(dx, dy);
      if (length <= EPSILON) { if (waypoint < path.size()) waypoint++; else break; continue; }
      double lengthStep = Math.min(Math.min(remaining, length), Math.min(GRID_W, GRID_H) / 5.0);
      double nx = x + dx / length * lengthStep / GRID_W;
      double ny = y + dy / length * lengthStep / GRID_H;
      if (!open(nx, ny) || !open(nx, y) || !open(x, ny)) {
        pathFailed = true;
        retryDelay=.5;
        break;
      }
      x = nx; y = ny; remaining -= lengthStep; progressed = true;
    }
    return progressed;
  }

  private SearchResult findPath(Cell start, Cell goal) {
    if (!navigationClear(goal.x(), goal.y(), true))
      return new SearchResult(SearchStatus.UNREACHABLE, List.of());
    int count = map.width * map.height;
    // Small regions keep flat arrays; a streamed world (about 19 M tiles) would allocate that per solve, so
    // its search state is hashed and bounded by MAX_EXPANSIONS instead.
    SearchState state = count <= DENSE_LIMIT ? new DenseState(count) : new HashedState();
    int source = start.y() * map.width + start.x(), destination = goal.y() * map.width + goal.x();
    var open = new PriorityQueue<Node>(Comparator.comparingDouble(Node::score));
    state.best(source, 0); open.add(new Node(source, 0));
    long deadline = System.nanoTime() + MAX_SEARCH_NANOS;
    int expanded = 0;
    while (!open.isEmpty() && expanded < MAX_EXPANSIONS && System.nanoTime() < deadline) {
      int current = open.remove().index();
      if (state.closed(current)) continue;
      if (current == destination) {
        var reverse = new ArrayList<Cell>();
        for (int at = current; at != source; at = state.parent(at))
          reverse.add(new Cell(at % map.width, at / map.width));
        java.util.Collections.reverse(reverse);
        return new SearchResult(SearchStatus.FOUND, List.copyOf(reverse));
      }
      state.close(current); expanded++;
      int cx = current % map.width, cy = current / map.width;
      for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dy == 0) continue;
        int nx = cx + dx, ny = cy + dy;
        if (!navigationClear(nx, ny, nx == goal.x() && ny == goal.y())
            || (dx != 0 && dy != 0 && (!navigationClear(cx + dx, cy, false)
                || !navigationClear(cx, cy + dy, false)))) continue;
        int index = ny * map.width + nx;
        double cost = state.best(current) + Math.hypot(dx * GRID_W, dy * GRID_H);
        if (cost >= state.best(index)) continue;
        state.best(index, cost); state.parent(index, current);
        open.add(new Node(index, cost + Math.hypot((goal.x() - nx) * GRID_W,
            (goal.y() - ny) * GRID_H)));
      }
    }
    return new SearchResult(open.isEmpty() ? SearchStatus.UNREACHABLE : SearchStatus.DEFERRED,
        List.of());
  }

  /** Offline A* requires static clearance around intermediate cells, but not the destination. */
  private boolean navigationClear(int x, int y, boolean goal) {
    int radius = goal ? 0 : ENTITY_COLLISION_CLEARANCE_TILES;
    for (int yy = y - radius; yy <= y + radius; yy++)
      for (int xx = x - radius; xx <= x + radius; xx++)
        if (map.blocksFoot(xx, yy)) return false;
    return true;
  }

  private void resetToReturn() {
    target = null; targetEpoch = -1; targetSpawnKey=null;targetLife=null;mode = Mode.RETURN;
    patrolX=null;patrolY=null;patrolPause=0;
    clearNavigation();
  }

  private void pickPatrolTarget() {
    clearNavigation();
    for(int attempt=0;attempt<12;attempt++) {
      double angle=patrolRandom.nextDouble()*2*Math.PI;
      double radius=patrolRandom.nextDouble()*MONSTER_PATROL_RADIUS;
      double candidateX=homeX+Math.cos(angle)*radius/GRID_W;
      double candidateY=homeY+Math.sin(angle)*radius/GRID_H;
      if(open(candidateX,candidateY)) { patrolX=candidateX;patrolY=candidateY;return; }
    }
    patrolX=null;patrolY=null;
  }

  private void clearNavigation() {
    path = List.of(); waypoint = 0; pathGoal = null; pathFailed = false; retryDelay=0; unreachable = 0;
  }

  private Decision decision(boolean suspended, boolean attack, boolean replan) {
    return new Decision(x, y, life, mode, target, targetEpoch,targetSpawnKey,targetLife,
        suspended, attack, replan);
  }
}
