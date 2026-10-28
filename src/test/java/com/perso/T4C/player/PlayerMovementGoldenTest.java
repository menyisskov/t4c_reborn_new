package com.perso.T4C.player;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_X;
import static com.perso.T4C.config.GameConstants.NEW_CHARACTER_START_TILE_Y;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Pins the single-player per-frame movement rule on the real world collision map: seeded start
 * positions (tile anchors and mid-tile pixels), seeded input runs (8 directions, diagonal y scaled
 * by the tile aspect exactly as {@code GameInputHandler} projects it), seeded frame times, a
 * dynamic blocker, a player-passable (teleport) tile set and click-to-move paths. Each scenario
 * records the end position and a CRC over every frame's position, facing, moving flag and
 * reservation state. The golden is a recording of the code as it was before the movement step was
 * moved into the shared domain; the move must reproduce it bit for bit.
 *
 * <p>Regenerate only for an intended single-player movement change: {@code mvn -o
 * -Dt4c.movement.golden.update=true -Dtest=PlayerMovementGoldenTest test}.
 */
@ResourceLock("CollisionManager")
class PlayerMovementGoldenTest {
  private static final Path GOLDEN =
      Path.of("src/test/resources/golden/player-movement/worldmap-seeded.txt");
  private static final Path WORLD_MAP = Path.of("assets/maps/worldmap/worldmap.colbin");
  private static final int SCENARIOS = 400;
  private static final int FRAMES = 60;
  private static final float[] DELTAS = {0.004f, 0.008f, 0.016f, 0.0166667f, 0.033f, 0.05f, 0.1f};
  private static final float TILE_ASPECT = (float) GRID_H / (float) GRID_W;

  @AfterEach
  void clearCollisionManager() {
    CollisionManager.getInstance().clear();
  }

  @Test
  void seededMovementOnTheRealWorldMapMatchesTheRecordedGolden() throws IOException {
    assertTrue(Files.exists(WORLD_MAP), "world collision map missing");
    CollisionReader reader = new CollisionReader(new File(WORLD_MAP.toString()));
    CollisionManager collision = CollisionManager.getInstance();
    collision.initialize(reader);
    // A deterministic, sparse set of extra blockers and teleport-passable tiles exercises the
    // dynamic provider and the player passability branches that real play uses for portals.
    collision.setDynamicProvider((x, y) -> mix((int) (x / GRID_W), (int) (y / GRID_H), 11) % 23 == 0);
    collision.setPlayerPassabilityProvider((x, y) -> mix(x, y, 29) % 41 == 0);

    List<String> lines = new ArrayList<>();
    Random random = new Random(0x7A4C_0007L);
    for (int scenario = 0; scenario < SCENARIOS; scenario++) {
      lines.add(runScenario(scenario, random, reader));
    }

    if (Boolean.getBoolean("t4c.movement.golden.update")) {
      Files.createDirectories(GOLDEN.getParent());
      Files.write(GOLDEN, lines, StandardCharsets.UTF_8);
      return;
    }
    assertTrue(Files.exists(GOLDEN), "no golden; regenerate with -Dt4c.movement.golden.update=true");
    List<String> expected = Files.readAllLines(GOLDEN, StandardCharsets.UTF_8);
    assertEquals(expected.size(), lines.size(), "scenario count");
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), lines.get(i), "scenario " + i);
    }
  }

  private static String runScenario(int scenario, Random random, CollisionReader reader) {
    // Mostly the starting region; every tenth scenario starts at a far, differently shaped place.
    int spread = scenario % 10 == 9 ? 600 : 40;
    int tileX = NEW_CHARACTER_START_TILE_X + random.nextInt(2 * spread + 1) - spread;
    int tileY = NEW_CHARACTER_START_TILE_Y + random.nextInt(2 * spread + 1) - spread;
    float startX = tileX * GRID_W;
    float startY = tileY * GRID_H;
    if (scenario % 4 == 1) { // saved mid-tile, as a load can restore
      startX += random.nextInt(GRID_W);
      startY += random.nextInt(GRID_H);
    }
    Player player = new Player();
    player.setMapBounds(reader.getWidth() * GRID_W, reader.getHeight() * GRID_H);
    player.setWorldPosition(startX, startY, 0);
    if (scenario % 17 == 5) player.setPlayerCollisionsEnabled(false);
    if (scenario % 13 == 3) player.setGmSpeedMultiplier(1.7f);

    boolean pathScenario = scenario % 7 == 6;
    if (pathScenario) {
      List<PlayerMovement.PathNode> nodes = new ArrayList<>();
      int x = tileX;
      int y = tileY;
      for (int n = 0; n < 6; n++) {
        x += random.nextInt(5) - 2;
        y += random.nextInt(5) - 2;
        nodes.add(new PlayerMovement.PathNode(x, y));
      }
      player.getMovement().setPath(nodes);
    }

    CRC32 crc = new CRC32();
    int frame = 0;
    int dx = 0;
    int dy = 0;
    while (frame < FRAMES) {
      int run = 1 + random.nextInt(12);
      if (!pathScenario || random.nextInt(4) == 0) {
        dx = random.nextInt(3) - 1;
        dy = random.nextInt(3) - 1;
      } else {
        dx = 0;
        dy = 0;
      }
      float delta = DELTAS[random.nextInt(DELTAS.length)];
      for (int i = 0; i < run && frame < FRAMES; i++, frame++) {
        player.move(dx == 0 ? 0f : dx, dy * TILE_ASPECT, delta);
        record(crc, player);
      }
    }
    var pos = player.getCoordinates();
    return scenario
        + " "
        + startX
        + " "
        + startY
        + " -> "
        + pos.getX()
        + " "
        + pos.getY()
        + " "
        + Long.toHexString(crc.getValue());
  }

  private static void record(CRC32 crc, Player player) {
    var pos = player.getCoordinates();
    PlayerMovement m = player.getMovement();
    put(crc, Float.floatToIntBits(pos.getX()));
    put(crc, Float.floatToIntBits(pos.getY()));
    put(crc, pos.getZ());
    put(crc, m.isMoving() ? 1 : 0);
    put(crc, m.isFlipX() ? 1 : 0);
    put(crc, m.isActiveGridStep() ? 1 : 0);
    put(crc, m.getCurrentAngle().hashCode());
  }

  private static void put(CRC32 crc, int value) {
    crc.update(value);
    crc.update(value >>> 8);
    crc.update(value >>> 16);
    crc.update(value >>> 24);
  }

  private static int mix(int x, int y, int salt) {
    int h = x * 73856093 ^ y * 19349663 ^ salt * 83492791;
    h ^= h >>> 13;
    h *= 0x5bd1e995;
    h ^= h >>> 15;
    return h & 0x7fffffff;
  }
}
