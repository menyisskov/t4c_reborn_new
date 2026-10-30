package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Stage A part 1 against PostgreSQL: a character far outside the old crop joins and walks the streamed world 0. */
@EnabledIfEnvironmentVariable(named = "T4C_DB_URL", matches = ".+")
class HostedWorld0DatabaseTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");

  @Test void aCharacterSavedAtTheFarShoreIsRefusedByTheDefaultPlanAndJoinsAndWalksWhenWorld0Streams() throws Exception {
    var store = OnlineStore.fromEnvironment();
    store.migrate();
    var account = store.provisionAccount();
    var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(3)).progress();
    var created = store.create(account.accountId(), "Far shore walker", 0, 3700.5, 1450.5, progress, 0);
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var session = store.acquire(account.secret(), created.id());
      assertEquals("unhosted_region", assertThrows(PreviewWorld.Rejected.class,
          () -> hosted.sessions.join(session, System.nanoTime())).getMessage(), "the shipped plan keeps refusing it");
      store.release(session);
    }
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD, true), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      var joined = hosted.sessions.join(store.acquire(account.secret(), created.id()), System.nanoTime());
      hosted.sessions.regionReady(joined.token(), 0);
      var region = hosted.actor(joined.token());
      assertEquals("start", region.spec.key());
      assertEquals(6144, region.map.width);
      assertTrue(region.creatures.pendingReplans().isEmpty());
      var before = region.world.position(joined.token());
      assertEquals(3700.5, before.x());
      assertEquals(1450.5, before.y());
      long sequence = 0;
      for (int i = 0; i < 400; i++) {
        region.world.input(joined.token(), ++sequence, 1, 0);
        hosted.step();
      }
      var after = region.world.position(joined.token());
      assertTrue(after.x() > before.x() + 5, "walked east over the shared collision: " + after.x());
      var snapshot = region.world.snapshot(joined.token());
      assertEquals((int) after.x() - 48, snapshot.window().x());
      assertEquals(1, snapshot.players().size());
      var left = hosted.sessions.leave(joined.token());
      await(hosted, left::isDone);
      left.get();
      var saved = store.load(account.accountId(), created.id());
      assertEquals(0, saved.world());
      assertTrue(saved.x() > 3700.5, "the walk was persisted at its absolute world coordinate: " + saved.x());
    }
  }

  /** Opt-in measurement (T4C_MEASURE_IDLE=1): paced at the real 34 Hz so patrol and checkpoint writes behave as in production. */
  @org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "T4C_MEASURE_IDLE", matches = "1")
  @Test void idleTickCostWithNoPlayersWithTheFlagOffAndOn() throws Exception {
    var store = OnlineStore.fromEnvironment();
    store.migrate();
    double off, on;
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      off = idleMeanMillis(hosted);
    }
    try (var hosted = new HostedWorlds(HostedRegionPlan.startingSlice(WORLD, true), store, ItemCatalog.loadBundled(), MonsterCatalog.loadBundled())) {
      on = idleMeanMillis(hosted);
    }
    System.out.printf("IDLE_TICK_MS off=%.4f on=%.4f%n", off, on);
    assertTrue(on < 2.0, "design budget for an idle full-world tick is 2.0 ms mean: " + on);
    assertTrue(off < 2.0, "baseline: " + off);
  }

  private static double idleMeanMillis(HostedWorlds hosted) {
    long period = 1_000_000_000L / PreviewWorld.TICK_RATE;
    long spent = 0, next = System.nanoTime();
    int measured = 0;
    for (int i = 0; i < 1500; i++) { // the first 300 ticks (about 9 s) warm the JIT and are not counted
      long began = System.nanoTime();
      hosted.step();
      long took = System.nanoTime() - began;
      if (i >= 300) { spent += took; measured++; }
      next += period;
      long wait = next - System.nanoTime();
      if (wait > 0) java.util.concurrent.locks.LockSupport.parkNanos(wait);
    }
    return spent / 1e6 / measured;
  }

  private static void await(HostedWorlds hosted, BooleanSupplier done) throws Exception {
    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(8);
    while (!done.getAsBoolean() && System.nanoTime() < deadline) { hosted.step(); Thread.sleep(5); }
    assertTrue(done.getAsBoolean());
  }
}
