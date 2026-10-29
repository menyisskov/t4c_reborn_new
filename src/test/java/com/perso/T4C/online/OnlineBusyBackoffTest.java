package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OnlineBusyBackoffTest {
  @Test
  void ceilingsDoubleThenStayAtTheCap() {
    long[] expected = {250, 500, 1000, 2000, 3000, 3000, 3000};
    for (int retry = 0; retry < expected.length; retry++)
      assertEquals(expected[retry], OnlineBusyBackoff.ceilingMillis(retry));
    assertEquals(3000, OnlineBusyBackoff.ceilingMillis(1000));
    assertThrows(IllegalArgumentException.class, () -> OnlineBusyBackoff.ceilingMillis(-1));
  }

  @Test
  void jitterStaysInTheUpperHalfOfEveryCeiling() {
    for (int retry = 0; retry <= OnlineBusyBackoff.MAX_RETRIES + 2; retry++) {
      long ceiling = OnlineBusyBackoff.ceilingMillis(retry);
      assertEquals(ceiling / 2, OnlineBusyBackoff.delayMillis(retry, 0));
      long top = OnlineBusyBackoff.delayMillis(retry, Math.nextDown(1.0));
      assertTrue(top >= ceiling / 2 && top <= ceiling);
    }
    assertThrows(IllegalArgumentException.class, () -> OnlineBusyBackoff.delayMillis(0, 1.0));
    assertThrows(IllegalArgumentException.class, () -> OnlineBusyBackoff.delayMillis(0, Double.NaN));
  }

  @Test
  void totalWorstCaseWaitIsBounded() {
    long total = 0;
    for (int retry = 0; retry < OnlineBusyBackoff.MAX_RETRIES; retry++)
      total += OnlineBusyBackoff.delayMillis(retry, Math.nextDown(1.0));
    assertTrue(total <= 10_000, "worst case " + total);
  }

  @Test
  void onlyDefiniteBusyIsRetried() {
    assertTrue(OnlineBusyBackoff.isBusy(503, "busy"));
    assertFalse(OnlineBusyBackoff.isBusy(503, "account_busy"));
    assertFalse(OnlineBusyBackoff.isBusy(503, null));
    assertFalse(OnlineBusyBackoff.isBusy(409, "busy"));
    assertFalse(OnlineBusyBackoff.isBusy(500, "busy"));
  }

  /** A busy map read is retried once, and the next definite failure ends the attempt without further tries. */
  @Test
  void busyMapReadIsRetriedAndOtherFailuresAreNot() throws Exception {
    var maps = new AtomicInteger();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    var socketPort = new AtomicInteger();
    server.createContext("/", exchange -> {
      if (exchange.getRequestURI().getPath().equals("/map")) {
        int call = maps.incrementAndGet();
        byte[] body = (call == 1 ? "{\"error\":\"busy\"}" : "{\"error\":\"other\"}").getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(call == 1 ? 503 : 500, body.length);
        exchange.getResponseBody().write(body);
      } else exchange.sendResponseHeaders(404, -1);
      exchange.close();
    });
    var socketBridge = new LoopbackGameSocket(server);
    socketPort.set(socketBridge.port());
    server.start();
    try (socketBridge; var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), null, socketPort.get())) {
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (maps.get() < 2 && System.nanoTime() < deadline) Thread.sleep(20);
      Thread.sleep(400);
      assertEquals(2, maps.get());
      assertEquals("disconnected", connection.status());
    } finally {
      socketBridge.close();
      server.stop(0);
    }
  }
}
