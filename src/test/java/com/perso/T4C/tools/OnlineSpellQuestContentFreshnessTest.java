package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Same freshness contract as OnlineMonsterContentExporterTest, for the spell and quest exports. */
class OnlineSpellQuestContentFreshnessTest {
  @TempDir Path temporary;

  @org.junit.jupiter.api.AfterEach
  void restoreSharedRegistries() {
    // snapshot() loads the JSON items into the static registry; do not leak them to later tests.
    com.perso.T4C.item.ItemRegistry.resetAdditionalDefinitions();
  }

  @org.junit.jupiter.api.Test
  void onlineQuestExportIncludesTheHostedLighthavenRatQuest() {
    var quest = OnlineQuestContentExporter.snapshot().quests().get("lighthaven_samaritan_rats");
    assertNotNull(quest);
    assertEquals("LighthavenSamaritan", quest.giverNpc());
    assertEquals("Brown Rat", quest.targetMonster());
    assertEquals(15, quest.requiredKills());
    assertEquals("__NEWBIE_QUEST", quest.activationFlag());
    assertEquals("__NEWBIE_QUEST", com.perso.T4C.quest.QuestRegistry
        .findById("lighthaven_samaritan_rats").getActivationFlag(),
        "the offline authored definition keeps its legacy shared activation marker");
  }

  @ParameterizedTest
  @ValueSource(strings = {"spells", "quests", "spells-v2"})
  void packagedContentMatchesActualRuntimeDefinitions(String kind) throws Exception {
    String exporter = switch (kind) {
      case "spells" -> OnlineSpellContentExporter.class.getName();
      case "quests" -> OnlineQuestContentExporter.class.getName();
      case "spells-v2" -> OnlineSpellContentV2Exporter.class.getName();
      default -> throw new IllegalArgumentException("Unknown content kind " + kind);
    };
    String file = kind.equals("spells-v2") ? "spells-v2.json" : kind + "-v1.json";
    String actual = Files.readString(Path.of("multiplayer/server/src/main/resources/content/" + file))
        .replace("\r\n", "\n");
    // Export in a fresh production-classpath JVM so freshness never depends on test ordering.
    String tests = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    String classpath = java.util.Arrays.stream(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"))
            .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator)))
        .filter(entry -> !Path.of(entry).toAbsolutePath().normalize().equals(Path.of(tests).toAbsolutePath().normalize()))
        .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
    String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
    Path generated = temporary.resolve(kind + ".json"), log = temporary.resolve("export.log");
    var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
        "-cp", classpath, exporter, generated.toString())
        .redirectErrorStream(true).redirectOutput(log.toFile()).start();
    try {
      assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Content exporter timed out");
      String output = Files.readString(log);
      assertEquals(0, process.exitValue(), output.substring(Math.max(0, output.length() - 2000)));
    } finally {
      if (process.isAlive()) { process.destroyForcibly(); process.waitFor(5, TimeUnit.SECONDS); }
    }
    String expected = Files.readString(generated);
    if (!digest(expected).equals(digest(actual))) {
      var report = Path.of("target/surefire-reports/online-" + kind + "-runtime.json");
      Files.createDirectories(report.getParent());
      Files.writeString(report, expected);
    }
    assertEquals(digest(expected), digest(actual),
        "Online content is stale: run " + exporter + " for the bundled JSON");
  }

  private static String digest(String value) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }
}
