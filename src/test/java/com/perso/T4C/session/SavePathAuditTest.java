package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Ratchet for the persistence audit (multiplayer/CLIENT_UNIFICATION_DESIGN.md section 3.8): every
 * production call of {@code PlayerStateStore.save} / {@code saveOrThrow} is listed here with its
 * count. Adding a call site, or routing one through the session, fails this test until the audit
 * table and this list are updated on purpose, so a new write path cannot appear unreviewed.
 *
 * <p>Calls that take a {@code Player} are refused for a remote character inside {@code
 * PlayerStateStore.save(Player, float)}. The two DTO-based callers (LocalCharacterDirectory, a
 * stored GM rank of a local slot; LocalCharacterStore, a brand-new local character) never derive
 * their DTO from a {@code Player}, which is why the DTO overloads need no remote guard.
 */
class SavePathAuditTest {
  private static final Pattern CALL =
      Pattern.compile("PlayerStateStore\\s*\\.\\s*(save|saveOrThrow)\\(");

  /** Allowed call counts by file path relative to src/main/java/com/perso/T4C. */
  private static Map<String, Integer> expected() {
    Map<String, Integer> m = new TreeMap<>();
    // Session seam (save(Player, float) and save(Player)) and the DTO writers.
    m.put("session/LocalCharacterPersistence.java", 2);
    m.put("helper/LocalCharacterStore.java", 1);
    m.put("helper/LocalCharacterDirectory.java", 1);
    // Player saves still made directly (all guarded): these classes get no HUD or session.
    m.put("gui/screen/LearnScreen.java", 1);
    m.put("gui/screen/TrainScreen.java", 1);
    m.put("gui/screen/StorageScreen.java", 1);
    m.put("gui/screen/ShopScreen.java", 2);
    m.put("MyGame.java", 1);
    return m;
  }

  @Test
  void everyProductionCallOfThePlayerStateStoreSaveIsAccountedFor() throws IOException {
    assertEquals(expected(), scan());
  }

  static Map<String, Integer> scan() throws IOException {
    Path root = Path.of(System.getProperty("user.dir"), "src/main/java/com/perso/T4C");
    Map<String, Integer> found = new TreeMap<>();
    List<Path> files;
    try (Stream<Path> walk = Files.walk(root)) {
      files = walk.filter(p -> p.toString().endsWith(".java")).toList();
    }
    for (Path file : files) {
      String rel = root.relativize(file).toString().replace('\\', '/');
      if (rel.equals("helper/PlayerStateStore.java")) continue;
      int count = 0;
      for (String line : Files.readAllLines(file)) {
        String t = line.trim();
        if (t.startsWith("*") || t.startsWith("//") || t.startsWith("/*")) continue;
        Matcher matcher = CALL.matcher(line);
        while (matcher.find()) count++;
      }
      if (count > 0) found.put(rel, count);
    }
    return found;
  }
}
