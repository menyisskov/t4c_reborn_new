package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.OnlineProtocol;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** M1: a refused entry into the game must end with a message the player can act on (T4C-0324). */
class OnlineJoinFailureTest {
  private static final String KEY = "c".repeat(64);

  @Test
  void everyServerRefusalMapsToAnActionableEnglishMessage() {
    // The server answers an unknown key, a foreign character, a missing one and one in use identically (409).
    String refused = OnlineJoinFailure.forRefusal(409, "account_or_session");
    assertEquals(I18n.key("online.lobby.error.join_refused"), refused);
    assertTrue(refused.contains("in use") && refused.contains("not on this account"));
    assertEquals(I18n.key("online.lobby.error.incompatible"), OnlineJoinFailure.forRefusal(426, "protocol"));
    assertEquals(I18n.key("online.lobby.error.rejected_auth"), OnlineJoinFailure.forRefusal(401, "account"));
    assertEquals(I18n.key("online.lobby.error.rejected_busy"), OnlineJoinFailure.forRefusal(503, "busy"));
    assertEquals(I18n.message("online.lobby.error.rejected", 418), OnlineJoinFailure.forRefusal(418, null));
    assertEquals(I18n.key("online.lobby.error.incompatible"),
        OnlineJoinFailure.forFailure(new OnlineJoinFailure.Incompatible("x")));
    assertEquals(I18n.key("online.lobby.error.unreachable"),
        OnlineJoinFailure.forFailure(new java.io.IOException("secret-detail")));
  }

  @Test
  void messagesNeverCarryTheKeyOrRawText() {
    for (int status : new int[] {400, 401, 403, 409, 426, 429, 503, 500}) {
      String text = OnlineJoinFailure.forRefusal(status, KEY);
      assertFalse(text.contains(KEY), "status " + status);
    }
  }

  /** Runs one connection against a stub that refuses the join (or reports another protocol) and returns its failure text. */
  private static String failureWith(int protocol, int joinStatus, String joinBody) throws Exception {
    var joins = new AtomicInteger();
    var socketPort = new AtomicInteger();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      exchange.getRequestBody().readAllBytes();
      String path = exchange.getRequestURI().getPath();
      int status = 200;
      String reply = "{}";
      if (path.equals("/map"))
        reply = "{\"protocolVersion\":" + protocol + ",\"gameSocketPort\":" + socketPort.get()
            + ",\"persistent\":true,\"world\":0,\"key\":\"r\",\"locationEpoch\":0,\"originX\":0,\"originY\":0,"
            + "\"width\":80,\"height\":60}";
      else if (path.equals("/join")) {
        joins.incrementAndGet();
        status = joinStatus;
        reply = joinBody;
      }
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    var bridge = new LoopbackGameSocket(server);
    socketPort.set(bridge.port());
    server.start();
    var credentials = new OnlineConnection.Credentials(KEY, UUID.randomUUID());
    try (bridge;
        var connection = new OnlineConnection(
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials, socketPort.get())) {
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
      while (!"disconnected".equals(connection.status()) && System.nanoTime() < deadline) Thread.sleep(20);
      assertEquals("disconnected", connection.status());
      assertEquals(protocol == OnlineProtocol.VERSION ? 1 : 0, joins.get(), "an incompatible server is never joined");
      return connection.failureText();
    } finally {
      bridge.close();
      server.stop(0);
    }
  }

  @Test
  void aRefusedJoinLeavesAMessageBeforeTheStatusFlipsToDisconnected() throws Exception {
    assertEquals(I18n.key("online.lobby.error.join_refused"),
        failureWith(OnlineProtocol.VERSION, 409, "{\"error\":\"account_or_session\"}"));
    assertEquals(I18n.key("online.lobby.error.incompatible"),
        failureWith(OnlineProtocol.VERSION, 426, "{\"error\":\"protocol\"}"));
  }

  @Test
  void anIncompatibleServerVersionIsNamedAndNeverJoined() throws Exception {
    assertEquals(I18n.key("online.lobby.error.incompatible"),
        failureWith(OnlineProtocol.VERSION + 1, 200, "{}"));
  }
}
