package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.online.model.PortalContent;
import com.perso.T4C.world.CollisionRules;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The shared 1-byte grid must answer exactly what the per-region array loader answers. */
class SharedCollisionTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");
  private static List<PortalContent.Portal> enabled;
  private static SharedCollision shared;
  private static byte[] raw;
  private static int width, height;

  @BeforeAll static void load() throws Exception {
    PortalContent content;
    try (var in = HostedRegionPlan.class.getResourceAsStream("/content/portals-v1.json")) {
      content = new Gson().fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), PortalContent.class);
    }
    enabled = content.portals().stream().filter(p -> HostedRegionPlan.ENABLED_PORTALS.contains(p.id())).toList();
    shared = SharedCollision.load(WORLD, 0, enabled);
    try (var in = new DataInputStream(BinaryIOUtils.openInputStream(WORLD.toFile(), 1 << 20))) {
      width = BinaryIOUtils.readIntLE(in);
      height = BinaryIOUtils.readIntLE(in);
      raw = in.readNBytes(width * height);
    }
  }

  @Test void theGridHoldsOneBytePerTileOfTheWholeWorld() {
    assertEquals(6144, shared.width());
    assertEquals(3072, shared.height());
    assertEquals(6144L * 3072L, shared.gridBytes());
  }

  @Test void everyTerrainCodeAndTheFootRuleMatchTheFileOnEveryTileOfTheWorld() {
    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; x++) {
        int code = raw[y * width + x] & 255;
        if (shared.terrain(x, y) != code || shared.foot(x, y) != CollisionRules.blocksMovement(code))
          fail("Terrain or foot mismatch at " + x + "," + y);
      }
  }

  @Test void theStarterCropMatchesTheArrayLoaderOnEveryTileAndEveryPublicRule() throws Exception {
    var spec = new RegionMapLoader.Spec(0, "start", HostedRegionPlan.START_X, HostedRegionPlan.START_Y,
        HostedRegionPlan.START_WIDTH, HostedRegionPlan.START_HEIGHT, 2944, 1059);
    var arrays = RegionMapLoader.load(WORLD, spec, enabled);
    var window = new PreviewMap(shared, spec.originX(), spec.originY(), spec.width(), spec.height(), 2944 - spec.originX(), 1059 - spec.originY());
    assertSameRules(arrays, window, new Random(1));
  }

  @Test void randomWindowsAcrossTheWholeWorldMatchTheArrayLoader() throws Exception {
    var random = new Random(20261030);
    int compared = 0;
    for (int attempt = 0; attempt < 400 && compared < 40; attempt++) {
      int size = 40 + random.nextInt(160);
      // Include the corners and edges: the above-foot fold treats the row above the map as blocked.
      int ox = switch (attempt % 4) { case 0 -> 0; case 1 -> width - size; default -> random.nextInt(width - size); };
      int oy = switch (attempt % 5) { case 0 -> 0; case 1 -> height - size; default -> random.nextInt(height - size); };
      int[] spawn = null;
      search:
      for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
        if (!shared.blocked(ox + x, oy + y)) { spawn = new int[] {x, y}; break search; }
      if (spawn == null) continue;
      var spec = new RegionMapLoader.Spec(0, "t", ox, oy, size, size, ox + spawn[0], oy + spawn[1]);
      var arrays = RegionMapLoader.load(WORLD, spec, enabled);
      var window = new PreviewMap(shared, ox, oy, size, size, spawn[0], spawn[1]);
      assertSameRules(arrays, window, random);
      compared++;
    }
    assertTrue(compared >= 30, "enough windows had a standable tile: " + compared);
  }

  @Test void aWindowOverTheSharedGridCopiesNothingAndRefusesToPublishAGrid() {
    var window = new PreviewMap(shared, 0, 0, 6144, 3072, 2944, 1059);
    assertTrue(window.streamed());
    assertEquals(6144, window.width);
    assertThrows(IllegalStateException.class, window::cells);
    assertThrows(IllegalArgumentException.class, () -> new PreviewMap(shared, 1, 0, 6144, 3072, 2944, 1059));
    assertThrows(IllegalArgumentException.class, () -> new PreviewMap(shared, 0, 0, 100, 100, 0, 0), "blocked spawn tile");
  }

  @Test void moreThanSixteenTerrainCodesAreRefusedNotSilentlyMerged() {
    var data = new byte[17 * 2];
    for (int i = 0; i < data.length; i++) data[i] = (byte) (i / 2);
    assertThrows(java.io.IOException.class, () -> SharedCollision.build(0, 17, 2, data, List.of()));
    assertDoesNotThrow(() -> SharedCollision.build(0, 16, 2, java.util.Arrays.copyOf(data, 32), List.of()));
  }

  private static void assertSameRules(PreviewMap expected, PreviewMap actual, Random random) {
    assertEquals(expected.width, actual.width);
    assertEquals(expected.height, actual.height);
    assertEquals(expected.spawnX, actual.spawnX);
    for (int y = -1; y <= expected.height; y++)
      for (int x = -1; x <= expected.width; x++) {
        double cx = x + .5, cy = y + .5;
        if (expected.canStand(cx, cy) != actual.canStand(cx, cy)) fail("canStand differs at " + x + "," + y);
        if (expected.canRestOnGrid(cx, cy) != actual.canRestOnGrid(cx, cy)) fail("canRestOnGrid differs at " + x + "," + y);
        if (expected.blocksFoot(x, y) != actual.blocksFoot(x, y)) fail("blocksFoot differs at " + x + "," + y);
        if (expected.portalPassable(x, y) != actual.portalPassable(x, y)) fail("portalPassable differs at " + x + "," + y);
      }
    var checked = new ArrayList<double[]>();
    for (int i = 0; i < 300; i++)
      checked.add(new double[] {random.nextDouble() * expected.width, random.nextDouble() * expected.height,
          random.nextDouble() * expected.width, random.nextDouble() * expected.height});
    for (var q : checked) {
      assertEquals(expected.hasLineOfSight(q[0], q[1], q[2], q[3]), actual.hasLineOfSight(q[0], q[1], q[2], q[3]));
      assertEquals(expected.hasSpellLineOfSight(q[0], q[1], q[2], q[3]), actual.hasSpellLineOfSight(q[0], q[1], q[2], q[3]));
      assertEquals(expected.combatEndpointsSafe(q[0], q[1], q[2], q[3], false), actual.combatEndpointsSafe(q[0], q[1], q[2], q[3], false));
      assertEquals(expected.combatEndpointsSafe(q[0], q[1], q[2], q[3], true), actual.combatEndpointsSafe(q[0], q[1], q[2], q[3], true));
      assertEquals(expected.onGridPath(q[0], q[1]), actual.onGridPath(q[0], q[1]));
    }
  }
}
