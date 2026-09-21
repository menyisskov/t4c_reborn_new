package com.perso.T4C.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Keeps libGDX out of the rule packages the authoritative game server will run headlessly.
 *
 * <p>The server has no GL context, so any class it loads must not reach for a {@code Texture},
 * {@code Gdx.files} or a {@code SpriteBatch}. These packages are gdx-free today; the point of this
 * test is that they stay that way as the server is built out, because the failure it prevents shows
 * up as a crash on a headless box rather than as a compile error on a developer's desktop.
 *
 * <p>This checks direct textual references only. It is deliberately not proof that a class is
 * headless-runnable — a guarded class can still reach GL indirectly by depending on one that is not
 * guarded (notably {@code player.Player}). Untangling those is separate work; this test only stops
 * the problem getting worse.
 */
class ServerPurityTest {

  private static final Path SOURCE_ROOT = Path.of("src/main/java/com/perso/T4C");

  private static final String LIBGDX_PACKAGE = "com.badlogic.gdx";

  /** Packages whose every class, at any depth, must stay free of libGDX. */
  private static final List<String> GUARDED_PACKAGES =
      List.of("combat", "spell", "item", "quest", "skill", "death", "monster/loot", "model");

  /**
   * Classes inside guarded packages that still reference libGDX, as repo-relative paths under
   * {@link #SOURCE_ROOT}. Each is presentation logic that belongs on the client once the
   * client/server split lands. Shrink this list; never grow it.
   */
  private static final Set<String> KNOWN_VIOLATIONS =
      Set.of(
          "combat/CombatGeometry.java",
          "spell/CompanionCastVfxHook.java",
          "spell/NpcCastVfxHook.java",
          "spell/TameChannel.java");

  @Test
  void guardedPackagesDoNotReferenceLibGdx() throws IOException {
    List<String> unexpected = new ArrayList<>();
    for (String relativePath : sourcesReferencingLibGdx()) {
      if (!KNOWN_VIOLATIONS.contains(relativePath)) {
        unexpected.add(relativePath);
      }
    }
    unexpected.sort(String::compareTo);

    assertTrue(
        unexpected.isEmpty(),
        "These classes are in packages the headless server loads, so they must not reference "
            + LIBGDX_PACKAGE
            + ". Move the rendering concern to the client instead of adding it here:\n  "
            + String.join("\n  ", unexpected));
  }

  /**
   * Fails once a recorded violation has been cleaned up, so the list above cannot silently rot into
   * a record of problems that no longer exist.
   */
  @Test
  void knownViolationListHasNoStaleEntries() throws IOException {
    List<String> stale = new ArrayList<>(KNOWN_VIOLATIONS);
    stale.removeAll(sourcesReferencingLibGdx());
    stale.sort(String::compareTo);

    assertTrue(
        stale.isEmpty(),
        "These classes no longer reference "
            + LIBGDX_PACKAGE
            + " and should be removed from KNOWN_VIOLATIONS:\n  "
            + String.join("\n  ", stale));
  }

  private static List<String> sourcesReferencingLibGdx() throws IOException {
    assertTrue(
        Files.isDirectory(SOURCE_ROOT),
        "expected to run from the repository root, but "
            + SOURCE_ROOT.toAbsolutePath()
            + " is not "
            + "a directory");

    List<String> found = new ArrayList<>();
    for (String guardedPackage : GUARDED_PACKAGES) {
      Path packageRoot = SOURCE_ROOT.resolve(guardedPackage);
      assertTrue(
          Files.isDirectory(packageRoot),
          "guarded package " + guardedPackage + " does not exist; update GUARDED_PACKAGES");

      try (Stream<Path> sources = Files.walk(packageRoot)) {
        for (Path source : sources.filter(ServerPurityTest::isJavaSource).toList()) {
          if (Files.readString(source, StandardCharsets.UTF_8).contains(LIBGDX_PACKAGE)) {
            found.add(SOURCE_ROOT.relativize(source).toString().replace('\\', '/'));
          }
        }
      }
    }
    return found;
  }

  private static boolean isJavaSource(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java");
  }
}
