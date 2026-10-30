package com.perso.T4C.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Database-side helper for the restored-gameplay check (T4C-0344, milestone M5). It reads, and never writes, a loopback
 * disposable database whose name is either {@code t4c_acc_<12 hex>} (the source) or {@code t4c_restore_<12 hex>} (the
 * restore made by verify_backup.py), through the private T4C_DB_URL, T4C_DB_USER and T4C_DB_PASSWORD environment that
 * acceptance-cluster.ps1 RunEnv sets for one child command. It never prints a credential.
 *
 * <p>Commands: {@code summary OUT_JSON} writes and prints a gameplay-level summary of the whole database (characters,
 * owned items, quest flags, gold, experience, receipts, combat events, loot claims and drops, corpses and one
 * fingerprint over every character's committed state, none of which includes a lease column);
 * {@code compare A_JSON B_JSON} exits 1 unless the two summaries are identical;
 * {@code greet PROFILES_DIR ENDPOINT} and {@code monitor PROFILES_DIR STATE_FILE STOP_FILE} run NativeFaultDrill's
 * adapter greeting and database sampler against a restored database (that tool's own guard admits only the source name);
 * {@code verify PROFILES_DIR STATE_FILE REPORT_FILE [expect-letters]} runs {@link NativeFaultDrill}'s committed-state
 * verifier (no revision lowered, every id the monitor once saw committed is still there, no duplicated item id, one
 * opening letter per character) against the restored database. The restored database is never dropped or modified.
 */
public final class NativeRestoreCheck {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String NAME = "(t4c_acc_[0-9a-f]{12}|t4c_restore_[0-9a-f]{12})";

  public static void main(String[] args) throws Exception {
    guardDatabase();
    switch (args.length == 0 ? "" : args[0]) {
      case "summary" -> summary(Path.of(args[1]));
      case "compare" -> System.exit(compare(Path.of(args[1]), Path.of(args[2])) ? 0 : 1);
      case "greet" -> NativeFaultDrill.greet(Path.of(args[1]), args[2]);
      case "monitor" -> NativeFaultDrill.monitor(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
      case "verify" -> System.exit(NativeFaultDrill.verify(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]),
          args.length > 4 && "expect-letters".equals(args[4])) ? 0 : 1);
      default -> throw new IllegalArgumentException("Usage: summary OUT | compare A B | verify PROFILES STATE REPORT [expect-letters]");
    }
  }

  private static void guardDatabase() {
    String url = System.getenv("T4C_DB_URL");
    if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/" + NAME + "(?:\\?.*)?"))
      throw new IllegalArgumentException("Refusing: only a loopback t4c_acc_* or t4c_restore_* database is allowed");
    for (String name : List.of("T4C_DB_USER", "T4C_DB_PASSWORD"))
      if (System.getenv(name) == null || System.getenv(name).isBlank()) throw new IllegalArgumentException("Private database environment is incomplete");
    int port = Integer.parseInt(url.replaceAll("^jdbc:postgresql://127\\.0\\.0\\.1:([0-9]+)/.*$", "$1"));
    if (List.of(55432, 55433, 18765, 18766, 18770, 18771).contains(port)) throw new IllegalArgumentException("Protected port");
  }

  private static Connection db() throws Exception {
    DriverManager.setLoginTimeout(3);
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
  }

  private static long scalar(Connection c, String sql) throws Exception {
    try (var s = c.createStatement(); var r = s.executeQuery(sql)) { r.next(); return r.getLong(1); }
  }

  private static void summary(Path out) throws Exception {
    var m = new TreeMap<String, Object>();
    try (var c = db()) {
      for (String t : List.of("accounts", "characters", "rewards", "world_rewards", "combat_events", "transfer_events",
          "loot_claims", "ground_loot", "corpses", "spawns", "character_creations"))
        m.put("rows." + t, scalar(c, "SELECT count(*) FROM online_" + t));
      m.put("info.liveLeasesNow", scalar(c, "SELECT count(*) FROM online_characters WHERE session_id IS NOT NULL AND session_until>clock_timestamp()"));
      m.put("corpses.recovered", scalar(c, "SELECT count(*) FROM online_corpses WHERE recovered"));
      m.put("corpses.unrecovered", scalar(c, "SELECT count(*) FROM online_corpses WHERE NOT recovered"));
      try (var s = c.createStatement(); var r = s.executeQuery("SELECT state, count(*) FROM online_ground_loot GROUP BY state ORDER BY state")) {
        while (r.next()) m.put("ground_loot.state." + r.getString(1), r.getLong(2));
      }
      long items = 0, stored = 0, flagged = 0, flags = 0, corpseItems = 0;
      var fingerprint = java.security.MessageDigest.getInstance("SHA-256");
      long gold = 0, xp = 0, revisions = 0;
      try (var s = c.createStatement();
           var r = s.executeQuery("SELECT id::text, revision, experience, gold, progress::text p FROM online_characters ORDER BY id::text")) {
        while (r.next()) {
          var p = JsonParser.parseString(r.getString("p")).getAsJsonObject();
          for (var i : p.getAsJsonArray("inventory")) items += i.getAsJsonObject().get("quantity").getAsLong();
          if (p.has("storage")) for (var i : p.getAsJsonArray("storage")) stored += i.getAsJsonObject().get("quantity").getAsLong();
          int f = p.has("questFlags") ? p.getAsJsonObject("questFlags").size() : 0;
          if (f > 0) flagged++;
          flags += f;
          gold += r.getLong("gold"); xp += r.getLong("experience"); revisions += r.getLong("revision");
          fingerprint.update((r.getString("id") + "|" + r.getLong("revision") + "|" + r.getLong("experience") + "|" + r.getLong("gold")
              + "|" + p + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
      }
      try (var s = c.createStatement(); var r = s.executeQuery("SELECT items::text FROM online_corpses")) {
        while (r.next()) for (var i : JsonParser.parseString(r.getString(1)).getAsJsonArray()) corpseItems += i.getAsJsonObject().get("quantity").getAsLong();
      }
      m.put("characters.inventoryItems", items);
      m.put("characters.storageItems", stored);
      m.put("characters.withQuestFlags", flagged);
      m.put("characters.questFlagsSet", flags);
      m.put("characters.goldTotal", gold);
      m.put("characters.experienceTotal", xp);
      m.put("characters.revisionTotal", revisions);
      m.put("corpses.itemStacksTotal", corpseItems);
      m.put("characters.stateSha256", java.util.HexFormat.of().formatHex(fingerprint.digest()));
    }
    String json = GSON.toJson(m);
    Files.writeString(out, json + "\n");
    System.out.println(json);
  }

  @SuppressWarnings("unchecked")
  private static boolean compare(Path a, Path b) throws Exception {
    Map<String, Object> x = GSON.fromJson(Files.readString(a), Map.class), y = GSON.fromJson(Files.readString(b), Map.class);
    boolean same = true;
    for (var key : new TreeMap<>(x).keySet()) {
      if (key.startsWith("info.")) continue; // time-dependent, reported but not compared
      boolean eq = String.valueOf(x.get(key)).equals(String.valueOf(y.get(key)));
      if (!eq) { same = false; System.out.println("DIFF  " + key + ": source " + x.get(key) + " restore " + y.get(key)); }
    }
    if (!x.keySet().equals(y.keySet())) { same = false; System.out.println("DIFF  key sets differ"); }
    System.out.println(same ? "SUMMARY_IDENTICAL " + x.size() + " measures" : "SUMMARY_DIFFERS");
    return same;
  }
}
