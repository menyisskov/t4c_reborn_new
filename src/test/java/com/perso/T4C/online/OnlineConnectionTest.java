package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OnlineConnectionTest {
  @Test
  void sendsOnlyDirectionAndPublishesAuthoritativeWorldCoordinatesThenLeaves() throws Exception {
    var input = new AtomicReference<String>();
    var leave = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    String token = "a".repeat(64);
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          if (path.equals("/input"))
            input.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          if (path.equals("/leave")) leave.countDown();
          String result =
              switch (path) {
                case "/map" -> "{\"originX\":2904,\"originY\":1029,\"width\":80,\"height\":60}";
                case "/join" -> "{\"token\":\"" + token + "\",\"id\":7}";
                case "/state" ->
                    "{\"tick\":8,\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0}]}";
                default -> "{}";
              };
          byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    server.start();
    try {
      try (var connection =
          new OnlineConnection(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))) {
        connection.direction(1, -1);
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while ((connection.view() == null || input.get() == null || !input.get().endsWith(" 1 -1"))
            && System.nanoTime() < deadline) Thread.sleep(10);
        assertNotNull(connection.view());
        assertEquals(7, connection.view().ownId());
        assertEquals(2904, connection.view().map().originX());
        assertEquals(40.5, connection.view().snapshot().players().getFirst().x());
        assertTrue(input.get().matches("[0-9]+ 1 -1"));
        assertThrows(
            UnsupportedOperationException.class,
            () -> connection.view().snapshot().players().clear());
      }
      assertTrue(leave.await(3, TimeUnit.SECONDS), "Closing must release the temporary session");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void refusesRemoteAndCredentialBearingEndpoints() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OnlineConnection(URI.create("http://example.com:18765")));
    assertThrows(
        IllegalArgumentException.class,
        () -> new OnlineConnection(URI.create("http://secret@127.0.0.1:18765")));
  }
}
