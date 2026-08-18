package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.MonsterDef;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class MonsterSoundManifestTest {
  private static final String MONSTER_PACKAGE = "com.perso.T4C.monster";
  private static final String MONSTER_PACKAGE_PATH = "com/perso/T4C/monster";
  private static final String CLIENT_SHA256 =
      "f11bdb03f041c2bde381b1104b58458a8f8fec33bb8cb39c9bc3d6e94dc244f8";

  @Test
  void originalClientUsedForTheManifestHasNotChanged() throws Exception {
    byte[] executable = Files.readAllBytes(Path.of("assets", "origin", "carnage", "T4C.exe"));
    String actual =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(executable));

    assertEquals(CLIENT_SHA256, actual);
  }

  @Test
  void everyGeneratedMonsterUsesItsOriginalClientSoundProfile() throws Exception {
    Map<String, ExpectedSounds> manifest = readManifest();
    Set<String> discovered = discoverMonsterDefinitionClasses();
    Set<String> audioAssets = audioAssetNames();
    List<String> errors = new ArrayList<>();

    if (manifest.size() != 422) {
      errors.add("manifest contains " + manifest.size() + " entries instead of 422");
    }
    if (!manifest.keySet().equals(discovered)) {
      Set<String> missing = new LinkedHashSet<>(discovered);
      missing.removeAll(manifest.keySet());
      Set<String> stale = new LinkedHashSet<>(manifest.keySet());
      stale.removeAll(discovered);
      errors.add("manifest coverage differs; missing=" + missing + ", stale=" + stale);
    }

    for (ExpectedSounds expected : manifest.values()) {
      validateExpectedAssets(expected, audioAssets, errors);
      validateMonster(expected, errors);
    }

    assertTrue(errors.isEmpty(), () -> "Monster sound audit failed:\n" + String.join("\n", errors));
  }

  private static void validateExpectedAssets(
      ExpectedSounds expected, Set<String> audioAssets, List<String> errors) {
    for (String sound : new String[] {expected.attack(), expected.death(), expected.hit()}) {
      if (sound != null && !audioAssets.contains(sound)) {
        errors.add(expected.className() + ": missing exact-case audio asset " + sound);
      }
    }
  }

  private static void validateMonster(ExpectedSounds expected, List<String> errors) {
    try {
      Class<?> type = Class.forName(MONSTER_PACKAGE + '.' + expected.className());
      Method definitionMethod = type.getDeclaredMethod("definition");
      MonsterDef definition = (MonsterDef) definitionMethod.invoke(null);

      compare(
          expected,
          "attack constant",
          expected.attack(),
          readSoundConstant(type, "SOUND_ATTACK"),
          errors);
      compare(
          expected,
          "death constant",
          expected.death(),
          readSoundConstant(type, "SOUND_DEATH"),
          errors);
      compare(
          expected, "hit constant", expected.hit(), readSoundConstant(type, "SOUND_HIT"), errors);
      compare(
          expected, "MonsterDef attack", expected.attack(), definition.getSoundAttack(), errors);
      compare(expected, "MonsterDef death", expected.death(), definition.getSoundDeath(), errors);
      compare(expected, "MonsterDef hit", expected.hit(), definition.getSoundHit(), errors);
    } catch (ReflectiveOperationException exception) {
      errors.add(expected.className() + ": cannot inspect definition: " + exception);
    }
  }

  private static String readSoundConstant(Class<?> type, String field)
      throws ReflectiveOperationException {
    return (String) type.getField(field).get(null);
  }

  private static void compare(
      ExpectedSounds expected, String slot, String wanted, String actual, List<String> errors) {
    if (!Objects.equals(wanted, actual)) {
      errors.add(
          expected.className()
              + " [C++ "
              + expected.cppAppearance()
              + '/'
              + expected.cppAppearanceSymbol()
              + "] "
              + slot
              + ": expected="
              + wanted
              + ", actual="
              + actual);
    }
  }

  private static Map<String, ExpectedSounds> readManifest() throws Exception {
    InputStream stream =
        MonsterSoundManifestTest.class.getResourceAsStream("/monster-sound-manifest.csv");
    assertNotNull(stream, "monster-sound-manifest.csv");
    Map<String, ExpectedSounds> result = new LinkedHashMap<>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      for (String line; (line = reader.readLine()) != null; ) {
        if (line.isBlank() || line.startsWith("#") || line.startsWith("class,")) {
          continue;
        }
        String[] columns = line.split(",", -1);
        if (columns.length != 6) {
          throw new IllegalStateException("Invalid monster sound manifest row: " + line);
        }
        ExpectedSounds expected =
            new ExpectedSounds(
                columns[0],
                Integer.parseInt(columns[1]),
                columns[2],
                emptyToNull(columns[3]),
                emptyToNull(columns[4]),
                emptyToNull(columns[5]));
        if (result.put(expected.className(), expected) != null) {
          throw new IllegalStateException("Duplicate manifest class: " + expected.className());
        }
      }
    }
    return result;
  }

  private static String emptyToNull(String value) {
    return value.isEmpty() ? null : value;
  }

  private static Set<String> audioAssetNames() throws Exception {
    try (Stream<Path> paths = Files.list(Path.of("assets", "sounds"))) {
      return paths
          .filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .collect(java.util.stream.Collectors.toSet());
    }
  }

  private static Set<String> discoverMonsterDefinitionClasses() throws Exception {
    ClassLoader loader = MonsterSoundManifestTest.class.getClassLoader();
    Set<String> candidates = new LinkedHashSet<>();
    Enumeration<URL> resources = loader.getResources(MONSTER_PACKAGE_PATH);
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if ("file".equals(resource.getProtocol())) {
        File[] files =
            new File(resource.toURI())
                .listFiles((directory, name) -> name.endsWith(".class") && !name.contains("$"));
        if (files != null) {
          for (File file : files) {
            candidates.add(file.getName().substring(0, file.getName().length() - 6));
          }
        }
      } else if ("jar".equals(resource.getProtocol())) {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        try (JarFile jar = connection.getJarFile()) {
          Enumeration<JarEntry> entries = jar.entries();
          while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            String prefix = MONSTER_PACKAGE_PATH + '/';
            if (name.startsWith(prefix)
                && name.endsWith(".class")
                && !name.substring(prefix.length()).contains("/")
                && !name.contains("$")) {
              candidates.add(name.substring(prefix.length(), name.length() - 6));
            }
          }
        }
      }
    }

    Set<String> definitions = new LinkedHashSet<>();
    for (String candidate : candidates) {
      Class<?> type = Class.forName(MONSTER_PACKAGE + '.' + candidate, false, loader);
      try {
        Method method = type.getDeclaredMethod("definition");
        if (Modifier.isStatic(method.getModifiers())
            && MonsterDef.class.equals(method.getReturnType())) {
          definitions.add(candidate);
        }
      } catch (NoSuchMethodException ignored) {
        // SpawnGroup and test helpers are intentionally not monster definitions.
      }
    }
    return definitions;
  }

  private record ExpectedSounds(
      String className,
      int cppAppearance,
      String cppAppearanceSymbol,
      String attack,
      String death,
      String hit) {}
}
