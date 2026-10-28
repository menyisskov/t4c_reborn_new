package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Stage 0 safety net for the client unification (multiplayer/CLIENT_UNIFICATION_DESIGN.md): golden
 * screenshots of the real single-player {@link MainGameScreen}. Any later refactor that changes how
 * single-player looks fails here.
 *
 * <p>The images come from {@link MainGameScreenGoldenPreview} run in a child JVM (a real LibGDX
 * window, a fixed fixture character, no network, no write to the real save directory). When there
 * is no display or OpenGL context (CI) the test is skipped, not failed; locally it runs.
 *
 * <p>Tolerance: a pixel differs when any colour channel is more than {@value #CHANNEL_TOLERANCE}
 * apart; the image passes while at most {@value #MAX_DIFFERING_PERCENT}% of pixels differ. On one
 * machine two runs are byte-identical; the tolerance only absorbs driver and anti-aliasing
 * differences between machines.
 *
 * <p>Regenerate the goldens after an intended visual change (review the PNGs in the diff!): {@code
 * mvn -o -Dt4c.golden.update=true -Dtest=MainGameScreenGoldenTest test}. Failure images and diffs
 * are written to {@code target/golden-diff}.
 */
class MainGameScreenGoldenTest {
  static final int CHANNEL_TOLERANCE = 8;
  static final double MAX_DIFFERING_PERCENT = 0.5;
  private static final Path GOLDEN_DIR = Path.of("src/test/resources/golden/main-game-screen");
  private static final Path DIFF_DIR = Path.of("target/golden-diff");
  private static Path sharedOut;
  private static String sharedLog = "";

  @Test
  void singlePlayerScreensMatchTheGoldenImages() throws Exception {
    assumeFalse(Boolean.getBoolean("t4c.golden.skip"), "golden screenshots disabled by property");
    assumeFalse(GraphicsEnvironment.isHeadless(), "no display: golden screenshots need a window");
    assumeTrue(Files.isDirectory(Path.of("assets")), "assets are required to render the game");

    Path out = sharedOutput();
    {
      String log = sharedLog;
      for (String state : MainGameScreenGoldenPreview.STATES) {
        assertTrue(
            Files.exists(out.resolve(state + ".png")),
            "preview did not produce " + state + ".png:\n" + log);
      }
      boolean update = Boolean.getBoolean("t4c.golden.update");
      if (update) Files.createDirectories(GOLDEN_DIR);
      List<String> failures = new ArrayList<>();
      for (String state : MainGameScreenGoldenPreview.STATES) {
        Path actual = out.resolve(state + ".png");
        Path golden = GOLDEN_DIR.resolve(state + ".png");
        if (update) {
          Files.copy(actual, golden, StandardCopyOption.REPLACE_EXISTING);
          continue;
        }
        if (!Files.exists(golden)) {
          failures.add(state + ": no golden image; regenerate with -Dt4c.golden.update=true");
          continue;
        }
        Comparison result = compare(ImageIO.read(golden.toFile()), ImageIO.read(actual.toFile()));
        if (!result.passes()) {
          Files.createDirectories(DIFF_DIR);
          Files.copy(
              actual, DIFF_DIR.resolve(state + "-actual.png"), StandardCopyOption.REPLACE_EXISTING);
          ImageIO.write(result.diffImage(), "png", DIFF_DIR.resolve(state + "-diff.png").toFile());
          failures.add(state + ": " + result);
        }
      }
      assertTrue(failures.isEmpty(), "single-player screen changed: " + failures);
    }
  }

  /** The 15-second autosave cadence the session seam must keep (read from the same child run). */
  @Test
  void autosaveWritesTheSaveEveryFifteenSecondsAndNotBefore() throws Exception {
    assumeFalse(Boolean.getBoolean("t4c.golden.skip"), "golden screenshots disabled by property");
    assumeFalse(GraphicsEnvironment.isHeadless(), "no display: needs the real screen");
    assumeTrue(Files.isDirectory(Path.of("assets")), "assets are required to render the game");
    Path out = sharedOutput();
    {
      String log = sharedLog;
      Path report = out.resolve("autosave.txt");
      assertTrue(Files.exists(report), "preview wrote no autosave report:\n" + log);
      Properties values = new Properties();
      values.load(Files.newBufferedReader(report, StandardCharsets.UTF_8));
      assertEquals(null, values.getProperty("error"), "autosave probe failed");
      assertEquals("false", values.getProperty("earlyWrite"), "saved after 1 s");
      assertEquals("false", values.getProperty("afterFourteen"), "saved before 15 s");
      assertEquals("true", values.getProperty("afterSixteen"), "no save after 16 s");
      assertEquals("Goldie", values.getProperty("reloadedName"));
      assertEquals("12", values.getProperty("reloadedLevel"));
      assertEquals("1234", values.getProperty("reloadedGold"));
    }
  }

  private static Path sharedOutput() throws IOException, InterruptedException {
    if (sharedOut == null) {
      Path out = Files.createTempDirectory("t4c-golden-out");
      sharedLog = runPreview(out);
      sharedOut = out;
    }
    return sharedOut;
  }

  @AfterAll
  static void cleanUp() throws IOException {
    if (sharedOut != null) deleteTree(sharedOut);
    sharedOut = null;
  }

  @Test
  void comparatorAcceptsSmallNoiseAndRejectsRealChanges() {
    BufferedImage base = solid(100, 100, 0x336699);
    BufferedImage noisy = solid(100, 100, 0x336699);
    noisy.setRGB(0, 0, 0x336699 + 0x050505); // within the channel tolerance
    assertTrue(compare(base, noisy).passes());
    BufferedImage few = solid(100, 100, 0x336699);
    for (int i = 0; i < 40; i++) few.setRGB(i, 0, 0xFF0000); // 0.4% differ: tolerated
    assertTrue(compare(base, few).passes());
    BufferedImage many = solid(100, 100, 0x336699);
    for (int i = 0; i < 80; i++) many.setRGB(i, 0, 0xFF0000); // 0.8% differ: rejected
    assertFalse(compare(base, many).passes());
    assertFalse(compare(base, solid(100, 101, 0x336699)).passes(), "size change must fail");
  }

  record Comparison(boolean sameSize, long differing, long total, BufferedImage diffImage) {
    boolean passes() {
      return sameSize && differing * 100.0 / total <= MAX_DIFFERING_PERCENT;
    }

    @Override
    public String toString() {
      return sameSize
          ? String.format(
              Locale.ROOT,
              "%d of %d pixels differ (%.3f%%, limit %.2f%%)",
              differing,
              total,
              differing * 100.0 / total,
              MAX_DIFFERING_PERCENT)
          : "image size changed";
    }
  }

  static Comparison compare(BufferedImage golden, BufferedImage actual) {
    if (golden.getWidth() != actual.getWidth() || golden.getHeight() != actual.getHeight()) {
      return new Comparison(false, 0, 1, actual);
    }
    int w = golden.getWidth(), h = golden.getHeight();
    BufferedImage diff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    long differing = 0;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int a = golden.getRGB(x, y), b = actual.getRGB(x, y);
        int delta =
            Math.max(
                Math.abs(((a >> 16) & 255) - ((b >> 16) & 255)),
                Math.max(
                    Math.abs(((a >> 8) & 255) - ((b >> 8) & 255)),
                    Math.abs((a & 255) - (b & 255))));
        if (delta > CHANNEL_TOLERANCE) {
          differing++;
          diff.setRGB(x, y, 0xFF0000);
        } else {
          diff.setRGB(x, y, (b & 0xFCFCFC) >> 2);
        }
      }
    }
    return new Comparison(true, differing, (long) w * h, diff);
  }

  private static BufferedImage solid(int w, int h, int rgb) {
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) image.setRGB(x, y, rgb);
    return image;
  }

  /** Runs the preview in a child JVM; skips the test when the machine has no usable GL window. */
  private static String runPreview(Path out) throws IOException, InterruptedException {
    String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    ProcessBuilder builder =
        new ProcessBuilder(
            java,
            "-cp",
            System.getProperty("java.class.path"),
            MainGameScreenGoldenPreview.class.getName(),
            out.toString());
    builder.redirectErrorStream(true);
    File logFile = Files.createTempFile("t4c-golden-log", ".txt").toFile();
    builder.redirectOutput(logFile);
    Process process = builder.start();
    boolean finished = process.waitFor(240, TimeUnit.SECONDS);
    if (!finished) process.destroyForcibly().waitFor();
    String log = Files.readString(logFile.toPath(), StandardCharsets.UTF_8);
    logFile.delete();
    assertTrue(finished, "preview did not finish in time:\n" + tail(log));
    if (process.exitValue() != 0) {
      String lower = log.toLowerCase(Locale.ROOT);
      assumeFalse(
          lower.contains("glfw") || lower.contains("opengl") || lower.contains("display"),
          "no usable display or OpenGL context: " + tail(log));
      throw new AssertionError(
          "preview failed with exit " + process.exitValue() + ":\n" + tail(log));
    }
    return tail(log);
  }

  private static String tail(String log) {
    return log.length() <= 4000 ? log : log.substring(log.length() - 4000);
  }

  private static void deleteTree(Path root) throws IOException {
    if (!Files.exists(root)) return;
    try (var walk = Files.walk(root)) {
      walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
    }
  }
}
