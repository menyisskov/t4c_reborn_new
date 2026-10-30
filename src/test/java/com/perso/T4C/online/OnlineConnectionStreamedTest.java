package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRegion;
import com.perso.T4C.online.model.OnlineWindow;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Protocol 9: a streamed map is accepted at world size and its windowed snapshots are validated. */
class OnlineConnectionStreamedTest {
  private static final String WINDOW = "\"window\":{\"x\":2896,\"y\":1011,\"width\":97,\"height\":97}";
  private static final String GUARD_AT = "{\"id\":3,\"definition\":\"Guard\",\"x\":%d,\"y\":%d,"
      + "\"health\":5,\"maxHealth\":5,\"walkPattern\":\"Warrio#l\"}";

  /** Runs one temporary-play connection against a stub; returns "connected" or the failure text. */
  private static String run(int width, int height, String mapExtra, double playerX, double playerY,
      String stateFields, OnlineConnection.View[] holder) throws Exception {
    var socketPort = new AtomicInteger();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    String region = "{\"world\":0,\"key\":\"start\",\"locationEpoch\":0,\"originX\":0,\"originY\":0,\"width\":"
        + width + ",\"height\":" + height + "}";
    server.createContext("/", exchange -> {
      String result = switch (exchange.getRequestURI().getPath()) {
        case "/map" -> "{\"protocolVersion\":" + OnlineProtocol.VERSION + ",\"gameSocketPort\":" + socketPort.get()
            + ",\"persistent\":false," + region.substring(1, region.length() - 1) + mapExtra + "}";
        case "/join" -> "{\"token\":\"" + "a".repeat(64) + "\",\"id\":7,\"region\":" + region + "}";
        case "/state" -> "{\"tick\":8,\"region\":" + region + ",\"players\":[{\"id\":7,\"x\":" + playerX + ",\"y\":" + playerY
            + ",\"sequence\":0,\"hp\":10,\"maxHp\":10}]," + stateFields + "}";
        case "/chat" -> "{\"cursor\":0,\"messages\":[]}";
        default -> "{}";
      };
      byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    var bridge = new LoopbackGameSocket(server);
    socketPort.set(bridge.port());
    server.start();
    try (bridge) {
      var connection = new OnlineConnection(URI.create("http://127.0.0.1:" + server.getAddress().getPort()), null, bridge.port());

      try {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (connection.view() == null && "connecting".equals(connection.status()) && System.nanoTime() < deadline)
          Thread.sleep(10);
        holder[0] = connection.view();
        return connection.view() != null ? "connected" : "failed: " + connection.status();
      } finally {
        connection.close();
      }
    } finally {
      server.stop(0);
    }
  }

  @Test
  void aStreamedWorldSizedMapAndItsWindowedSnapshotAreAccepted() throws Exception {
    var holder = new OnlineConnection.View[1];
    String creatures = "\"creatures\":[" + String.format(GUARD_AT, 2950, 1060) + "],";
    String result = run(6144, 3072, ",\"streamed\":true", 2944.5, 1059.5,
        creatures + WINDOW + ",\"truncated\":false", holder);
    assertEquals("connected", result);
    var view = holder[0];
    assertTrue(view.map().streamed());
    assertEquals(6144, view.map().width());
    assertEquals(new OnlineWindow(2896, 1011, 97, 97), view.snapshot().window());
    assertEquals(Boolean.FALSE, view.snapshot().truncated());
    assertEquals(1, view.snapshot().creatures().size());
  }

  @Test
  void aMapThatIsNotStreamedStillCannotExceedItsOldSizeCap() throws Exception {
    var holder = new OnlineConnection.View[1];
    assertNotEquals("connected", run(6144, 3072, "", 2944.5, 1059.5, "\"creatures\":[]", holder));
    assertEquals("connected", run(1024, 1024, "", 40.5, 30.5, "\"creatures\":[]", holder));
    assertThrows(IllegalArgumentException.class, () -> new OnlineRegion(0, "x", 0, 0, 8193, 10, 0));
    assertEquals(8192, new OnlineRegion(0, "x", 0, 0, 8192, 8192, 0).width());
  }

  @Test
  void aStreamedSnapshotMustCarryAWindowAndKeepEveryListInsideIt() throws Exception {
    var holder = new OnlineConnection.View[1];
    assertNotEquals("connected", run(6144, 3072, ",\"streamed\":true", 2944.5, 1059.5, "\"creatures\":[]", holder),
        "A streamed map without a window is a protocol error");
    assertNotEquals("connected", run(6144, 3072, ",\"streamed\":true", 2944.5, 1059.5,
        "\"creatures\":[" + String.format(GUARD_AT, 100, 100) + "]," + WINDOW, holder),
        "A creature outside the announced window is a protocol error");
    assertNotEquals("connected", run(6144, 3072, ",\"streamed\":true", 6140.5, 10.5,
        "\"creatures\":[],\"window\":{\"x\":6100,\"y\":0,\"width\":97,\"height\":97}", holder),
        "A window that leaves the region is a protocol error");
    assertNotEquals("connected", run(80, 60, "", 40.5, 30.5,
        "\"creatures\":[],\"window\":{\"x\":0,\"y\":0,\"width\":80,\"height\":60}", holder),
        "A window on a map that is not streamed is a protocol error");
    assertNotEquals("connected", run(6144, 3072, ",\"streamed\":true", 100.5, 100.5,
        "\"creatures\":[]," + WINDOW, holder), "A player outside the announced window is a protocol error");
  }
}
