package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Opt-in measurements (T4C_MEASURE_STREAM=1) for WORLD_STREAMING_DESIGN.md section 12, in the server process with a
 * real PostgreSQL and the real world-0 content. Paced at the real 34 Hz. Lines start with MEASURE_.
 */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "T4C_MEASURE_STREAM", matches = "1")
class StreamedMeasurementTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");
  private static final long PERIOD = 1_000_000_000L / PreviewWorld.TICK_RATE;
  private static com.perso.T4C.server.persistence.OnlineDatabasePool pool;

  private static OnlineStore store() throws Exception {
    if (pool == null) pool = new com.perso.T4C.server.persistence.OnlineDatabasePool(
        System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
    var store = new OnlineStore(pool);
    store.migrate();
    return store;
  }

  @org.junit.jupiter.api.AfterAll static void close() { if (pool != null) pool.close(); }

  private static void clearRows() throws Exception {
    var properties = new java.util.Properties();
    properties.setProperty("user", System.getenv("T4C_DB_USER"));
    properties.setProperty("password", System.getenv("T4C_DB_PASSWORD"));
    try (var c = java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL"), properties); var s = c.createStatement()) {
      s.executeUpdate("DELETE FROM online_spawns");
    }
  }

  private record Tick(double meanMillis, double maxMillis, int over) {}

  /** Runs paced ticks; the first {@code warm} are not counted. */
  private static Tick pace(HostedWorlds hosted, int warm, int counted, Runnable each) {
    long next = System.nanoTime(), spent = 0, worst = 0;
    int over = 0, n = 0;
    for (int i = 0; i < warm + counted; i++) {
      long began = System.nanoTime();
      if (each != null) each.run();
      hosted.step();
      long took = System.nanoTime() - began;
      if (i >= warm) { spent += took; worst = Math.max(worst, took); n++; if (took > PERIOD) over++; }
      next += PERIOD;
      long wait = next - System.nanoTime();
      if (wait > 0) java.util.concurrent.locks.LockSupport.parkNanos(wait);
    }
    return new Tick(spent / 1e6 / n, worst / 1e6, over);
  }

  @Test void idleTickWithNActiveCreaturesAndNoPlayers() throws Exception {
    var store = store();
    clearRows();
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD, true), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var region = hosted.regions.getFirst();
      var creatures = region.creatures;
      var spawns = region.world.hostedSpawns();
      // Densest centres first, so N grows quickly: (n_target, windows requested).
      var centres = new ArrayList<int[]>();
      for (var s : spawns) centres.add(new int[] {s.spawn().x(), s.spawn().y()});
      var random = new Random(11);
      var held = new ArrayList<int[]>();
      var targets = new int[] {0, 100, 250, 500, 1000};
      var tick = pace(hosted, 300, 600, null);
      System.out.printf("MEASURE_IDLE active=%d mean=%.4f ms max=%.2f ms over=%d%n", creatures.stats().active(), tick.meanMillis(), tick.maxMillis(), tick.over());
      for (int t = 1; t < targets.length; t++) {
        int guard = 0;
        while (creatures.stats().active() + creatures.stats().loading() + creatures.stats().queued() < targets[t] && guard++ < 4000) {
          var c = centres.get(random.nextInt(centres.size()));
          held.add(c);
          creatures.requestWindow(c[0], c[1]);
          hosted.step();
          if (creatures.stats().active() + creatures.stats().queued() + creatures.stats().loading() > 1500) break;
        }
        int[] last = {0};
        // With no player the windows would release after 15 s; the requests are renewed so N stays active.
        Runnable renew = () -> { if (++last[0] % 200 == 0) for (var c : held) creatures.requestWindow(c[0], c[1]); };
        pace(hosted, 400, 1, renew);
        tick = pace(hosted, 0, 600, renew);
        var stats = creatures.stats();
        System.out.printf("MEASURE_IDLE active=%d (target %d, queued=%d) mean=%.4f ms max=%.2f ms over=%d heapMB=%d%n",
            stats.active(), targets[t], stats.queued(), tick.meanMillis(), tick.maxMillis(), tick.over(),
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20);
      }
    }
  }

  @Test void stateBytesAtTheStarterTownAndInTheDensestWindow() throws Exception {
    var store = store();
    clearRows();
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD, true), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var region = hosted.regions.getFirst();
      var creatures = region.creatures;
      var gson = new Gson();
      for (int[] spot : new int[][] {{2944, 1059}, densest(region.world.hostedSpawns())}) {
        var s = standable(region.map, spot[0], spot[1]);
        var account = store.provisionAccount();
        var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
        var created = store.create(account.accountId(), "Bytes " + s[0], 0, s[0] + .5, s[1] + .5, progress, 0);
        var joined = hosted.sessions.join(store.acquire(account.secret(), created.id()), System.nanoTime());
        hosted.sessions.regionReady(joined.token(), 0);
        pace(hosted, 0, 200, null);
        var snapshot = region.world.snapshot(joined.token());
        int bytes = gson.toJson(snapshot).getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        System.out.printf("MEASURE_STATE at %d,%d creatures=%d truncated=%s jsonBytes(players+creatures+window)=%d%n",
            s[0], s[1], snapshot.creatures().size(), snapshot.truncated(), bytes);
        var left = hosted.sessions.leave(joined.token());
        pace(hosted, 0, 60, null);
        assertTrue(left.isDone());
      }
    }
  }

  @Test void twentyPlayersSpreadOverWorldZeroHeapCpuAndTick() throws Exception {
    var store = store();
    clearRows();
    int seconds = Integer.parseInt(System.getenv().getOrDefault("T4C_MEASURE_SPREAD_SECONDS", "120"));
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD, true), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var region = hosted.regions.getFirst();
      var spawns = region.world.hostedSpawns();
      // Twenty well separated busy fields.
      int cell = 32;
      var counts = new int[3072 / cell][6144 / cell];
      for (var s : spawns) counts[s.spawn().y() / cell][s.spawn().x() / cell]++;
      var order = new ArrayList<int[]>();
      for (int y = 0; y < counts.length; y++) for (int x = 0; x < counts[0].length; x++) if (counts[y][x] > 0) order.add(new int[] {x, y, counts[y][x]});
      order.sort((a, b) -> b[2] - a[2]);
      var spots = new ArrayList<int[]>();
      for (var c : order) {
        if (spots.size() == 20) break;
        int cx = c[0] * cell + cell / 2, cy = c[1] * cell + cell / 2;
        if (spots.stream().anyMatch(sp -> Math.max(Math.abs(sp[0] - cx), Math.abs(sp[1] - cy)) < 300)) continue;
        spots.add(standable(region.map, cx, cy));
      }
      assertEquals(20, spots.size());
      var tokens = new ArrayList<String>();
      for (int i = 0; i < 20; i++) {
        var account = store.provisionAccount();
        var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(i + 1)).progress();
        var created = store.create(account.accountId(), "Spread " + i, 0, spots.get(i)[0] + .5, spots.get(i)[1] + .5, progress, 0);
        var joined = hosted.sessions.join(store.acquire(account.secret(), created.id()), System.nanoTime());
        hosted.sessions.regionReady(joined.token(), 0);
        tokens.add(joined.token());
      }
      var random = new Random(5);
      var cpu = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
      long cpuBefore = cpu.getProcessCpuTime(), wallBefore = System.nanoTime();
      long[] seq = new long[20];
      long[] heapPeak = {0};
      int[] n = {0};
      var tick = pace(hosted, 340, seconds * PreviewWorld.TICK_RATE, () -> {
        if (n[0]++ % 17 == 0) for (int i = 0; i < 20; i++) {
          int dx = random.nextInt(3) - 1, dy = random.nextInt(3) - 1;
          try { region.world.input(tokens.get(i), ++seq[i], dx, dy); region.world.snapshot(tokens.get(i)); }
          catch (PreviewWorld.Rejected ignored) { /* a respawn moved this actor */ }
        }
        if (n[0] % 340 == 0) heapPeak[0] = Math.max(heapPeak[0], Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
      });
      double cpuShare = (cpu.getProcessCpuTime() - cpuBefore) / (double) (System.nanoTime() - wallBefore);
      var stats = region.creatures.stats();
      System.out.printf("MEASURE_SPREAD20 seconds=%d mean=%.3f ms max=%.1f ms over=%d cpu=%.1f%% of one core heapPeakMB=%d active=%d maxActive=%d activated=%d deactivated=%d batches=%d maxBatchMs=%.1f rows=%s%n",
          seconds, tick.meanMillis(), tick.maxMillis(), tick.over(), cpuShare * 100, heapPeak[0] >> 20, stats.active(), stats.maxActiveSeen(),
          stats.activated(), stats.deactivated(), stats.batches(), stats.maxBatchNanos() / 1e6, "see MEASURE_ROWS");
    }
  }

  private static int[] densest(List<PreviewWorld.CreatureSpawn> spawns) {
    int bin = 8, w = 6144 / bin, h = 3072 / bin;
    var counts = new int[h + 1][w + 1];
    for (var s : spawns) counts[s.spawn().y() / bin + 1][s.spawn().x() / bin + 1]++;
    for (int y = 1; y <= h; y++) for (int x = 1; x <= w; x++) counts[y][x] += counts[y - 1][x] + counts[y][x - 1] - counts[y - 1][x - 1];
    int span = 97 / bin, best = -1, bx = 0, by = 0;
    for (int y = 0; y + span <= h; y++) for (int x = 0; x + span <= w; x++) {
      int k = counts[y + span][x + span] - counts[y][x + span] - counts[y + span][x] + counts[y][x];
      if (k > best) { best = k; bx = x; by = y; }
    }
    return new int[] {bx * bin + 48, by * bin + 48};
  }

  private static int[] standable(PreviewMap map, int cx, int cy) {
    for (int r = 0; r < 80; r++)
      for (int dy = -r; dy <= r; dy++) for (int dx = -r; dx <= r; dx++)
        if (Math.max(Math.abs(dx), Math.abs(dy)) == r && map.canStand(cx + dx + .5, cy + dy + .5) && map.canRestOnGrid(cx + dx + .5, cy + dy + .5))
          return new int[] {cx + dx, cy + dy};
    throw new IllegalStateException("No standable tile near " + cx + "," + cy);
  }
}
