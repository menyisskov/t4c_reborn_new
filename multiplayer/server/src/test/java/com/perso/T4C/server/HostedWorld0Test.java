package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRegion;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Stage A part 1: the streamed whole-world-0 plan (no creature activation yet), real collision data. */
class HostedWorld0Test {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");
  private static HostedRegionPlan plan;

  @BeforeAll static void build() throws Exception { plan = HostedRegionPlan.startingSlice(WORLD, true); }

  @Test void theModelBoundIsRaisedWhileNonStreamedSpecsKeepTheOldRule() {
    assertEquals(6144, new OnlineRegion(0, "start", 0, 0, 6144, 3072, 0).width());
    assertEquals(8192, new OnlineRegion(0, "start", 0, 0, 8192, 8192, 0).height());
    assertThrows(IllegalArgumentException.class, () -> new OnlineRegion(0, "start", 0, 0, 8193, 10, 0));
    assertThrows(IllegalArgumentException.class, () -> new OnlineRegion(0, "start", 0, 0, 10, 8193, 0));
    assertThrows(IllegalArgumentException.class, () -> new RegionMapLoader.Spec(0, "big", 0, 0, 257, 100, 1, 1),
        "a region that is not streamed keeps the 256 rule");
    assertThrows(IllegalArgumentException.class, () -> new RegionMapLoader.Spec(0, "big", 0, 0, 100, 257, 1, 1));
    assertEquals(256, new RegionMapLoader.Spec(0, "ok", 0, 0, 256, 256, 1, 1).width());
    assertThrows(IllegalArgumentException.class, () -> new RegionMapLoader.Spec(0, "huge", 0, 0, 8193, 100, 1, 1, true));
    assertEquals(6144, new RegionMapLoader.Spec(0, "w", 0, 0, 6144, 3072, 1, 1, true).width());
  }

  @Test void theDefaultPlanIsUntouchedAndTheStreamedPlanHostsAllOfWorldZero() throws Exception {
    var normal = HostedRegionPlan.startingSlice(WORLD);
    assertFalse(normal.streamed());
    assertEquals(249, normal.regions().getFirst().spec().width());
    assertTrue(normal.sharedCollision.isEmpty());
    assertTrue(plan.streamed());
    var start = plan.regions().getFirst().spec();
    assertEquals("start", start.key());
    assertEquals(0, start.originX());
    assertEquals(6144, start.width());
    assertEquals(3072, start.height());
    assertEquals(6, plan.regions().size(), "the five world-1 pieces stay as shipped");
    assertEquals(1, plan.sharedCollision.size());
    // A character saved far outside the old crop is hosted now; it was an unhosted_region before.
    assertThrows(PreviewWorld.Rejected.class, () -> normal.require(0, 3700.5, 1450.5));
    assertEquals("start", plan.require(0, 3700.5, 1450.5).spec().key());
    assertEquals("start", plan.require(0, 0.5, 0.5).spec().key());
    assertThrows(PreviewWorld.Rejected.class, () -> plan.require(0, 6144.5, 10));
    assertThrows(PreviewWorld.Rejected.class, () -> plan.require(2, 100, 100), "worlds 1 and 2 stay as shipped");
  }

  @Test void portalsBetweenHostedRegionsAreDerivedAndSameRegionOnesStayRefused() {
    var normal = enabledIds(HostedRegionPlan.ENABLED_PORTALS);
    var all = new HashSet<Integer>();
    for (var portal : plan.enabledPortals) all.add(portal.id());
    assertTrue(all.containsAll(normal), "every shipped route stays enabled");
    for (var refused : plan.refusedPortals) {
      var sourceRegion = plan.require(refused.sourceWorld(), refused.sourceX() + .5, refused.sourceY() + .5);
      boolean sameRegion = false;
      try {
        sameRegion = plan.require(refused.targetWorld(), refused.targetX() + .5, refused.targetY() + .5) == sourceRegion;
      } catch (PreviewWorld.Rejected unhosted) { /* target not hosted: refused as before */ }
      boolean hostedTarget = true;
      try { plan.require(refused.targetWorld(), refused.targetX() + .5, refused.targetY() + .5); }
      catch (PreviewWorld.Rejected unhosted) { hostedTarget = false; }
      assertTrue(!hostedTarget || sameRegion || !plan.canStand(new com.perso.T4C.online.model.CharacterProgress.Respawn(
          refused.targetWorld(), refused.targetX(), refused.targetY())), "refused portal " + refused.id() + " is explained");
    }
    // Every world-0 to world-0 portal is a same-region transfer and waits for stage B.
    long worldZeroInternal = plan.refusedPortals.stream().filter(p -> p.sourceWorld() == 0 && p.targetWorld() == 0).count();
    assertEquals(76, worldZeroInternal);
  }

  private static Set<Integer> enabledIds(Set<Integer> ids) { return ids; }

  /** The whole world as the old per-tile arrays at origin 0, built here from the raw file as an independent oracle. */
  private static PreviewMap wholeWorldArrays() throws Exception {
    byte[] data;
    int w, h;
    try (var in = new java.io.DataInputStream(com.perso.T4C.helper.BinaryIOUtils.openInputStream(WORLD.toFile(), 1 << 20))) {
      w = com.perso.T4C.helper.BinaryIOUtils.readIntLE(in);
      h = com.perso.T4C.helper.BinaryIOUtils.readIntLE(in);
      data = in.readNBytes(w * h);
    }
    var blocked = new boolean[h][w];
    var terrain = new byte[h][w];
    var foot = new boolean[h][w];
    var passable = new boolean[h][w];
    for (int y = 0; y < h; y++)
      for (int x = 0; x < w; x++) {
        terrain[y][x] = data[y * w + x];
        foot[y][x] = com.perso.T4C.world.CollisionRules.blocksMovement(data[y * w + x] & 255);
        blocked[y][x] = y == 0 || foot[y][x] || com.perso.T4C.world.CollisionRules.blocksMovement(data[(y - 1) * w + x] & 255);
      }
    for (var portal : plan.enabledPortals)
      if (portal.sourceWorld() == 0) { blocked[portal.sourceY()][portal.sourceX()] = false; passable[portal.sourceY()][portal.sourceX()] = true; }
    return new PreviewMap(blocked, terrain, foot, passable, 0, 0, 2944, 1059);
  }

  @Test void aPlayerWalksOverTheSharedGridExactlyLikeOverTheOldArrays() throws Exception {
    // Same origin, so the same floats: any difference would be a different rule, not rounding.
    var streamedWorld = new PreviewWorld(plan.regions().getFirst().map(), null, 0);
    var arrayWorld = new PreviewWorld(wholeWorldArrays(), null, 0);
    var random = new Random(77);
    // Start at the temple, then the far shore, then a monster-field region.
    double[][] starts = {{2944.5, 1059.5}, {3700.5, 1450.5}, {1200.5, 2000.5}};
    int moved = 0;
    for (double[] start : starts) {
      var a = streamedWorld.joinAt(start[0], start[1]);
      var b = arrayWorld.joinAt(start[0], start[1]);
      long sequence = 0;
      int dx = 0, dy = 0;
      for (int tick = 0; tick < 1500; tick++) {
        if (tick % 17 == 0) { dx = random.nextInt(3) - 1; dy = random.nextInt(3) - 1; }
        sequence++;
        streamedWorld.input(a.token(), sequence, dx, dy);
        arrayWorld.input(b.token(), sequence, dx, dy);
        streamedWorld.step();
        arrayWorld.step();
        var pa = streamedWorld.position(a.token());
        var pb = arrayWorld.position(b.token());
        assertEquals(pb.x(), pa.x(), 1e-9, "tick " + tick);
        assertEquals(pb.y(), pa.y(), 1e-9, "tick " + tick);
        if (streamedWorld.moving(a.token())) moved++;
      }
      streamedWorld.leave(a.token());
      arrayWorld.leave(b.token());
    }
    assertTrue(moved > 1500, "the walks actually moved: " + moved);
  }

  @Test void aStreamedPlayerCannotLeaveTheMapOrEnterRockAtTheWorldEdge() throws Exception {
    var world = new PreviewWorld(plan.regions().getFirst().map(), null, 0);
    var joined = world.joinAt(2944.5, 1059.5);
    long sequence = 0;
    for (int tick = 0; tick < 6000; tick++) { // walk north-west for a long time
      world.input(joined.token(), ++sequence, -1, -1);
      world.step();
      var at = world.position(joined.token());
      assertTrue(at.x() >= 0 && at.y() >= 0 && at.x() < 6144 && at.y() < 3072);
      assertTrue(plan.regions().getFirst().map().canRestOnGrid(at.x(), at.y()) || world.moving(joined.token()));
    }
  }


  @Test void snapshotsListOnlyTheViewersWindowAndMatchABruteForceFilter() throws Exception {
    var map = plan.regions().getFirst().map();
    var world = new PreviewWorld(map, null, 0);
    var random = new Random(5);
    var tokens = new ArrayList<String>();
    var at = new ArrayList<double[]>();
    int[][] clusters = {{2944, 1059}, {3700, 1450}, {1200, 2000}};
    while (tokens.size() < PreviewWorld.MAX_PLAYERS) {
      var c = clusters[random.nextInt(clusters.length)];
      double x = c[0] + random.nextInt(130) - 65 + .5, y = c[1] + random.nextInt(130) - 65 + .5;
      if (!map.canRestOnGrid(x, y)) continue;
      var joined = world.joinAt(x, y);
      tokens.add(joined.token());
      at.add(new double[] {x, y, joined.id()});
    }
    for (int viewer = 0; viewer < tokens.size(); viewer++) {
      var snapshot = world.snapshot(tokens.get(viewer));
      var me = at.get(viewer);
      var expected = new HashSet<Long>();
      for (var other : at)
        if (Math.abs(Math.floor(other[0]) - Math.floor(me[0])) <= 48 && Math.abs(Math.floor(other[1]) - Math.floor(me[1])) <= 48)
          expected.add((long) other[2]);
      var actual = new HashSet<Long>();
      for (var player : snapshot.players()) actual.add(player.id());
      assertEquals(expected, actual, "viewer " + viewer);
      assertTrue(actual.contains((long) me[2]));
      var window = snapshot.window();
      assertNotNull(window);
      assertEquals(97, Math.max(window.width(), window.height()), "interior windows are 97 tiles");
      assertEquals(Boolean.FALSE, snapshot.truncated());
      assertTrue(snapshot.creatures().isEmpty());
    }
    // The three clusters are thousands of tiles apart: nobody in one can see another.
    var far = world.snapshot(tokens.getFirst());
    assertTrue(far.players().size() < tokens.size());
  }

  @Test void aNonStreamedWorldStillSnapshotsEveryoneWithoutAWindow() {
    var world = new PreviewWorld(new PreviewMap(new boolean[200][200], 0, 0, 5, 5));
    var one = world.join();
    var two = world.joinAt(150.5, 150.5);
    var snapshot = world.snapshot(one.token());
    assertNull(snapshot.window());
    assertNull(snapshot.truncated());
    assertEquals(2, snapshot.players().size());
    assertNotNull(two);
  }

  @Test void streamedMapJsonCarriesNoGridAndStateIsWindowedOverRealHttp() throws Exception {
    try (var server = new PreviewServer(plan, 0, null, null, ServerTransport.local()); var client = HttpClient.newHttpClient()) {
      server.start();
      String url = server.url();
      var map = get(client, url, "/map", null, null);
      assertEquals(200, map.statusCode());
      var json = JsonParser.parseString(map.body()).getAsJsonObject();
      assertEquals(OnlineProtocol.VERSION, json.get("protocolVersion").getAsInt());
      assertTrue(json.get("streamed").getAsBoolean());
      assertFalse(json.has("blocked"), "a streamed region publishes no grid");
      assertEquals(6144, json.get("width").getAsInt());
      assertEquals(3072, json.get("height").getAsInt());
      assertTrue(map.body().length() < 600, "bytes: " + map.body().length());

      var joined = client.send(HttpRequest.newBuilder(URI.create(url + "/join")).header("Origin", url)
          .header(OnlineProtocol.VERSION_HEADER, Integer.toString(OnlineProtocol.VERSION))
          .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("")).build(), HttpResponse.BodyHandlers.ofString());
      assertEquals(200, joined.statusCode());
      String token = JsonParser.parseString(joined.body()).getAsJsonObject().get("token").getAsString();
      var state = get(client, url, "/state", token, null);
      assertEquals(200, state.statusCode());
      var snapshot = JsonParser.parseString(state.body()).getAsJsonObject();
      var window = snapshot.getAsJsonObject("window");
      assertEquals(97, window.get("width").getAsInt());
      assertEquals(2896, window.get("x").getAsInt());
      assertFalse(snapshot.get("truncated").getAsBoolean());
      assertEquals(6144, snapshot.getAsJsonObject("region").get("width").getAsInt());
      assertTrue(snapshot.getAsJsonArray("npcs").size() <= 64);
      assertTrue(state.body().length() < 8_000, "state bytes: " + state.body().length());
      var authenticated = get(client, url, "/map", token, null);
      assertFalse(JsonParser.parseString(authenticated.body()).getAsJsonObject().has("blocked"));
    }
  }

  @Test void aPlainServerKeepsTheOldWireShapeApartFromTheProtocolNumber() throws Exception {
    try (var server = new PreviewServer(new PreviewMap(new boolean[60][80], 0, 0, 30, 30), 0); var client = HttpClient.newHttpClient()) {
      server.start();
      String url = server.url();
      var json = JsonParser.parseString(get(client, url, "/map", null, null).body()).getAsJsonObject();
      assertTrue(json.has("blocked"));
      assertFalse(json.has("streamed"));
      var joined = client.send(HttpRequest.newBuilder(URI.create(url + "/join")).header("Origin", url)
          .header(OnlineProtocol.VERSION_HEADER, Integer.toString(OnlineProtocol.VERSION))
          .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("")).build(), HttpResponse.BodyHandlers.ofString());
      String token = JsonParser.parseString(joined.body()).getAsJsonObject().get("token").getAsString();
      JsonObject state = JsonParser.parseString(get(client, url, "/state", token, null).body()).getAsJsonObject();
      assertFalse(state.has("window"));
      assertFalse(state.has("truncated"));
    }
  }

  @Test void anOlderClientIsRefusedBeforeAnyCredentialAtTheNewVersion() throws Exception {
    assertEquals(9, OnlineProtocol.VERSION);
    try (var server = new PreviewServer(plan, 0, null, null, ServerTransport.local()); var client = HttpClient.newHttpClient()) {
      server.start();
      String url = server.url();
      var old = client.send(HttpRequest.newBuilder(URI.create(url + "/join")).header("Origin", url)
          .header(OnlineProtocol.VERSION_HEADER, "8").header("Content-Type", "text/plain")
          .POST(HttpRequest.BodyPublishers.ofString("")).build(), HttpResponse.BodyHandlers.ofString());
      assertEquals(426, old.statusCode());
    }
  }

  private static HttpResponse<String> get(HttpClient client, String url, String path, String token, String body) throws Exception {
    var builder = HttpRequest.newBuilder(URI.create(url + path)).timeout(java.time.Duration.ofSeconds(10));
    if (token != null) builder.header("Authorization", "Bearer " + token);
    return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }
}
