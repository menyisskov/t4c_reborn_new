package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.combat.MonsterCombatRules;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Stage A part 2 gates against PostgreSQL (WORLD_STREAMING_DESIGN.md sections 4 and 10): exception-only
 * persistence, dormant/active lifecycle, arrival pre-activation and activation latency, on the real world-0
 * collision and content. Disposable database only. The measurements print lines starting with STREAM_.
 */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
class StreamedCreaturesDatabaseTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");
  private static final long PERIOD = 1_000_000_000L / PreviewWorld.TICK_RATE;

  // A pooled store like production: the unpooled test store opens a TCP connection per statement, which exhausts
  // the Windows ephemeral port range over a few hundred statements.
  private static com.perso.T4C.server.persistence.OnlineDatabasePool pool;

  private static OnlineStore store() throws Exception {
    if (pool == null) pool = new com.perso.T4C.server.persistence.OnlineDatabasePool(
        System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
    var store = new OnlineStore(pool);
    store.migrate();
    return store;
  }

  @org.junit.jupiter.api.AfterAll static void closePool() {
    if (pool != null) { pool.close(); pool = null; }
  }

  private static java.sql.Connection jdbc() throws Exception {
    var properties = new java.util.Properties();
    properties.setProperty("user", System.getenv("T4C_DB_USER"));
    properties.setProperty("password", System.getenv("T4C_DB_PASSWORD"));
    return java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL"), properties);
  }

  private static void clearCreatureRows() throws Exception {
    try (var c = jdbc(); var s = c.createStatement()) { s.executeUpdate("DELETE FROM online_spawns"); }
  }

  private static String[] world0Keys;

  /** Rows that say nothing the content files do not (alive, unhurt, no target, no position): there must never be one. */
  private static long pristineRows() throws Exception {
    creatureRows();
    try (var c = jdbc(); var s = c.prepareStatement("SELECT count(*) FROM online_spawns WHERE spawn_key=ANY(?) AND health IS NULL "
        + "AND died_at IS NULL AND target_character IS NULL AND target_spawn_key IS NULL AND current_x IS NULL")) {
      s.setArray(1, c.createArrayOf("varchar", world0Keys));
      try (var rows = s.executeQuery()) {
        rows.next();
        return rows.getLong(1);
      }
    }
  }

  /** Rows of the streamed region only: the five world-1 pieces are not streamed and keep their per-spawn rows. */
  private static long creatureRows() throws Exception {
    if (world0Keys == null) {
      world0Keys = MonsterCatalog.loadBundled().spawns().stream().filter(s -> s.world() == 0)
          .map(com.perso.T4C.online.model.MonsterContent.Spawn::persistenceKey).toArray(String[]::new);
    }
    try (var c = jdbc(); var s = c.prepareStatement("SELECT count(*) FROM online_spawns WHERE spawn_key=ANY(?)")) {
      s.setArray(1, c.createArrayOf("varchar", world0Keys));
      try (var rows = s.executeQuery()) {
        rows.next();
        return rows.getLong(1);
      }
    }
  }

  private static HostedWorlds host(OnlineStore store) throws Exception {
    return new HostedWorlds(HostedRegionPlan.startingSlice(WORLD, true), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled());
  }

  /** Steps at the real 34 Hz until the condition holds; returns elapsed milliseconds. */
  private static double stepPaced(HostedWorlds hosted, BooleanSupplier done, int maxTicks) {
    long started = System.nanoTime(), next = started;
    for (int i = 0; i < maxTicks && !done.getAsBoolean(); i++) {
      hosted.step();
      next += PERIOD;
      long wait = next - System.nanoTime();
      if (wait > 0) java.util.concurrent.locks.LockSupport.parkNanos(wait);
    }
    assertTrue(done.getAsBoolean(), "condition not reached");
    return (System.nanoTime() - started) / 1e6;
  }

  private static void stepTicks(HostedWorlds hosted, int ticks) {
    long next = System.nanoTime();
    for (int i = 0; i < ticks; i++) {
      hosted.step();
      next += PERIOD / 4; // faster than real time; the lifecycle counts ticks, not seconds
      long wait = next - System.nanoTime();
      if (wait > 0) java.util.concurrent.locks.LockSupport.parkNanos(wait);
    }
  }

  /** Centre of the densest window of the given side, counted on 8-tile bins from the hosted spawns' homes. */
  private static int[] densest(List<PreviewWorld.CreatureSpawn> spawns, int side) {
    int bin = 8, w = 6144 / bin, h = 3072 / bin;
    var counts = new int[h + 1][w + 1];
    for (var s : spawns) counts[(int) (s.spawn().y() / bin) + 1][(int) (s.spawn().x() / bin) + 1]++;
    for (int y = 1; y <= h; y++) for (int x = 1; x <= w; x++) counts[y][x] += counts[y - 1][x] + counts[y][x - 1] - counts[y - 1][x - 1];
    int span = side / bin, best = -1, bx = 0, by = 0;
    for (int y = 0; y + span <= h; y++) for (int x = 0; x + span <= w; x++) {
      int n = counts[y + span][x + span] - counts[y][x + span] - counts[y + span][x] + counts[y][x];
      if (n > best) { best = n; bx = x; by = y; }
    }
    return new int[] {bx * bin + side / 2, by * bin + side / 2, best};
  }

  /** Nearest tile a player can stand on, spiralling out from the wanted centre. */
  private static int[] standable(PreviewMap map, int cx, int cy) {
    for (int r = 0; r < 60; r++)
      for (int dy = -r; dy <= r; dy++) for (int dx = -r; dx <= r; dx++)
        if (Math.max(Math.abs(dx), Math.abs(dy)) == r && map.canStand(cx + dx + .5, cy + dy + .5)
            && map.canRestOnGrid(cx + dx + .5, cy + dy + .5)) return new int[] {cx + dx, cy + dy};
    throw new IllegalStateException("No standable tile near " + cx + "," + cy);
  }

  private record Hero(OnlineStore.AccountKey account, OnlineStore.CharacterState character) {}

  private static Hero hero(OnlineStore store, String name, double x, double y) throws Exception {
    var account = store.provisionAccount();
    var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    return new Hero(account, store.create(account.accountId(), name, 0, x, y, progress, 0));
  }

  @Test void startupReadsNoRowsAndAnUntouchedWindowActivatesAndDeactivatesWithZeroWrites() throws Exception {
    var store = store();
    clearCreatureRows();
    long beganStartup = System.nanoTime();
    try (var hosted = host(store)) {
      double startupMs = (System.nanoTime() - beganStartup) / 1e6;
      var region = hosted.regions.getFirst();
      var creatures = region.creatures;
      assertTrue(creatures.streamed());
      assertEquals(0, creatures.stats().active());
      assertTrue(region.world.creatureSpawns().isEmpty());
      int hostedCount = region.world.hostedSpawns().size();
      System.out.printf("STREAM_STARTUP_MS hosted=%d deviatedRows=0 plan+creatures=%.1f%n", hostedCount, startupMs);
      assertEquals(0, creatureRows(), "startup created no per-spawn rows");

      var dense = densest(region.world.hostedSpawns(), 129);
      var spot = standable(region.map, dense[0], dense[1]);
      var hero = hero(store, "Dense walker", spot[0] + .5, spot[1] + .5);
      var joined = hosted.sessions.join(store.acquire(hero.account().secret(), hero.character().id()), System.nanoTime());
      double ms = stepPaced(hosted, () -> creatures.windowActive(spot[0] + .5, spot[1] + .5), 400);
      var stats = creatures.stats();
      System.out.printf("STREAM_ARRIVAL_JOIN window129=%d creatures snapshotWindowActive=%.0f ms active=%d batches=%d maxBatchMs=%.1f%n",
          dense[2], ms, stats.active(), stats.batches(), stats.maxBatchNanos() / 1e6);
      assertTrue(stats.active() > 0);
      assertEquals(0, creatureRows(), "activating an untouched window writes no rows");

      // Walk away: nothing is within reach, the lease ends and the cells release after 15 s.
      var left = hosted.sessions.leave(joined.token());
      stepPaced(hosted, left::isDone, 400);
      stepTicks(hosted, (int) AreaOfInterest.RELEASE_TICKS + 3 * PreviewWorld.TICK_RATE);
      var after = creatures.stats();
      System.out.printf("STREAM_DEACTIVATE active=%d activated=%d deactivated=%d rows=%d%n", after.active(), after.activated(), after.deactivated(), creatureRows());
      assertEquals(0, after.active(), "every untouched creature went dormant again");
      assertEquals(after.activated(), after.deactivated());
      assertEquals(0, creatureRows(), "activate then deactivate wrote zero rows");
      assertTrue(region.world.creatureSpawns().isEmpty());
    }
  }

  @Test void theDensestWindowsActivateWithinTheBudgetAndTheSnapshotWindowIsCompleteBeforeTheActorIsReleased() throws Exception {
    var store = store();
    clearCreatureRows();
    try (var hosted = host(store)) {
      var region = hosted.regions.getFirst();
      var creatures = region.creatures;
      var spawns = region.world.hostedSpawns();
      // The busiest 384 x 384 window, activated by nine arrivals 128 tiles apart (a worst-case teleport tour).
      var dense384 = densest(spawns, 384);
      var rect = new AreaOfInterest.Window(dense384[0] - 192, dense384[1] - 192, 384, 384);
      double ms384;
      long began = System.nanoTime();
      for (int gy = -1; gy <= 1; gy++) for (int gx = -1; gx <= 1; gx++) creatures.requestWindow(dense384[0] + gx * 128, dense384[1] + gy * 128);
      ms384 = stepPaced(hosted, () -> creatures.rectActive(rect), 600);
      var stats = creatures.stats();
      System.out.printf("STREAM_ACTIVATE_384 inWindow=%d activeAfter=%d latency=%.0f ms batches=%d maxBatchMs=%.1f%n",
          dense384[2], stats.active(), ms384, stats.batches(), stats.maxBatchNanos() / 1e6);
      assertTrue(ms384 <= 1000, "busiest 384 x 384 window live within the 1.0 s arrival budget: " + ms384);
      assertEquals(0, creatureRows());
    }
    clearCreatureRows();
    try (var hosted = host(store)) {
      var region = hosted.regions.getFirst();
      var creatures = region.creatures;
      var spawns = region.world.hostedSpawns();
      var dense = densest(spawns, 129);
      var spot = standable(region.map, dense[0], dense[1]);
      var hero = hero(store, "Arriving walker", spot[0] + .5, spot[1] + .5);
      var joined = hosted.sessions.join(store.acquire(hero.account().secret(), hero.character().id()), System.nanoTime());
      assertTrue(hosted.sessions.arrivalHeld(joined.token()));
      hosted.sessions.regionReady(joined.token(), 0); // the client is ready at once
      assertTrue(hosted.sessions.arrivalHeld(joined.token()), "ready, but held until the room is populated");
      var snapshotNow = region.world.snapshot(joined.token());
      assertTrue(snapshotNow.creatures().isEmpty() || snapshotNow.creatures().size() < dense[2]);
      long began = System.nanoTime();
      double ms = stepPaced(hosted, () -> !hosted.sessions.arrivalHeld(joined.token()), 200);
      var window = AreaOfInterest.window(spot[0] + .5, spot[1] + .5, AreaOfInterest.SNAPSHOT_HALF_EXTENT, 6144, 3072);
      var first = region.world.snapshot(joined.token());
      var ids = new HashSet<Long>();
      for (var c : first.creatures()) ids.add(c.id());
      var expected = new HashSet<Long>();
      for (var s : spawns) {
        double x = s.spawn().x() - region.map.originX, y = s.spawn().y() - region.map.originY;
        // Patrol wanders at most 6 tiles from home; anything comfortably inside must be listed.
        if (x >= window.x() + 8 && x < window.right() - 8 && y >= window.y() + 8 && y < window.bottom() - 8) expected.add(s.id());
      }
      System.out.printf("STREAM_ARRIVAL_RELEASE heldFor=%.0f ms snapshotCreatures=%d expectedInside=%d truncated=%s%n",
          ms, first.creatures().size(), expected.size(), first.truncated());
      if (expected.size() <= PreviewWorld.MAX_SNAPSHOT_CREATURES) assertTrue(ids.containsAll(expected), "the first snapshot after release lists every window creature");
      else assertEquals(PreviewWorld.MAX_SNAPSHOT_CREATURES, first.creatures().size(), "capped at the adapter limit, nearest first");
      assertTrue(ms <= 1000, "arrival held no longer than the 1.0 s budget: " + ms);
      System.out.printf("STREAM_ARRIVAL_ROWS deviatedRows=%d pristineRows=%d (aggressive neighbours aggroed the newcomer)%n", creatureRows(), pristineRows());
      assertEquals(0, pristineRows(), "no row for an untouched creature, even with a player standing among them");
      var left = hosted.sessions.leave(joined.token());
      stepPaced(hosted, left::isDone, 400);
    }
  }

  @Test void aWoundedCreatureWritesExactlyOneRowSurvivesARestartAndAHealedOneLeavesNothingStale() throws Exception {
    var store = store();
    clearCreatureRows();
    long id;
    String key;
    int[] stand = null;
    try (var hosted = host(store)) {
      var region = hosted.regions.getFirst();
      var spawns = region.world.hostedSpawns();
      // An isolated, sturdy, attackable creature on the temple's continent.
      PreviewWorld.CreatureSpawn target = null;
      for (var s : spawns) {
        if (s.spawn().stationary() || s.spawn().aggressive() || s.definition().getHealth() < 150 || !OnlineRetaliation.supported(s.definition())) continue;
        int near = 0;
        for (var o : spawns) if (o != s && Math.abs(o.spawn().x() - s.spawn().x()) < 40 && Math.abs(o.spawn().y() - s.spawn().y()) < 40) near++;
        if (near > 0) continue;
        var approach = stand0(region, s);
        // The easiest isolated target (lowest dodge and armour), so a maximum-roll swing lands.
        if (approach != null && (target == null || MonsterCombatRules.resolve(s.definition()).dodge() < MonsterCombatRules.resolve(target.definition()).dodge())) {
          target = s;
          stand = approach;
        }
      }
      assertNotNull(target, "an isolated attackable creature exists");
      id = target.id();
      key = target.spawn().persistenceKey();
      var fighter = hero(store, "Wounder", stand[0] + .5, stand[1] + .5);
      var joined = hosted.sessions.join(store.acquire(fighter.account().secret(), fighter.character().id()), System.nanoTime());
      hosted.sessions.regionReady(joined.token(), 0);
      stepPaced(hosted, () -> !hosted.sessions.arrivalHeld(joined.token()), 300);
      assertTrue(creatureRows() <= 1, "only the isolated creature itself may have deviated (it aggroed the newcomer)");
      // Swing until one lands (a miss is a legal outcome); a client retries a busy or cooling-down refusal exactly like this.
      var refusals = new java.util.TreeMap<String, Integer>();
      var rolls = new Random(7); // seeded; the loop swings until one lands
      var melee = new OnlineMelee(region.map, region.world, hosted.sessions, region.creatures, hosted.items, rolls, System::currentTimeMillis);
      for (int tries = 0; tries < 400; tries++) {
        Integer current = null;
        region.world.snapshot(joined.token()); // a client polling keeps its session alive
        try { current = region.creatures.life(id).health(); } catch (PreviewWorld.Rejected busy) { /* checkpoint in flight */ }
        if (current != null) break;
        try {
          var attack = melee.attack(joined.token(), hosted.sessions.character(joined.token()).revision(), id).toCompletableFuture();
          stepPaced(hosted, attack::isDone, 300);
          attack.get();
        } catch (PreviewWorld.Rejected busy) {
          refusals.merge(busy.getMessage(), 1, Integer::sum);
          if (!Set.of("creature_unavailable", "creature_busy", "revision", "cooldown", "out_of_range_or_blocked").contains(busy.getMessage())) throw busy;
        }
        stepTicks(hosted, 8);
      }
      stepTicks(hosted, 60);
      System.out.println("STREAM_SWINGS refusals=" + refusals + " life=" + region.creatures.life(id)
          + " cooldowns=" + hosted.sessions.character(joined.token()).progress().cooldowns() + " now=" + System.currentTimeMillis());
      long rows = creatureRows();
      System.out.printf("STREAM_WOUNDED rows=%d (one creature touched)%n", rows);
      assertEquals(1, rows, "a wounded creature writes exactly one row");
      var life = region.creatures.life(id);
      assertNotNull(life.health());
      assertTrue(life.health() < target.definition().getHealth(), "wounded");
      var left = hosted.sessions.leave(joined.token());
      stepPaced(hosted, left::isDone, 400);
      stepTicks(hosted, 100);
    }
    assertEquals(1, creatureRows(), "the wound survives shutdown");

    long began = System.nanoTime();
    try (var hosted = host(store)) {
      double startupMs = (System.nanoTime() - began) / 1e6;
      System.out.printf("STREAM_STARTUP_MS hosted=%d deviatedRows=1 plan+creatures=%.1f%n", hosted.regions.getFirst().world.hostedSpawns().size(), startupMs);
      var region = hosted.regions.getFirst();
      var spawn = region.world.hostedSpawns().get((int) id - 1);
      assertEquals(key, spawn.spawn().persistenceKey());
      region.creatures.requestWindow(spawn.spawn().x(), spawn.spawn().y());
      stepPaced(hosted, () -> region.creatures.lifecycleOf(id) == PersistentCreatures.ACTIVE, 200);
      var life = region.creatures.life(id);
      assertNotNull(life.health());
      assertTrue(life.health() < spawn.definition().getHealth(), "the wounded creature came back wounded after a restart");
    }

    // Healed back: the row now says what the content files say. Activation then removes it.
    try (var c = jdbc(); var s = c.prepareStatement("UPDATE online_spawns SET health=NULL,target_character=NULL,target_epoch=NULL,mode='IDLE',"
        + "current_x=NULL,current_y=NULL,next_attack_at=0,state_revision=state_revision+1 WHERE spawn_key=?")) {
      s.setString(1, key);
      assertEquals(1, s.executeUpdate());
    }
    try (var hosted = host(store)) {
      var region = hosted.regions.getFirst();
      var spawn = region.world.hostedSpawns().get((int) id - 1);
      region.creatures.requestWindow(spawn.spawn().x(), spawn.spawn().y());
      stepPaced(hosted, () -> region.creatures.lifecycleOf(id) == PersistentCreatures.ACTIVE, 200);
      stepTicks(hosted, 3 * PreviewWorld.TICK_RATE);
      assertEquals(0, creatureRows(), "a creature back to pristine leaves no stale row");
      assertEquals(1, region.creatures.stats().pristineRowsDeleted());
    }
  }

  private static int[] stand0(HostedWorlds.Region region, PreviewWorld.CreatureSpawn s) {
    for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++) {
      if (dx == 0 && dy == 0) continue;
      int x = s.spawn().x() + dx, y = s.spawn().y() + dy;
      if (region.map.canStand(x + .5, y + .5) && region.map.canRestOnGrid(x + .5, y + .5)
          && region.map.allowsCombat(x + .5, y + .5, s.spawn().x() + .5, s.spawn().y() + .5, false)
          && Math.hypot(dx * com.perso.T4C.config.GameConstants.GRID_W, dy * com.perso.T4C.config.GameConstants.GRID_H)
              <= com.perso.T4C.config.GameConstants.PLAYER_MELEE_ATTACK_RANGE) return new int[] {x, y};
    }
    return null;
  }
}
