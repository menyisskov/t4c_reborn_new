package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Helper for the twenty-adapter fault drills (T4C-0310): provisions twenty characters beside the opening NPC, lets
 * each obtain the opening letter, watches the committed database while NativeAdapterSoak plays, and afterwards checks
 * the committed record against everything the monitor saw. It never writes gameplay state itself (provisioning and the
 * greeting go through the same store and adapter paths a player uses), never prints a credential and refuses any
 * database whose name does not start with t4c_acc_ (the disposable acceptance cluster).
 *
 * Commands: provision PROFILES_DIR ENDPOINT | greet PROFILES_DIR ENDPOINT | monitor PROFILES_DIR STATE_FILE STOP_FILE
 * | verify PROFILES_DIR STATE_FILE REPORT_FILE [expect-letters]
 */
public final class NativeFaultDrill {
  private static final Pattern UUID_TEXT = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
  private static final Gson GSON = new GsonBuilder().create();

  /** What the monitor remembers; written as JSON so verify can run in a fresh JVM after the fault. */
  static final class State {
    Map<String, Map<String, Object>> lastSeen = new TreeMap<>();   // characterId -> revision, experience, gold, hash
    Map<String, Set<String>> seenIds = new TreeMap<>();            // table -> ids once observed committed
    Map<String, String> lootState = new TreeMap<>();               // loot id -> last observed state
    List<String> timeline = new ArrayList<>();                     // "elapsedMs,dbOk,activeSessions"
    int samples, dbFailures;
    long firstDbFailureMs = -1, lastDbFailureMs = -1, startedMs;
    long lastGoodMs = -1;
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) throw new IllegalArgumentException("Usage: see class comment");
    guardDatabase();
    switch (args[0]) {
      case "provision" -> provision(Path.of(args[1]));
      case "greet" -> greet(Path.of(args[1]), args[2]);
      case "greet-kill" -> greetKill(Path.of(args[1]), args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
      case "letters" -> letters(Path.of(args[1]));
      case "monitor" -> monitor(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
      case "verify" -> System.exit(verify(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]),
          args.length > 4 && "expect-letters".equals(args[4])) ? 0 : 1);
      default -> throw new IllegalArgumentException("Unknown command");
    }
  }

  private static void guardDatabase() {
    String url = System.getenv("T4C_DB_URL");
    if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/t4c_acc_[0-9a-f]{12}(?:\\?.*)?"))
      throw new IllegalArgumentException("Refusing: only a loopback t4c_acc_* acceptance database is allowed");
    for (String name : List.of("T4C_DB_USER", "T4C_DB_PASSWORD"))
      if (System.getenv(name) == null || System.getenv(name).isBlank()) throw new IllegalArgumentException("Private database environment is incomplete");
    int port = Integer.parseInt(url.replaceAll("^jdbc:postgresql://127\\.0\\.0\\.1:([0-9]+)/.*$", "$1"));
    if (List.of(55432, 55433, 18765, 18766, 18770, 18771).contains(port)) throw new IllegalArgumentException("Protected port");
  }

  private static Connection db() throws Exception {
    DriverManager.setLoginTimeout(3);
    var c = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
    return c;
  }

  // ---------------------------------------------------------------- provisioning and greeting

  private static void provision(Path dir) throws Exception {
    Files.createDirectories(dir);
    var npc = NpcInteractionCatalog.loadBundled().require(CrimsonscaleLetterRules.NPC);
    try (var store = OnlineStore.fromEnvironment()) {
      for (int i = 1; i <= 20; i++) {
        var kit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(309L * 100 + i));
        var key = store.provisionAccount();
        var created = store.create(key.accountId(), String.format("Drill %s-%02d", UUID.randomUUID().toString().substring(0, 6), i),
            npc.world(), npc.x() + .5, npc.y() + .5, kit.progress(), kit.gold());
        Files.writeString(dir.resolve(String.format("player-%02d.json", i)),
            GSON.toJson(Map.of("accountId", key.accountId(), "characterId", created.id(), "secret", key.secret())),
            StandardCharsets.UTF_8);
      }
    }
    System.out.println("PROVISIONED 20 characters at the opening NPC");
  }

  private static List<OnlineConnection.Credentials> credentials(Path dir) throws Exception {
    var list = new ArrayList<OnlineConnection.Credentials>();
    try (var files = Files.list(dir)) {
      for (var file : files.filter(p -> p.getFileName().toString().matches("player-[0-9]{2}\\.json")).sorted().toList()) {
        var profile = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        list.add(new OnlineConnection.Credentials(profile.get("secret").getAsString(), UUID.fromString(profile.get("characterId").getAsString())));
      }
    }
    if (list.size() != 20) throw new IllegalArgumentException("Twenty profiles required");
    return list;
  }

  private static void greet(Path dir, String endpoint) throws Exception {
    int granted = 0;
    for (var credentials : credentials(dir)) {
      try (var connection = new OnlineConnection(java.net.URI.create(endpoint), credentials)) {
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (connection.view() == null && !connection.status().equals("disconnected") && System.nanoTime() < until) Thread.sleep(25);
        if (connection.view() == null) throw new AssertionError("Adapter did not join for the greeting");
        connection.confirmRegionReady(connection.view().map().locationEpoch());
        boolean ok = false;
        for (int attempt = 0; attempt < 20 && !ok; attempt++) {
          ok = connection.interact(CrimsonscaleLetterRules.NPC).toCompletableFuture().get(5, TimeUnit.SECONDS).accepted();
          if (!ok) Thread.sleep(200);
        }
        if (!ok) throw new AssertionError("Opening NPC never accepted the greeting");
        granted++;
      }
      Thread.sleep(600); // paced like the soak: the public admission is two wide
    }
    System.out.println("GREETED " + granted + " (leases are released on a normal logout)");
  }

  /**
   * Lost-acknowledgement round: join one character, send the opening greeting and hard-kill the owned server process a
   * fixed number of milliseconds after the request is handed to the adapter, so the kill races the reward commit. The
   * server is only ever our own child: the target must be a t4c-server.jar --online process on this endpoint's port.
   */
  private static void greetKill(Path dir, String endpoint, long pid, int index, int delayMillis) throws Exception {
    var credentials = credentials(dir).get(index);
    var target = ProcessHandle.of(pid).orElseThrow(() -> new IllegalArgumentException("No such process"));
    // Windows does not always expose another process's command line, so ownership is the parent: the orchestrator that
    // started this tool started the server, so both must have the same parent process.
    var ourParent = ProcessHandle.current().parent();
    if (ourParent.isEmpty() || target.parent().isEmpty() || ourParent.get().pid() != target.parent().get().pid()
        || target.pid() == ProcessHandle.current().pid() || !target.info().command().orElse("").toLowerCase().endsWith("java.exe"))
      throw new IllegalArgumentException("Refusing to kill a process that was not started by our own orchestrator as a java server");
    try (var connection = new OnlineConnection(java.net.URI.create(endpoint), credentials)) {
      long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
      while (connection.view() == null && !connection.status().equals("disconnected") && System.nanoTime() < until) Thread.sleep(25);
      if (connection.view() == null) throw new AssertionError("Adapter did not join before the kill");
      connection.confirmRegionReady(connection.view().map().locationEpoch());
      var pending = connection.interact(CrimsonscaleLetterRules.NPC).toCompletableFuture();
      if (delayMillis > 0) Thread.sleep(delayMillis);
      target.destroyForcibly();
      String outcome;
      try { outcome = pending.get(8, TimeUnit.SECONDS).accepted() ? "ACKNOWLEDGED" : "REFUSED"; }
      catch (Exception lost) { outcome = "LOST"; }
      System.out.println("GREET_KILL index=" + index + " delayMs=" + delayMillis + " reply=" + outcome);
    }
  }

  private static void letters(Path dir) throws Exception {
    var characters = ids(dir);
    var counts = new StringBuilder();
    try (var c = db(); var s = c.prepareStatement("SELECT progress::text FROM online_characters WHERE id=?")) {
      for (var id : characters) {
        s.setObject(1, id);
        try (var rows = s.executeQuery()) {
          rows.next();
          int n = 0;
          for (var item : JsonParser.parseString(rows.getString(1)).getAsJsonObject().getAsJsonArray("inventory")) {
            var o = item.getAsJsonObject();
            if (o.get("key").getAsString().equals(CrimsonscaleLetterRules.ITEM)) n += o.get("quantity").getAsInt();
          }
          counts.append(n);
        }
      }
    }
    System.out.println("LETTERS " + counts);
  }

  // ---------------------------------------------------------------- monitor

  private static List<UUID> ids(Path dir) throws Exception {
    return credentials(dir).stream().map(OnlineConnection.Credentials::characterId).toList();
  }

  private static void monitor(Path dir, Path stateFile, Path stopFile) throws Exception {
    var characters = ids(dir);
    var state = new State();
    state.startedMs = System.currentTimeMillis();
    for (var table : List.of("rewards", "worldRewards", "combatEvents", "transferEvents", "lootClaims", "groundLoot", "corpses"))
      state.seenIds.put(table, new TreeSet<>());
    while (!Files.exists(stopFile)) {
      long now = System.currentTimeMillis();
      state.samples++;
      try (var c = db()) {
        var array = c.createArrayOf("uuid", characters.toArray());
        int active = 0;
        try (var s = c.prepareStatement("SELECT id, revision, experience, gold, md5(progress::text) h, location_epoch, "
            + "(session_id IS NOT NULL AND session_until>clock_timestamp()) active FROM online_characters WHERE id=ANY(?)")) {
          s.setQueryTimeout(4); s.setArray(1, array);
          try (var rows = s.executeQuery()) {
            while (rows.next()) {
              var entry = new LinkedHashMap<String, Object>();
              entry.put("revision", rows.getLong("revision")); entry.put("experience", rows.getLong("experience"));
              entry.put("gold", rows.getLong("gold")); entry.put("hash", rows.getString("h"));
              entry.put("locationEpoch", rows.getLong("location_epoch"));
              state.lastSeen.put(rows.getString("id"), entry);
              if (rows.getBoolean("active")) active++;
            }
          }
        }
        collect(c, state, "rewards", "SELECT event_id::text FROM online_rewards WHERE character_id=ANY(?)", array);
        collect(c, state, "worldRewards", "SELECT event_id::text FROM online_world_rewards WHERE character_id=ANY(?)", array);
        collect(c, state, "combatEvents", "SELECT event_id::text FROM online_combat_events WHERE character_id=ANY(?)", array);
        collect(c, state, "transferEvents", "SELECT event_id::text FROM online_transfer_events WHERE character_id=ANY(?)", array);
        collect(c, state, "lootClaims", "SELECT event_id::text FROM online_loot_claims WHERE character_id=ANY(?)", array);
        collect(c, state, "corpses", "SELECT id::text FROM online_corpses WHERE character_id=ANY(?)", array);
        try (var s = c.prepareStatement("SELECT id::text, state FROM online_ground_loot WHERE owner_character=ANY(?)")) {
          s.setQueryTimeout(4); s.setArray(1, array);
          try (var rows = s.executeQuery()) {
            while (rows.next()) { state.seenIds.get("groundLoot").add(rows.getString(1)); state.lootState.merge(rows.getString(1), rows.getString(2), NativeFaultDrill::laterLootState); }
          }
        }
        state.lastGoodMs = now;
        state.timeline.add((now - state.startedMs) + ",1," + active);
      } catch (Exception failure) {
        state.dbFailures++;
        if (state.firstDbFailureMs < 0) state.firstDbFailureMs = now;
        state.lastDbFailureMs = now;
        state.timeline.add((now - state.startedMs) + ",0,-1");
      }
      write(stateFile, state);
      Thread.sleep(1000);
    }
    write(stateFile, state);
    System.out.println("MONITOR_STOPPED samples=" + state.samples + " dbFailures=" + state.dbFailures);
  }

  private static String laterLootState(String old, String now) { return old.equals("ON_GROUND") ? now : old; }

  private static void collect(Connection c, State state, String table, String sql, java.sql.Array array) throws Exception {
    try (var s = c.prepareStatement(sql)) {
      s.setQueryTimeout(4); s.setArray(1, array);
      try (var rows = s.executeQuery()) { while (rows.next()) state.seenIds.get(table).add(rows.getString(1)); }
    }
  }

  private static void write(Path file, State state) throws Exception {
    Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
    Files.writeString(tmp, GSON.toJson(state), StandardCharsets.UTF_8);
    Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
  }

  // ---------------------------------------------------------------- verification

  private static boolean verify(Path dir, Path stateFile, Path reportFile, boolean expectLetters) throws Exception {
    var characters = ids(dir);
    State seen = Files.exists(stateFile) ? GSON.fromJson(Files.readString(stateFile), State.class) : new State();
    var out = new ArrayList<String>();
    boolean[] ok = {true};
    java.util.function.BiConsumer<String, String> pass = (n, d) -> out.add("PASS  " + n + (d.isEmpty() ? "" : " - " + d));
    java.util.function.BiConsumer<String, String> fail = (n, d) -> { ok[0] = false; out.add("FAIL  " + n + " - " + d); };
    try (var c = db()) {
      var array = c.createArrayOf("uuid", characters.toArray());
      // 1. No acknowledged (observed-committed) revision is lost, and a revision seen earlier still holds its state.
      int regressed = 0, identical = 0, advanced = 0, mismatched = 0;
      var holders = new TreeMap<String, Set<String>>();
      var letterCount = new TreeMap<String, Integer>();
      try (var s = c.prepareStatement("SELECT id::text, revision, experience, gold, md5(progress::text) h, progress::text p FROM online_characters WHERE id=ANY(?)")) {
        s.setArray(1, array);
        try (var rows = s.executeQuery()) {
          int count = 0;
          while (rows.next()) {
            count++;
            String id = rows.getString("id"); long rev = rows.getLong("revision");
            var before = seen.lastSeen.get(id);
            if (before != null) {
              long seenRev = ((Number) before.get("revision")).longValue();
              if (rev < seenRev) { regressed++; out.add("      character " + id.substring(0, 8) + " revision " + rev + " < seen " + seenRev); }
              else if (rev == seenRev) {
                boolean same = ((Number) before.get("experience")).longValue() == rows.getLong("experience")
                    && ((Number) before.get("gold")).longValue() == rows.getLong("gold") && before.get("hash").equals(rows.getString("h"));
                if (same) identical++; else { mismatched++; out.add("      character " + id.substring(0, 8) + " changed state at an unchanged revision"); }
              } else advanced++;
            }
            var progress = JsonParser.parseString(rows.getString("p")).getAsJsonObject();
            int letters = 0;
            for (var item : progress.getAsJsonArray("inventory")) {
              var o = item.getAsJsonObject();
              if (o.get("key").getAsString().equals(CrimsonscaleLetterRules.ITEM)) letters += o.get("quantity").getAsInt();
            }
            letterCount.put(id, letters);
            // Item ids: the same id held by two characters (or by a character and an unrecovered corpse) is duplicated value.
            var m = UUID_TEXT.matcher(rows.getString("p"));
            while (m.find()) holders.computeIfAbsent(m.group(), k -> new LinkedHashSet<>()).add("character:" + id);
          }
          if (count != 20) fail.accept("twenty characters present", "found " + count); else pass.accept("twenty characters present", "");
        }
      }
      if (regressed == 0) pass.accept("no committed revision lost", "revision >= last observed for all; identical at same revision " + identical + ", advanced " + advanced);
      else fail.accept("no committed revision lost", regressed + " character(s) went backwards");
      if (mismatched == 0) pass.accept("state at an observed revision is unchanged", ""); else fail.accept("state at an observed revision is unchanged", mismatched + " differ");
      // 2. Receipts and events once seen committed are still there.
      var tables = Map.of("rewards", "SELECT event_id::text FROM online_rewards WHERE character_id=ANY(?)",
          "worldRewards", "SELECT event_id::text FROM online_world_rewards WHERE character_id=ANY(?)",
          "combatEvents", "SELECT event_id::text FROM online_combat_events WHERE character_id=ANY(?)",
          "transferEvents", "SELECT event_id::text FROM online_transfer_events WHERE character_id=ANY(?)",
          "lootClaims", "SELECT event_id::text FROM online_loot_claims WHERE character_id=ANY(?)",
          "corpses", "SELECT id::text FROM online_corpses WHERE character_id=ANY(?)",
          "groundLoot", "SELECT id::text FROM online_ground_loot WHERE owner_character=ANY(?)");
      for (var table : new TreeSet<>(tables.keySet())) {
        var now = new TreeSet<String>();
        try (var s = c.prepareStatement(tables.get(table))) { s.setArray(1, array); try (var rows = s.executeQuery()) { while (rows.next()) now.add(rows.getString(1)); } }
        var lost = new TreeSet<>(seen.seenIds.getOrDefault(table, Set.of())); lost.removeAll(now);
        var fresh = new TreeSet<>(now); fresh.removeAll(seen.seenIds.getOrDefault(table, Set.of()));
        if (lost.isEmpty()) pass.accept(table + " rows observed before are all still committed", "seen " + seen.seenIds.getOrDefault(table, Set.of()).size() + ", now " + now.size() + ", new since last sample " + fresh.size());
        else fail.accept(table + " rows lost", lost.size() + " observed id(s) missing");
      }
      // 3. Ground-loot lifecycle never goes backwards and every claim has exactly one matching receipt.
      int regressedLoot = 0;
      try (var s = c.prepareStatement("SELECT id::text, state FROM online_ground_loot WHERE owner_character=ANY(?)")) {
        s.setArray(1, array);
        try (var rows = s.executeQuery()) {
          while (rows.next()) { String was = seen.lootState.get(rows.getString(1)); if (was != null && !was.equals("ON_GROUND") && !was.equals(rows.getString(2))) regressedLoot++; }
        }
      }
      if (regressedLoot == 0) pass.accept("no ground-loot state went backwards", ""); else fail.accept("ground loot state", regressedLoot + " rows left a terminal state");
      long badClaims = scalar(c, "SELECT count(*) FROM online_ground_loot g LEFT JOIN online_loot_claims k ON k.loot_id=g.id WHERE g.owner_character=ANY(?) AND "
          + "((g.state='CLAIMED' AND (k.event_id IS NULL OR k.event_id<>g.claim_event)) OR (g.state<>'CLAIMED' AND k.event_id IS NOT NULL))", array);
      if (badClaims == 0) pass.accept("every claimed drop has exactly one matching claim receipt", ""); else fail.accept("claim receipts", badClaims + " drops disagree with their receipts");
      // 4. No invented experience: a character cannot hold more XP than its reward receipts paid (deaths only lower it).
      long overXp = scalar(c, "SELECT count(*) FROM online_characters ch WHERE ch.id=ANY(?) AND ch.experience > COALESCE((SELECT sum(r.experience) FROM online_rewards r WHERE r.character_id=ch.id),0)", array);
      if (overXp == 0) pass.accept("no character holds more experience than its receipts paid", ""); else fail.accept("experience", overXp + " characters exceed their receipts");
      // 5. Unrecovered corpses join the item-id ownership check.
      try (var s = c.prepareStatement("SELECT id::text, character_id::text, items::text FROM online_corpses WHERE character_id=ANY(?) AND NOT recovered")) {
        s.setArray(1, array);
        try (var rows = s.executeQuery()) {
          while (rows.next()) {
            var m = UUID_TEXT.matcher(rows.getString(3)); while (m.find()) holders.computeIfAbsent(m.group(), k -> new LinkedHashSet<>()).add("corpse:" + rows.getString(1));
            for (var item : JsonParser.parseString(rows.getString(3)).getAsJsonArray()) {
              var o = item.getAsJsonObject();
              if (o.has("key") && o.get("key").getAsString().equals(CrimsonscaleLetterRules.ITEM))
                letterCount.merge(rows.getString(2), o.has("quantity") ? o.get("quantity").getAsInt() : 1, Integer::sum);
            }
          }
        }
      }
      long moreThanOne = letterCount.values().stream().filter(n -> n > 1).count(), none = letterCount.values().stream().filter(n -> n == 0).count();
      if (moreThanOne == 0) pass.accept("no character holds more than one opening letter (inventory plus unrecovered corpse)", "");
      else fail.accept("opening letters", moreThanOne + " characters hold more than one");
      if (expectLetters) { if (none == 0) pass.accept("every character still has its one opening letter", ""); else fail.accept("opening letters", none + " characters lost the letter"); }
      long shared = holders.values().stream().filter(h -> h.size() > 1).count();
      if (shared == 0) pass.accept("no item id is held by two characters or a character and an unrecovered corpse", holders.size() + " distinct ids");
      else fail.accept("item ownership", shared + " ids held in more than one place");
      // 6. Leases: none can be live unless an adapter is connected; report the count for the record.
      out.add("INFO  live leases at verification: " + scalar(c, "SELECT count(*) FROM online_characters WHERE id=ANY(?) AND session_id IS NOT NULL AND session_until>clock_timestamp()", array));
    }
    out.add("INFO  monitor samples " + seen.samples + ", database sample failures " + seen.dbFailures
        + (seen.firstDbFailureMs > 0 ? ", failure window " + (seen.firstDbFailureMs - seen.startedMs) + " to " + (seen.lastDbFailureMs - seen.startedMs) + " ms" : ""));
    out.add(ok[0] ? "RESULT PASS" : "RESULT FAIL");
    Files.write(reportFile, out, StandardCharsets.UTF_8);
    out.forEach(System.out::println);
    return ok[0];
  }

  private static long scalar(Connection c, String sql, java.sql.Array array) throws Exception {
    try (var s = c.prepareStatement(sql)) { s.setArray(1, array); try (var rows = s.executeQuery()) { rows.next(); return rows.getLong(1); } }
  }
}
