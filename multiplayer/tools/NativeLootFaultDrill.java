package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.server.persistence.OnlineStore;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Lost-acknowledgement and restart drill for the LOOT CLAIM and CORPSE RECOVERY requests (T4C-0335, milestone M2). The
 * sibling of {@link NativeFaultDrill}'s {@code greet-kill}: one fresh character per round joins a real ServerMain child,
 * the request is handed to the real {@code OnlineConnection} adapter, and the child is hard-killed a fixed number of
 * milliseconds later so the kill races the commit. With the database static, the committed outcome must be exactly
 * "applied" or exactly "not applied" (never half), after the restart plus lease expiry the same request is retried
 * through a new adapter, and the final state must hold the item(s) exactly once.
 *
 * <p>The ground-loot row and the corpse row are inserted by this operator tool before the first server start (the one
 * way to get a corpse without lethal combat, the same seeding the tamper probe uses); the claim, the recovery, the kill
 * and the restart are the real paths. Refuses any database that is not a loopback {@code t4c_acc_<id>} cluster, protected
 * ports, and any port already listening. It starts and kills only its own child.
 *
 * <p>Usage: {@code NativeLootFaultDrill RUNTIME_DIR JAVA_EXE SERVER_PORT NEW_OUTPUT_DIR} (runtime = a copied folder with
 * {@code t4c-server.jar}, {@code lib/*} and {@code assets/maps/...}); the probe classpath is the checkout's classes.
 */
public final class NativeLootFaultDrill {
  private static final Gson GSON = new Gson();
  private static final String POTION = "item.light_healing_potion";
  private static final int[] DELAYS = {0, 10, 25, 50, 80, 120, 200, 350, 600};
  private static final long CORPSE_GOLD = 50;

  private record Round(String kind, int delay, UUID character, String secret, UUID target, String name) {
    String describe() { return kind + " delay " + delay + " ms"; }
  }

  private static Process server;
  private static int serverNumber;
  private static Path runtime, output, javaExe;
  private static int port;
  private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
  private static final List<String> LINES = new ArrayList<>();
  private static boolean failed;

  public static void main(String[] args) throws Exception {
    if (args.length != 4) throw new IllegalArgumentException("Usage: NativeLootFaultDrill RUNTIME_DIR JAVA_EXE SERVER_PORT NEW_OUTPUT_DIR");
    runtime = Path.of(args[0]).toRealPath();
    javaExe = Path.of(args[1]).toRealPath();
    port = Integer.parseInt(args[2]);
    output = Path.of(args[3]).toAbsolutePath().normalize();
    if (Set.of(18765, 18766, 18770, 18771, 55432, 55433).contains(port) || port < 1024 || port > 65535) throw new IllegalArgumentException("Protected or invalid port");
    if (Files.exists(output)) throw new IllegalArgumentException("Output must be new");
    String url = System.getenv("T4C_DB_URL");
    if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/t4c_acc_[0-9a-f]{12}(?:\\?.*)?"))
      throw new IllegalArgumentException("Refusing: only a loopback t4c_acc_* acceptance database is allowed");
    int dbPort = Integer.parseInt(url.replaceAll("^jdbc:postgresql://127\\.0\\.0\\.1:([0-9]+)/.*$", "$1"));
    if (Set.of(55432, 55433, 18765, 18766, 18770, 18771).contains(dbPort)) throw new IllegalArgumentException("Protected database port");
    for (String name : List.of("T4C_DB_USER", "T4C_DB_PASSWORD"))
      if (System.getenv(name) == null || System.getenv(name).isBlank()) throw new IllegalArgumentException("Private database environment incomplete");
    try (var probe = new java.net.ServerSocket(port, 1, java.net.InetAddress.getByName("127.0.0.1"))) { /* free */ }
    Files.createDirectories(output);
    Path map = runtime.resolve("assets/maps/worldmap/worldmap.colbin");
    var rounds = new ArrayList<Round>();
    try (var store = OnlineStore.fromEnvironment()) {
      store.migrate();
      var plan = HostedRegionPlan.startingSlice(map);
      var aelric = NpcInteractionCatalog.loadBundled().require("WardenAelric");
      double[] tile = standable(plan, aelric.world(), aelric.x(), aelric.y());
      for (String kind : List.of("claim", "recover"))
        for (int delay : DELAYS) {
          var kit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(335L + rounds.size()));
          var key = store.provisionAccount();
          String name = "Fault " + kind.charAt(0) + String.format("%03d", delay) + UUID.randomUUID().toString().substring(0, 4);
          var created = store.create(key.accountId(), name, aelric.world(), tile[0], tile[1], kit.progress(), kit.gold());
          UUID target = UUID.randomUUID();
          seed(kind, created.id(), tile, target);
          rounds.add(new Round(kind, delay, created.id(), key.secret(), target, name));
        }
    }
    say("provisioned " + rounds.size() + " characters; loot and corpse rows seeded at tile " + tile(plan0(map), 0));
    try {
      startServer();
      var before = new java.util.LinkedHashMap<UUID, int[]>();
      for (var round : rounds) {
        before.put(round.character(), counts(round.character()));
        runRound(round, before.get(round.character()));
        stopServer();
        report(round, before.get(round.character()), "after kill, before restart");
        startServer();
      }
      say("waiting 38 s so every lease held by a killed session has really expired");
      Thread.sleep(38_000);
      for (var round : rounds) {
        retry(round);
        report(round, before.get(round.character()), "after retry");
        verifyFinal(round, before.get(round.character()));
      }
      stopServerGracefully();
    } finally {
      if (server != null && server.isAlive()) server.destroyForcibly();
      Files.write(output.resolve("report.txt"), LINES, StandardCharsets.UTF_8);
    }
    say(failed ? "RESULT: FAIL" : "RESULT: PASS");
    Files.write(output.resolve("report.txt"), LINES, StandardCharsets.UTF_8);
    if (failed) System.exit(1);
  }

  private static String plan0(Path map) { return map.getFileName().toString(); }
  private static String tile(String a, int b) { return a; }

  // ---------------------------------------------------------------- one round

  private static void runRound(Round round, int[] start) throws Exception {
    try (var connection = new OnlineConnection(URI.create("http://127.0.0.1:" + port), new OnlineConnection.Credentials(round.secret(), round.character()))) {
      long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
      while (connection.view() == null && !connection.status().equals("disconnected") && System.nanoTime() < until) Thread.sleep(25);
      if (connection.view() == null) throw new AssertionError("Adapter did not join: " + round.describe());
      connection.confirmRegionReady(connection.view().map().locationEpoch());
      until = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
      boolean seen = false;
      while (!seen && System.nanoTime() < until) {
        var view = connection.view();
        seen = round.kind().equals("claim")
            ? view.snapshot() != null && view.snapshot().loot() != null && view.snapshot().loot().stream().anyMatch(l -> l.id().equals(round.target()))
            : view.corpses().stream().anyMatch(c -> c.id().equals(round.target()));
        if (!seen) Thread.sleep(100);
      }
      if (!seen) throw new AssertionError("The seeded " + round.kind() + " target never appeared in the adapter's view: " + round.describe());
      var pending = (round.kind().equals("claim") ? connection.pickUp(round.target()) : connection.recover(round.target())).toCompletableFuture();
      if (round.delay() > 0) Thread.sleep(round.delay());
      server.destroyForcibly();
      String outcome;
      try { var r = pending.get(8, TimeUnit.SECONDS); outcome = r.accepted() ? "ACKNOWLEDGED" : "REFUSED(" + r.messageKey() + ")"; }
      catch (Exception lost) { outcome = "LOST"; }
      say("ROUND " + round.describe() + ": reply " + outcome);
    }
  }

  private static void retry(Round round) throws Exception {
    for (int attempt = 0; attempt < 6; attempt++) {
      try (var connection = new OnlineConnection(URI.create("http://127.0.0.1:" + port), new OnlineConnection.Credentials(round.secret(), round.character()))) {
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (connection.view() == null && !connection.status().equals("disconnected") && System.nanoTime() < until) Thread.sleep(25);
        if (connection.view() == null) { Thread.sleep(3000); continue; }
        connection.confirmRegionReady(connection.view().map().locationEpoch());
        Thread.sleep(1500);
        var r = (round.kind().equals("claim") ? connection.pickUp(round.target()) : connection.recover(round.target())).toCompletableFuture().get(8, TimeUnit.SECONDS);
        say("RETRY " + round.describe() + ": " + (r.accepted() ? "ACCEPTED" : "REFUSED(" + r.messageKey() + ")"));
        return;
      } catch (Exception e) {
        say("RETRY " + round.describe() + " attempt " + attempt + ": " + e);
        Thread.sleep(3000);
      }
    }
    fail("retry never completed for " + round.describe());
  }

  // ---------------------------------------------------------------- database facts

  /** potions carried, gold, then corpse/loot facts: {potions, gold, lootOnGround, receipts, corpseItems, corpseRecovered, distinctPotionIds} */
  private static int[] counts(UUID character) throws Exception {
    try (Connection c = db()) {
      int potions = 0, distinct = 0;
      long gold;
      var ids = new java.util.HashSet<String>();
      try (var q = c.prepareStatement("SELECT progress::text, gold FROM online_characters WHERE id=?")) {
        q.setObject(1, character);
        try (var rows = q.executeQuery()) {
          rows.next();
          gold = rows.getLong(2);
          for (var item : JsonParser.parseString(rows.getString(1)).getAsJsonObject().getAsJsonArray("inventory")) {
            var o = item.getAsJsonObject();
            if (o.get("key").getAsString().equals(POTION)) { potions += o.get("quantity").getAsInt(); ids.add(o.get("id").getAsString()); }
          }
        }
      }
      distinct = ids.size();
      int onGround = 0, receipts = 0, corpseItems = 0, recovered = 0;
      try (var q = c.prepareStatement("SELECT count(*) FILTER (WHERE state='ON_GROUND') FROM online_ground_loot WHERE owner_character=?")) {
        q.setObject(1, character);
        try (var rows = q.executeQuery()) { rows.next(); onGround = rows.getInt(1); }
      }
      try (var q = c.prepareStatement("SELECT count(*) FROM online_loot_claims WHERE character_id=?")) {
        q.setObject(1, character);
        try (var rows = q.executeQuery()) { rows.next(); receipts = rows.getInt(1); }
      }
      try (var q = c.prepareStatement("SELECT jsonb_array_length(items), recovered, gold FROM online_corpses WHERE character_id=?")) {
        q.setObject(1, character);
        try (var rows = q.executeQuery()) { if (rows.next()) { corpseItems = rows.getInt(1); recovered = rows.getBoolean(2) ? 1 : 0; } }
      }
      return new int[] {potions, (int) gold, onGround, receipts, corpseItems, recovered, distinct};
    }
  }

  private static void report(Round round, int[] start, String when) throws Exception {
    int[] now = counts(round.character());
    String view = "potions " + start[0] + "->" + now[0] + ", gold " + start[1] + "->" + now[1];
    view += round.kind().equals("claim") ? ", loot on ground " + now[2] + ", claim receipts " + now[3]
        : ", corpse items left " + now[4] + ", corpse recovered " + (now[5] == 1);
    boolean applied = round.kind().equals("claim") ? now[0] == start[0] + 1 && now[2] == 0 && now[3] == 1
        : now[0] == start[0] + 2 && now[1] == start[1] + (int) CORPSE_GOLD && now[4] == 0 && now[5] == 1;
    boolean untouched = round.kind().equals("claim") ? now[0] == start[0] && now[2] == 1 && now[3] == 0
        : now[0] == start[0] && now[1] == start[1] && now[4] == 2 && now[5] == 0;
    String verdict = applied ? "APPLIED once" : untouched ? "NOT applied" : "INCONSISTENT";
    say("  [" + when + "] " + round.describe() + ": " + view + " => " + verdict);
    if (!applied && !untouched) fail("half-applied state for " + round.describe());
    if (now[6] != now[0]) fail("duplicate item id inside the inventory for " + round.describe());
  }

  private static void verifyFinal(Round round, int[] start) throws Exception {
    int[] now = counts(round.character());
    boolean ok = round.kind().equals("claim") ? now[0] == start[0] + 1 && now[2] == 0 && now[3] == 1
        : now[0] == start[0] + 2 && now[1] == start[1] + (int) CORPSE_GOLD && now[4] == 0 && now[5] == 1;
    if (!ok) fail("final state wrong for " + round.describe());
    // The same item id must not also be on the ground or on the corpse: single permitted place.
    try (Connection c = db()) {
      if (round.kind().equals("claim")) {
        try (var q = c.prepareStatement("SELECT state, claimed_by FROM online_ground_loot WHERE id=?")) {
          q.setObject(1, round.target());
          try (var rows = q.executeQuery()) {
            rows.next();
            if (!"CLAIMED".equals(rows.getString(1)) || !round.character().toString().equals(rows.getString(2))) fail("loot row not CLAIMED by the claimant for " + round.describe());
          }
        }
      }
    }
  }

  // ---------------------------------------------------------------- server child

  private static void startServer() throws Exception {
    serverNumber++;
    var command = new ProcessBuilder(javaExe.toString(), "-Djava.awt.headless=true", "-jar", runtime.resolve("t4c-server.jar").toString(),
        "--online", runtime.resolve("assets/maps/worldmap/worldmap.colbin").toString(), Integer.toString(port));
    command.directory(runtime.toFile());
    command.environment().put("T4C_BIND_ADDRESS", "127.0.0.1");
    command.environment().put("T4C_CONTROL_FILE", output.resolve("control-" + serverNumber + ".token").toString());
    command.redirectOutput(output.resolve("server-" + serverNumber + ".out.log").toFile());
    command.redirectError(output.resolve("server-" + serverNumber + ".err.log").toFile());
    server = command.start();
    long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(90);
    while (System.nanoTime() < until) {
      if (!server.isAlive()) throw new IllegalStateException("Server exited early, code " + server.exitValue());
      try {
        var r = HTTP.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/health")).timeout(Duration.ofSeconds(2)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() == 200 && r.body().contains("local-persistent")) return;
      } catch (IOException notYet) { /* retry */ }
      Thread.sleep(300);
    }
    throw new IllegalStateException("Server did not become healthy");
  }

  private static void stopServer() throws Exception {
    if (!server.waitFor(10, TimeUnit.SECONDS)) throw new IllegalStateException("Server survived the kill");
  }

  private static void stopServerGracefully() throws Exception {
    String token = Files.readString(output.resolve("control-" + serverNumber + ".token")).trim();
    var r = HTTP.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/admin/shutdown")).timeout(Duration.ofSeconds(5))
        .header("Origin", "http://127.0.0.1:" + port).header("Authorization", "Bearer " + token).header("Content-Type", "text/plain")
        .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
    boolean exited = server.waitFor(100, TimeUnit.SECONDS);
    say("final graceful shutdown: HTTP " + r.statusCode() + ", exited " + exited + ", exit code " + (exited ? server.exitValue() : -1));
    if (!exited || server.exitValue() != 0) fail("final graceful shutdown did not exit 0");
  }

  // ---------------------------------------------------------------- fixtures

  private static Connection db() throws Exception {
    DriverManager.setLoginTimeout(3);
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
  }

  private static double[] standable(HostedRegionPlan plan, int world, int tx, int ty) {
    for (int r = 0; r <= 3; r++)
      for (int dy = -r; dy <= r; dy++)
        for (int dx = -r; dx <= r; dx++) {
          if (Math.max(Math.abs(dx), Math.abs(dy)) != r) continue;
          double x = tx + dx + .5, y = ty + dy + .5;
          if (plan.canStand(new CharacterProgress.Respawn(world, x, y))
              && !(plan.isRefusedPortalSource(world, x, y) || plan.portals.resolve(world, x, y, plan::canStand).isPresent())) return new double[] {x, y};
        }
    throw new IllegalArgumentException("No standable tile beside the NPC");
  }

  /** claim: one owner-window ground-loot row; recover: one corpse with two stacks and gold. Both at the character's own tile. */
  private static void seed(String kind, UUID owner, double[] at, UUID target) throws Exception {
    String hash = "0".repeat(63) + "1";
    try (Connection c = db()) {
      c.setAutoCommit(false);
      try {
        if (kind.equals("recover")) {
          try (var q = c.prepareStatement("INSERT INTO online_combat_events(event_id,character_id,payload_hash) VALUES(?,?,?)")) {
            q.setObject(1, target); q.setObject(2, owner); q.setString(3, hash); q.executeUpdate();
          }
          String items = GSON.toJson(List.of(new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1), new CharacterProgress.Item(UUID.randomUUID(), POTION, 1, 1)));
          try (var q = c.prepareStatement("INSERT INTO online_corpses(id,character_id,world,x,y,items,gold) VALUES(?,?,?,?,?,?::jsonb,?)")) {
            q.setObject(1, target); q.setObject(2, owner); q.setInt(3, 0); q.setDouble(4, at[0]); q.setDouble(5, at[1]); q.setString(6, items); q.setLong(7, CORPSE_GOLD); q.executeUpdate();
          }
        } else {
          UUID source = UUID.randomUUID();
          try (var q = c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience,payload_hash) VALUES(?,?,0,0,?)")) {
            q.setObject(1, owner); q.setObject(2, source); q.setString(3, hash); q.executeUpdate();
          }
          try (var q = c.prepareStatement("INSERT INTO online_world_rewards(event_id,character_id) VALUES(?,?)")) {
            q.setObject(1, source); q.setObject(2, owner); q.executeUpdate();
          }
          try (var q = c.prepareStatement("""
              INSERT INTO online_ground_loot(id,source_event,drop_index,world,x,y,item_key,quantity,charges,owner_character,owner_until,expires_at)
              VALUES (?,?,0,0,?,?,?,1,1,?,clock_timestamp()+interval '60 minutes',clock_timestamp()+interval '90 minutes')
              """)) {
            q.setObject(1, target); q.setObject(2, source); q.setDouble(3, at[0]); q.setDouble(4, at[1]); q.setString(5, POTION); q.setObject(6, owner); q.executeUpdate();
          }
        }
        c.commit();
      } catch (Exception failure) { c.rollback(); throw failure; }
    }
  }

  private static void say(String line) {
    System.out.println(line);
    LINES.add(line);
  }

  private static void fail(String line) {
    failed = true;
    say("FAIL " + line);
  }
}
