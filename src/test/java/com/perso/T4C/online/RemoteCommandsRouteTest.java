package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.OnlineConnectionLink;
import com.perso.T4C.session.RemotePlayerCommands;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

/**
 * The online commands over a real {@link OnlineConnection} against a local fake server (T4C-0290):
 * which route and body each intent produces, how the prediction frame travels with {@code /input}
 * and comes back in the snapshot's acknowledged sequence, and how refusals read.
 */
class RemoteCommandsRouteTest {
  private static final java.util.Map<HttpServer, LoopbackGameSocket> SOCKETS =
      new ConcurrentHashMap<>();
  private static final java.util.Map<HttpServer, Integer> SOCKET_PORTS = new ConcurrentHashMap<>();
  private static final String TOKEN = "a".repeat(64);
  private static final String REGION =
      "{\"world\":0,\"key\":\"start\",\"locationEpoch\":0,\"originX\":2904,"
          + "\"originY\":1029,\"width\":80,\"height\":60}";

  private record Call(String path, String body) {}

  private static void awaitTrue(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
    assertTrue(condition.getAsBoolean());
  }

  private static HttpServer server(
      OnlineConnection.Credentials credentials,
      AtomicLong revision,
      CopyOnWriteArrayList<Call> calls,
      AtomicLong lastInputSequence,
      AtomicReference<String> reply,
      AtomicInteger status)
      throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    var socketPort = new AtomicInteger();
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          String request =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          int code = 200;
          String body = "{}";
          switch (path) {
            case "/map" ->
                body =
                    "{\"protocolVersion\":"
                        + com.perso.T4C.online.model.OnlineProtocol.VERSION
                        + ",\"gameSocketPort\":"
                        + socketPort.get()
                        + ",\"persistent\":true,"
                        + REGION.substring(1);
            case "/join" -> body = "{\"token\":\"" + TOKEN + "\",\"id\":7,\"region\":" + REGION + "}";
            case "/state" ->
                body =
                    "{\"tick\":8,\"region\":"
                        + REGION
                        + ",\"characterRevision\":"
                        + revision.get()
                        + ",\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":"
                        + lastInputSequence.get()
                        + ",\"hp\":27,\"maxHp\":40}]}";
            case "/character" ->
                body =
                    "{\"id\":\""
                        + credentials.characterId()
                        + "\",\"name\":\"Fighter\",\"gold\":5,\"experience\":0,\"revision\":"
                        + revision.get()
                        + ",\"locationEpoch\":0}";
            case "/corpses" -> body = "[]";
            case "/input" -> {
              calls.add(new Call(path, request));
              String[] parts = request.split(" ");
              lastInputSequence.set(Long.parseLong(parts[1]));
            }
            case "/attack", "/pickup", "/interact" -> {
              calls.add(new Call(path, request));
              code = status.get();
              body = reply.get();
              if (code == 200) revision.incrementAndGet();
            }
            default -> {}
          }
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(code, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    var bridge = new LoopbackGameSocket(server);
    socketPort.set(bridge.port());
    server.start();
    SOCKETS.put(server, bridge);
    SOCKET_PORTS.put(server, bridge.port());
    return server;
  }

  private static void stop(HttpServer server) {
    var bridge = SOCKETS.remove(server);
    SOCKET_PORTS.remove(server);
    if (bridge != null) bridge.close();
    server.stop(0);
  }

  private static List<Call> inputs(List<Call> calls) {
    List<Call> out = new ArrayList<>();
    for (Call call : calls) if (call.path().equals("/input")) out.add(call);
    return out;
  }

  @Test
  void everyIntentIsOneRouteWithIdentityOnlyAndRefusalsReadAsMessages() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var revision = new AtomicLong(1);
    var calls = new CopyOnWriteArrayList<Call>();
    var reply = new AtomicReference<String>("{\"revision\":2}");
    var status = new AtomicInteger(200);
    var server = server(credentials, revision, calls, new AtomicLong(-1), reply, status);
    try (var connection =
        new OnlineConnection(
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            credentials,
            SOCKET_PORTS.get(server))) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      var link = new OnlineConnectionLink(connection);

      var attacked = link.attack(41).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertTrue(attacked.accepted());
      assertEquals("/attack", otherCalls(calls).getLast().path());
      assertEquals("1 0 41", otherCalls(calls).getLast().body(), "revision, epoch, creature id");

      UUID loot = UUID.randomUUID();
      reply.set("{\"revision\":3}");
      link.pickUp(loot).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertEquals("/pickup", otherCalls(calls).getLast().path());
      assertEquals("2 0 " + loot, otherCalls(calls).getLast().body());

      reply.set("{\"revision\":3,\"messageKey\":\"npc.sigfried.hello\",\"changed\":false}");
      var talked = link.interact("Sigfried").toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertEquals("/interact", otherCalls(calls).getLast().path());
      assertEquals("3 0 Sigfried", otherCalls(calls).getLast().body());
      assertEquals("npc.sigfried.hello", talked.messageKey());

      // Refusals: the shared mapping, one key per server reason.
      status.set(409);
      reply.set("{\"error\":\"out_of_range_or_blocked\"}");
      var refused = link.attack(41).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertEquals("online.attack.out_of_reach", refused.messageKey());
      assertTrue(I18n.has(refused.messageKey()));
      reply.set("{\"error\":\"cooldown\"}");
      var cooldown = link.attack(41).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertEquals(RemotePlayerCommands.SILENT_COOLDOWN_KEY, cooldown.messageKey());
      assertEquals(
          OnlineCommandRefusals.ATTACK_COOLDOWN,
          RemotePlayerCommands.SILENT_COOLDOWN_KEY,
          "the silent key is the one the refusal table maps routine cooldowns to");
      reply.set("{\"error\":\"loot_reserved\"}");
      var reserved = link.pickUp(loot).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertEquals("online.loot.reserved", reserved.messageKey());
      assertEquals("connected", connection.status());
    } finally {
      stop(server);
    }
  }

  private static List<Call> otherCalls(List<Call> calls) {
    List<Call> out = new ArrayList<>();
    for (Call call : calls) if (!call.path().equals("/input")) out.add(call);
    return out;
  }

  @Test
  void aRefusalReachesTheHostThroughTheCommands() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var calls = new CopyOnWriteArrayList<Call>();
    var reply = new AtomicReference<String>("{\"error\":\"loot_unavailable\"}");
    var server =
        server(credentials, new AtomicLong(1), calls, new AtomicLong(-1), reply, new AtomicInteger(409));
    try (var connection =
        new OnlineConnection(
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            credentials,
            SOCKET_PORTS.get(server))) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      var shown = new CopyOnWriteArrayList<String>();
      var commands = new RemotePlayerCommands(new OnlineConnectionLink(connection), monster -> -1);
      commands.bind(
          new RemotePlayerCommands.Host() {
            @Override
            public Player player() {
              return null;
            }

            @Override
            public void showMessage(String text) {
              shown.add(text);
            }
          });
      assertTrue(commands.pickUp(UUID.randomUUID()));
      awaitTrue(() -> otherCalls(calls).size() == 1);
      Thread.sleep(100);
      commands.pump();
      assertEquals(List.of(I18n.key("online.loot.unavailable")), shown);
    } finally {
      stop(server);
    }
  }

  @Test
  void theHeldDirectionTravelsWithItsFrameAndTheAcknowledgedSequenceMapsBack() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var calls = new CopyOnWriteArrayList<Call>();
    var server =
        server(
            credentials,
            new AtomicLong(1),
            calls,
            new AtomicLong(-1),
            new AtomicReference<>("{}"),
            new AtomicInteger(200));
    try (var connection =
        new OnlineConnection(
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            credentials,
            SOCKET_PORTS.get(server))) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      assertEquals(-1, connection.acknowledgedFrame(0), "nothing tagged yet");

      // Input is held back until the renderer confirms the region (the screen does that at load).
      connection.confirmRegionReady(connection.view().map().locationEpoch());
      connection.direction(1, 0, 77);
      awaitTrue(
          () -> inputs(calls).stream().anyMatch(c -> c.body().endsWith(" 1 0")));
      var firstWithDirection =
          inputs(calls).stream().filter(c -> c.body().endsWith(" 1 0")).findFirst().get();
      long sequence = Long.parseLong(firstWithDirection.body().split(" ")[1]);
      assertEquals(77, connection.acknowledgedFrame(sequence), "the input that carried frame 77");

      connection.direction(0, 0, 90);
      awaitTrue(
          () -> {
            long newest = Long.parseLong(inputs(calls).getLast().body().split(" ")[1]);
            return newest > sequence && connection.acknowledgedFrame(newest) == 90;
          });
      assertEquals(77, connection.acknowledgedFrame(sequence), "the older input keeps its frame");

      // The snapshot's own actor carries the last applied sequence the prediction acknowledges.
      awaitTrue(
          () ->
              connection.view().snapshot().players().getFirst().sequence() >= sequence
                  && connection.acknowledgedFrame(
                          connection.view().snapshot().players().getFirst().sequence())
                      >= 77);
    } finally {
      stop(server);
    }
  }
}
