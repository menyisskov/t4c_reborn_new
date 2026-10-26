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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OnlineConnectionTest {
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void sendsOnlyDirectionAndPublishesAuthoritativeWorldCoordinatesThenLeaves(boolean persistent)
      throws Exception {
    var input = new AtomicReference<String>();
    var login = new AtomicReference<String>();
    var credentials = new OnlineConnection.Credentials("b".repeat(64), java.util.UUID.randomUUID());
    var leave = new CountDownLatch(1);
    var tapReceived = new CountDownLatch(1);
    var stoppedAfterTap = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    String token = "a".repeat(64);
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          if (path.equals("/join"))
            login.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          if (path.equals("/input")) {
            input.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (input.get().endsWith(" -1 1")) tapReceived.countDown();
            if (tapReceived.getCount() == 0 && input.get().endsWith(" 0 0"))
              stoppedAfterTap.countDown();
          }
          if (path.equals("/leave")) leave.countDown();
          String result =
              switch (path) {
                case "/map" ->
                    "{\"originX\":2904,\"originY\":1029,\"width\":80,\"height\":60,\"persistent\":"
                        + persistent
                        + "}";
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
          new OnlineConnection(
              URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
              persistent ? credentials : null)) {
        connection.direction(1, -1);
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while ((connection.view() == null || input.get() == null || !input.get().endsWith(" 1 -1"))
            && System.nanoTime() < deadline) Thread.sleep(10);
        assertNotNull(connection.view());
        assertEquals(
            persistent ? credentials.secret() + " " + credentials.characterId() : "", login.get());
        assertEquals(7, connection.view().ownId());
        assertEquals(2904, connection.view().map().originX());
        assertEquals(40.5, connection.view().snapshot().players().getFirst().x());
        assertTrue(input.get().matches("[0-9]+ 1 -1"));
        assertThrows(
            UnsupportedOperationException.class,
            () -> connection.view().snapshot().players().clear());
        connection.direction(0, 0);
        connection.tap(-1, 1);
        assertTrue(tapReceived.await(3, TimeUnit.SECONDS), "A brief key tap must reach the server");
        assertTrue(
            stoppedAfterTap.await(3, TimeUnit.SECONDS), "A key tap must not become held movement");
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
