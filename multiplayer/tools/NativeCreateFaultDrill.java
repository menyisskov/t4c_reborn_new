package com.perso.T4C.server;

import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.CharacterStartingKit;
import com.perso.T4C.online.OnlineAccountClient;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.server.persistence.OnlineStore;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Interrupted and retried CHARACTER CREATION drill (T4C-0350, milestone M1 bullet 3). One fresh account per round; the
 * real {@link OnlineAccountClient} (the one the lobby's Create form uses) sends the creation request to a real
 * {@code ServerMain --online} child, and the child is hard-killed a fixed number of milliseconds later, so the kill races
 * the commit. Rounds cycle through all seven classes. With the database static, the committed outcome must be exactly
 * "no character" or exactly "one character with the complete server-generated kit" (never half). After the restart the
 * SAME request id is sent again: it must return one character, and if the first attempt had committed it must be the
 * same character with a byte-identical record (the kit is not granted again). A second replay must change nothing, a
 * new request id with the same name must be refused without a second character, and the same id with other fields must
 * be a creation conflict. Finally every character's kit is compared with its class's shared starting kit.
 *
 * <p>Refuses any database that is not a loopback {@code t4c_acc_<id>} cluster, protected ports and a listening port.
 * Starts and kills only its own child. Never prints an account key.
 *
 * <p>Usage: {@code NativeCreateFaultDrill RUNTIME_DIR JAVA_EXE SERVER_PORT NEW_OUTPUT_DIR} (runtime = a copied folder
 * with {@code t4c-server.jar}, {@code lib/*} and {@code assets/maps/...}); the probe classpath is the checkout's classes.
 */
public final class NativeCreateFaultDrill {
  /** Milliseconds between handing the request to the client and the hard kill; 80 to 120 ms straddles the commit on the dev machine. */
  private static final int[] DELAYS = {0, 10, 35, 60, 80, 85, 90, 95, 100, 105, 110, 115, 120, 150, 200, 350, 600, 900, 0, 20};
  /** The server admits 40 requests per peer and refills one per two seconds; every client call also fetches the map first, so a call costs two (all drill accounts share the loopback peer). */
  private static final long PACE_MS = 4500;
  private static final String[] CLASSES = OnlineCharacterCreation.CLASSES.toArray(new String[0]);

  private record Round(int number, int delay, String secret, UUID account, UUID requestId, String name, String characterClass,
      String gender, boolean evil) {
    String describe() { return "round " + number + " (" + characterClass + ", kill after " + delay + " ms)"; }
    OnlineCharacterCreation.Request request() { return new OnlineCharacterCreation.Request(requestId, name, characterClass, gender, evil); }
  }

  private record Row(UUID id, String progress, long gold, long revision) {
    String signature() throws Exception {
      var digest = MessageDigest.getInstance("SHA-256").digest((progress + "|" + gold + "|" + revision).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest).substring(0, 16);
    }
  }

  private static Process server;
  private static int serverNumber;
  private static Path runtime, output, javaExe;
  private static int port;
  private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
  private static final List<String> LINES = new ArrayList<>();
  private static boolean failed;

  public static void main(String[] args) throws Exception {
    if (args.length != 4) throw new IllegalArgumentException("Usage: NativeCreateFaultDrill RUNTIME_DIR JAVA_EXE SERVER_PORT NEW_OUTPUT_DIR");
    runtime = Path.of(args[0]).toRealPath();
    javaExe = Path.of(args[1]).toRealPath();
    port = Integer.parseInt(args[2]);
    output = Path.of(args[3]).toAbsolutePath().normalize();
    if (Set.of(18765, 18766, 18770, 18771, 55432, 55433).contains(port) || port < 1024 || port > 65534) throw new IllegalArgumentException("Protected or invalid port");
    if (Files.exists(output)) throw new IllegalArgumentException("Output must be new");
    String url = System.getenv("T4C_DB_URL");
    if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/t4c_acc_[0-9a-f]{12}(?:\\?.*)?"))
      throw new IllegalArgumentException("Refusing: only a loopback t4c_acc_* acceptance database is allowed");
    int dbPort = Integer.parseInt(url.replaceAll("^jdbc:postgresql://127\\.0\\.0\\.1:([0-9]+)/.*$", "$1"));
    if (Set.of(55432, 55433, 18765, 18766, 18770, 18771).contains(dbPort)) throw new IllegalArgumentException("Protected database port");
    for (String name : List.of("T4C_DB_USER", "T4C_DB_PASSWORD"))
      if (System.getenv(name) == null || System.getenv(name).isBlank()) throw new IllegalArgumentException("Private database environment incomplete");
    for (int p : new int[] {port, port + 1})
      try (var probe = new java.net.ServerSocket(p, 1, java.net.InetAddress.getByName("127.0.0.1"))) { /* free */ }
    Files.createDirectories(output);

    var rounds = new ArrayList<Round>();
    try (var store = OnlineStore.fromEnvironment()) {
      store.migrate();
      for (int i = 0; i < DELAYS.length; i++) {
        var key = store.provisionAccount();
        String cls = CLASSES[i % CLASSES.length];
        rounds.add(new Round(i + 1, DELAYS[i], key.secret(), key.accountId(), UUID.randomUUID(),
            "Drill" + (char) ('a' + i), cls,
            i % 2 == 0 ? "male" : "female", i % 3 == 0));
      }
    }
    say("provisioned " + rounds.size() + " empty accounts; classes " + String.join(",", CLASSES) + " cycled");
    var firstRow = new TreeMap<Integer, Row>();
    try {
      startServer();
      for (var round : rounds) {
        String outcome = send(round);
        stopServer();
        var rows = rows(round.account());
        String state = rows.size() == 0 ? "NO character" : rows.size() == 1 ? "ONE character, kit complete=" + kitMatches(round, rows.get(0)) : "DUPLICATES " + rows.size();
        say("ROUND " + round.describe() + ": reply " + outcome + "; database right after the kill: " + state + ", creation receipts " + receipts(round.account()));
        if (rows.size() > 1) fail("duplicate characters after the kill: " + round.describe());
        if (rows.size() == 1) {
          firstRow.put(round.number(), rows.get(0));
          if (!kitMatches(round, rows.get(0))) fail("partial or wrong kit after the kill: " + round.describe());
          if (receipts(round.account()) != 1) fail("character without exactly one creation receipt: " + round.describe());
        } else if (receipts(round.account()) != 0) fail("receipt without character: " + round.describe());
        startServer();
      }
      say("retrying every request with the SAME request id after the restart");
      for (var round : rounds) {
        var result = retry(round, "retry");
        var rows = rows(round.account());
        if (rows.size() != 1) { fail("not exactly one character after the retry: " + round.describe() + " has " + rows.size()); continue; }
        var row = rows.get(0);
        if (result == null || !row.id().equals(result.character().id())) fail("retry answered another character: " + round.describe());
        var before = firstRow.get(round.number());
        boolean sameAsBefore = before == null || before.signature().equals(row.signature());
        say("  RETRY " + round.describe() + ": " + (before == null ? "created by the retry" : "character existed already, record identical=" + sameAsBefore)
            + "; stacks " + stacks(row) + ", gold " + row.gold() + ", kit matches class kit=" + kitMatches(round, row) + ", record " + row.signature());
        if (!sameAsBefore) fail("the retry changed the committed record (kit regranted?): " + round.describe());
        if (!kitMatches(round, row)) fail("kit differs from the class kit: " + round.describe());
        if (receipts(round.account()) != 1) fail("receipt count is not 1: " + round.describe());
        // A second replay changes nothing.
        retry(round, "second replay");
        var again = rows(round.account());
        if (again.size() != 1 || !again.get(0).signature().equals(row.signature())) fail("a replay changed the record: " + round.describe());
        // Same id with other fields: creation_conflict; new id with the same name: refused; neither adds a character.
        var other = new OnlineCharacterCreation.Request(round.requestId(), round.name(), round.characterClass(), round.gender(), !round.evil());
        String conflict = code(round, other);
        var renamed = new OnlineCharacterCreation.Request(UUID.randomUUID(), round.name(), round.characterClass(), round.gender(), round.evil());
        String taken = code(round, renamed);
        var after = rows(round.account());
        say("    changed fields with the same id => " + conflict + "; new id with the same name => " + taken + "; characters now " + after.size()
            + ", receipts " + receipts(round.account()));
        if (!"creation_conflict".equals(conflict)) fail("same id, other fields was not a creation_conflict: " + round.describe());
        if (!"name_taken".equals(taken)) fail("same name under a new id was not refused as name_taken: " + round.describe());
        if (after.size() != 1 || !after.get(0).signature().equals(row.signature()) || receipts(round.account()) != 1) fail("a refusal changed the record: " + round.describe());
      }
      int applied = firstRow.size();
      say("summary: " + applied + " of " + rounds.size() + " requests had already committed when the server was killed, "
          + (rounds.size() - applied) + " had not; every account ended with exactly one character and one receipt");
      if (applied == 0 || applied == rounds.size()) say("NOTE: the kill delays produced only one of the two outcomes; widen the delays before relying on this run");
      stopServerGracefully();
    } finally {
      if (server != null && server.isAlive()) server.destroyForcibly();
      Files.write(output.resolve("report.txt"), LINES, StandardCharsets.UTF_8);
    }
    say(failed ? "RESULT: FAIL" : "RESULT: PASS");
    Files.write(output.resolve("report.txt"), LINES, StandardCharsets.UTF_8);
    if (failed) System.exit(1);
  }

  // ---------------------------------------------------------------- requests

  /** Hands the request to the real client, kills the server after the delay and reports how the reply ended. */
  private static String send(Round round) throws Exception {
    var client = new OnlineAccountClient(URI.create("http://127.0.0.1:" + port), round.secret());
    CompletableFuture<OnlineCharacterCreation.Result> pending = CompletableFuture.supplyAsync(() -> {
      try { return client.create(round.request()); }
      catch (Exception e) { throw new RuntimeException(e); }
    });
    if (round.delay() > 0) Thread.sleep(round.delay());
    server.destroyForcibly();
    try {
      var r = pending.get(10, TimeUnit.SECONDS);
      return "ACKNOWLEDGED (" + r.character().name() + ")";
    } catch (Exception lost) {
      return "LOST";
    } finally {
      client.close();
    }
  }

  private static OnlineCharacterCreation.Result retry(Round round, String label) throws Exception {
    for (int attempt = 0; attempt < 5; attempt++) {
      Thread.sleep(PACE_MS);
      try (var client = new OnlineAccountClient(URI.create("http://127.0.0.1:" + port), round.secret())) {
        return client.create(round.request());
      } catch (Exception e) {
        say("  " + label + " " + round.describe() + " attempt " + attempt + ": " + e.getClass().getSimpleName()
            + (e instanceof OnlineAccountClient.Rejected r ? " " + r.statusCode() + " " + r.code() : ""));
        Thread.sleep(PACE_MS);
      }
    }
    fail(label + " never completed for " + round.describe());
    return null;
  }

  private static String code(Round round, OnlineCharacterCreation.Request request) throws Exception {
    Thread.sleep(PACE_MS);
    try (var client = new OnlineAccountClient(URI.create("http://127.0.0.1:" + port), round.secret())) {
      client.create(request);
      return "ACCEPTED";
    } catch (OnlineAccountClient.Rejected rejected) {
      return rejected.code();
    }
  }

  // ---------------------------------------------------------------- database facts

  private static List<Row> rows(UUID account) throws Exception {
    var list = new ArrayList<Row>();
    try (Connection c = db(); var q = c.prepareStatement("SELECT id, progress::text, gold, revision FROM online_characters WHERE account_id=? ORDER BY name")) {
      q.setObject(1, account);
      try (var rows = q.executeQuery()) {
        while (rows.next()) list.add(new Row(rows.getObject(1, UUID.class), rows.getString(2), rows.getLong(3), rows.getLong(4)));
      }
    }
    return list;
  }

  private static int receipts(UUID account) throws Exception {
    try (Connection c = db(); var q = c.prepareStatement("SELECT count(*) FROM online_character_creations WHERE account_id=?")) {
      q.setObject(1, account);
      try (var rows = q.executeQuery()) { rows.next(); return rows.getInt(1); }
    }
  }

  private static int stacks(Row row) {
    return JsonParser.parseString(row.progress()).getAsJsonObject().getAsJsonArray("inventory").size();
  }

  /** The record equals the class's shared starting kit: items, worn slots, spells, skills, level, karma, gold range, distinct item ids. */
  private static boolean kitMatches(Round round, Row row) {
    var type = switch (round.characterClass()) {
      case "warrior" -> CharacterClass.WARRIOR;
      case "archer" -> CharacterClass.ARCHER;
      case "paladin" -> CharacterClass.PALADIN;
      case "cleric" -> CharacterClass.CLERIC;
      case "healer" -> CharacterClass.HEALER;
      case "mage" -> CharacterClass.MAGE;
      case "battlemage" -> CharacterClass.BATTLE_MAGE;
      default -> throw new IllegalArgumentException(round.characterClass());
    };
    var progress = JsonParser.parseString(row.progress()).getAsJsonObject();
    var expected = new TreeMap<String, Integer>();
    type.startingEquipment().values().forEach(key -> expected.merge(key, 1, Integer::sum));
    for (var key : type.startingStacks()) expected.merge(key, CharacterClass.STACKED_KIT_ITEM_COUNT, Integer::sum);
    expected.merge(CharacterStartingKit.TORCH, CharacterStartingKit.TORCH_COUNT, Integer::sum);
    for (var key : CharacterStartingKit.POTIONS) expected.merge(key, CharacterStartingKit.POTION_COUNT, Integer::sum);
    var actual = new TreeMap<String, Integer>();
    var ids = new TreeSet<String>();
    var inventoryIds = new TreeSet<String>();
    for (var item : progress.getAsJsonArray("inventory")) {
      var o = item.getAsJsonObject();
      actual.merge(o.get("key").getAsString(), o.get("quantity").getAsInt(), Integer::sum);
      ids.add(o.get("id").getAsString());
      inventoryIds.add(o.get("id").getAsString());
    }
    var worn = new TreeSet<String>();
    var equipment = progress.getAsJsonObject("equipment");
    for (var slot : equipment.keySet()) {
      worn.add(slot);
      if (!inventoryIds.contains(equipment.get(slot).getAsString())) return false;
    }
    var wantedSlots = new TreeSet<String>();
    type.startingEquipment().keySet().forEach(slot -> wantedSlots.add(slot.name()));
    var spells = new TreeSet<String>();
    progress.getAsJsonArray("spells").forEach(s -> spells.add(s.getAsString()));
    var wantedSpells = new TreeSet<>(type.startingSpellKeys());
    var skills = new TreeMap<String, Integer>();
    progress.getAsJsonObject("skills").entrySet().forEach(e -> skills.put(e.getKey(), e.getValue().getAsInt()));
    long karma = progress.getAsJsonObject("advancement").get("karma").getAsLong();
    long level = progress.getAsJsonObject("advancement").get("level").getAsLong();
    return actual.equals(expected) && ids.size() == progress.getAsJsonArray("inventory").size() && worn.equals(wantedSlots)
        && spells.equals(wantedSpells) && skills.equals(new TreeMap<>(CharacterStartingKit.SKILLS)) && level == 1
        && karma == (round.evil() ? CharacterStartingKit.EVIL_KARMA : CharacterStartingKit.GOOD_KARMA)
        && row.gold() >= CharacterStartingKit.MIN_GOLD && row.gold() <= CharacterStartingKit.MAX_GOLD
        && round.gender().equals(progress.get("gender").getAsString())
        && round.characterClass().equals(progress.get("characterClass").getAsString());
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

  private static Connection db() throws Exception {
    DriverManager.setLoginTimeout(3);
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
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
