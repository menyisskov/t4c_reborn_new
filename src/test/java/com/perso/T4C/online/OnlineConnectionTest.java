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
    var protocol = new AtomicReference<String>();
    var revision = new java.util.concurrent.atomic.AtomicLong(1);
    var regionEpoch = new java.util.concurrent.atomic.AtomicLong();
    var moveOnCharacter = new java.util.concurrent.atomic.AtomicBoolean();
    var characterReads = new java.util.concurrent.atomic.AtomicInteger();
    var characterAuth = new AtomicReference<String>();
    var equipmentBody = new AtomicReference<String>();
    var equipmentAuth = new AtomicReference<String>();
    var equipmentCode = new java.util.concurrent.atomic.AtomicInteger(200);
    var equipmentCalls = new java.util.concurrent.atomic.AtomicInteger();
    var equipmentReceived = new CountDownLatch(1);
    var releaseEquipment = new CountDownLatch(1);
    var credentials = new OnlineConnection.Credentials("b".repeat(64), java.util.UUID.randomUUID());
    var corpseId=java.util.UUID.randomUUID();
    var leave = new CountDownLatch(1);
    var tapReceived = new CountDownLatch(1);
    var stoppedAfterTap = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    String token = "a".repeat(64);
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          if (path.equals("/equip") || path.equals("/unequip") || path.equals("/attack") || path.equals("/recover")) {
            equipmentCalls.incrementAndGet();
            equipmentBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            equipmentAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            equipmentReceived.countDown();
            try { releaseEquipment.await(3, TimeUnit.SECONDS); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); }
            if (equipmentCode.get() == 200) revision.incrementAndGet();
          }
          if (path.equals("/join")) {
            login.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            protocol.set(exchange.getRequestHeaders().getFirst(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER));
          }
          if (path.equals("/input")) {
            input.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (input.get().endsWith(" -1 1")) tapReceived.countDown();
            if (tapReceived.getCount() == 0 && input.get().endsWith(" 0 0"))
              stoppedAfterTap.countDown();
          }
          if (path.equals("/leave")) leave.countDown();
          if (path.equals("/character")) {
            characterReads.incrementAndGet();
            characterAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            if (moveOnCharacter.compareAndSet(true,false)) regionEpoch.set(1);
          }
          String region="{\"world\":"+regionEpoch.get()+",\"key\":\"region-"+regionEpoch.get()
              +"\",\"locationEpoch\":"+regionEpoch.get()+",\"originX\":"+(regionEpoch.get()==0 ? 2904 : 264)
              +",\"originY\":"+(regionEpoch.get()==0 ? 1029 : 353)+",\"width\":80,\"height\":60}";
          String result =
              switch (path) {
                case "/map" ->
                    "{\"protocolVersion\":" + com.perso.T4C.online.model.OnlineProtocol.VERSION
                        + ",\"persistent\":"+persistent+","+region.substring(1);
                case "/join" -> "{\"token\":\"" + token + "\",\"id\":7,\"region\":"+region+"}";
                case "/state" ->
                    "{\"tick\":8,\"region\":"+region+",\"characterRevision\":" + revision.get()
                        + ",\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0,\"hp\":27,\"maxHp\":40}],"
                        + "\"creatures\":[{\"id\":1,\"definition\":\"Guard\",\"x\":21,\"y\":37,"
                        + "\"health\":55,\"maxHealth\":55,\"walkPattern\":\"Warrio#l\"}]}";
                case "/character" ->
                    "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Online tester\",\"gold\":"
                        + (200 + revision.get()) + ",\"experience\":0,\"revision\":" + revision.get() + ",\"locationEpoch\":"+regionEpoch.get()+"}";
                case "/corpses" -> "[{\"id\":\""+corpseId+"\",\"world\":0,\"x\":2944,\"y\":1059,\"itemCount\":1,\"gold\":0}]";
                case "/equip", "/unequip", "/attack", "/recover" -> "{\"revision\":" + revision.get() + "}";
                default -> "{}";
              };
          byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(path.equals("/equip") || path.equals("/unequip") || path.equals("/attack") || path.equals("/recover") ? equipmentCode.get() : 200, bytes.length);
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
        while (connection.view() == null
            && System.nanoTime() < deadline) Thread.sleep(10);
        assertNotNull(connection.view());
        connection.confirmRegionReady(connection.view().map().locationEpoch());
        while ((input.get()==null || !input.get().endsWith(" 1 -1")) && System.nanoTime()<deadline) Thread.sleep(10);
        assertEquals(
            persistent ? credentials.secret() + " " + credentials.characterId() : "", login.get());
        assertEquals(7, connection.view().ownId());
        assertEquals(27, connection.view().snapshot().players().getFirst().hp());
        assertEquals(40, connection.view().snapshot().players().getFirst().maxHp());
        assertEquals(Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION), protocol.get());
        assertEquals(2904, connection.view().map().originX());
        assertEquals(40.5, connection.view().snapshot().players().getFirst().x());
        assertEquals(21, connection.view().snapshot().creatures().getFirst().x());
        assertEquals("Warrio#l", connection.view().snapshot().creatures().getFirst().walkPattern());
        assertThrows(UnsupportedOperationException.class, () -> connection.view().snapshot().creatures().clear());
        assertTrue(input.get().matches("0 [0-9]+ 1 -1"));
        assertThrows(
            UnsupportedOperationException.class,
            () -> connection.view().snapshot().players().clear());
        connection.direction(0, 0);
        connection.tap(-1, 1);
        assertTrue(tapReceived.await(3, TimeUnit.SECONDS), "A brief key tap must reach the server");
        assertTrue(
            stoppedAfterTap.await(3, TimeUnit.SECONDS), "A key tap must not become held movement");
        assertEquals(persistent ? 1 : 0, characterReads.get(), "Unchanged state is not downloaded every frame");
        if (persistent) {
          assertEquals("Bearer " + token, characterAuth.get());
          assertEquals(201, connection.view().character().gold());
          revision.set(2);
          deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
          while (connection.view().character().revision() < 2 && System.nanoTime() < deadline) Thread.sleep(10);
          assertEquals(202, connection.view().character().gold());
          assertEquals(2, characterReads.get());
          var instance = java.util.UUID.randomUUID();
          var equipped = connection.equip(instance, com.perso.T4C.player.BodyPart.HEAD).toCompletableFuture();
          assertTrue(equipmentReceived.await(2, TimeUnit.SECONDS));
          assertFalse(equipped.isDone(), "An in-flight action cannot publish a predicted success");
          assertThrows(IllegalStateException.class, () -> connection.unequip(com.perso.T4C.player.BodyPart.HEAD));
          assertEquals(2, connection.view().character().revision());
          releaseEquipment.countDown();
          var result = equipped.get(3, TimeUnit.SECONDS);
          assertTrue(result.accepted());
          assertEquals(3, result.character().revision());
          assertEquals(result.character(), connection.view().character(), "Publish committed private state before completion");
          assertEquals("2 0 " + instance + " HEAD", equipmentBody.get());
          assertEquals("Bearer " + token, equipmentAuth.get());

          equipmentCode.set(409);
          var rejected = connection.unequip(com.perso.T4C.player.BodyPart.HEAD).toCompletableFuture().get(3, TimeUnit.SECONDS);
          assertFalse(rejected.accepted());
          assertEquals("3 0 HEAD", equipmentBody.get());
          assertEquals("connected", connection.status(), "A stale or ineligible action is not a broken connection");
          assertEquals(4, characterReads.get(), "Conflicts must refresh private state before another action");

          equipmentCode.set(200);
          var attack=connection.attack(1).toCompletableFuture().get(3,TimeUnit.SECONDS);
          assertTrue(attack.accepted());
          assertEquals("3 0 1",equipmentBody.get(),"Attack intent contains only revision, location epoch and target");
          assertEquals(4,attack.character().revision());
          assertEquals(attack.character(),connection.view().character());
          assertEquals(corpseId,connection.view().corpses().getFirst().id());
          var recovered=connection.recover(corpseId).toCompletableFuture().get(3,TimeUnit.SECONDS);
          assertTrue(recovered.accepted());
          assertEquals("4 0 "+corpseId,equipmentBody.get(),"Recovery accepts only revision, location epoch and owned corpse identity");
          assertEquals(5,recovered.character().revision());

          // Move after the state reply but before its private-state read. Refresh reads only.
          moveOnCharacter.set(true); revision.set(6);
          deadline=System.nanoTime()+Duration.ofSeconds(4).toNanos();
          while ((connection.view()==null || connection.view().map().locationEpoch()!=1) && System.nanoTime()<deadline) Thread.sleep(10);
          assertNotNull(connection.view());
          assertEquals(1,connection.view().map().world());
          assertEquals(connection.view().map().region(),connection.view().snapshot().region());
          assertEquals(1,connection.view().character().locationEpoch());
          assertEquals(4,equipmentCalls.get(),"A map/private-state race must not replay any mutation");
          connection.direction(1,0);
          while ((!input.get().startsWith("1 ") || !input.get().endsWith(" 0 0")) && System.nanoTime()<deadline) Thread.sleep(10);
          assertTrue(input.get().endsWith(" 0 0"),"Movement waits for render-thread map acknowledgement");
          connection.confirmRegionReady(1);
          while (!input.get().endsWith(" 1 0") && System.nanoTime()<deadline) Thread.sleep(10);
          assertTrue(input.get().startsWith("1 "));
          assertTrue(input.get().endsWith(" 1 0"));

          equipmentCode.set(503);
          var uncertain = connection.equip(instance, com.perso.T4C.player.BodyPart.HEAD).toCompletableFuture();
          assertThrows(java.util.concurrent.ExecutionException.class, () -> uncertain.get(3, TimeUnit.SECONDS));
          assertEquals("disconnected", connection.status());
          assertNull(connection.view());
          assertEquals(5, equipmentCalls.get(), "An ambiguous mutation must never be automatically replayed");
        } else assertNull(connection.view().character());
      }
      assertTrue(leave.await(3, TimeUnit.SECONDS), "Closing must release the temporary session");
    } finally {
      releaseEquipment.countDown();
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

  @ParameterizedTest
  @ValueSource(strings = {"", ",\"protocolVersion\":999"})
  void incompatibleServersNeverReceiveAccountCredentials(String versionField) throws Exception {
    var requests = new java.util.concurrent.CopyOnWriteArrayList<String>();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      requests.add(exchange.getRequestURI().getPath());
      byte[] reply = ("{\"width\":80,\"height\":60,\"persistent\":true" + versionField + "}").getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, reply.length);
      exchange.getResponseBody().write(reply);
      exchange.close();
    });
    server.start();
    var credentials = new OnlineConnection.Credentials("b".repeat(64), java.util.UUID.randomUUID());
    try (var connection = new OnlineConnection(URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
      while (!connection.status().equals("disconnected") && System.nanoTime() < deadline) Thread.sleep(10);
      assertEquals("disconnected", connection.status());
      assertNull(connection.view());
      assertEquals(java.util.List.of("/map"), requests, "No login, credentials or gameplay may follow incompatible discovery");
    } finally { server.stop(0); }
  }

  @Test
  void oversizedServerResponseDisconnectsWithoutPublishingState() throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      try {
        byte[] oversized = new byte[2 * 1024 * 1024 + 1];
        exchange.sendResponseHeaders(200, oversized.length);
        exchange.getResponseBody().write(oversized);
      } catch (java.io.IOException expectedDisconnect) {
        // The receiver is expected to cancel as soon as its bound is exceeded.
      } finally { exchange.close(); }
    });
    server.start();
    try (var connection = new OnlineConnection(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))) {
      long deadline = System.nanoTime() + Duration.ofSeconds(4).toNanos();
      while (!connection.status().equals("disconnected") && System.nanoTime() < deadline) Thread.sleep(10);
      assertEquals("disconnected", connection.status());
      assertNull(connection.view());
    } finally { server.stop(0); }
  }

  @Test
  void stalledBodyCannotKeepTheNetworkWorkerConnectedIndefinitely() throws Exception {
    var release = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      try {
        exchange.sendResponseHeaders(200, 100);
        exchange.getResponseBody().write('{');
        exchange.getResponseBody().flush();
        release.await(6, TimeUnit.SECONDS);
      } catch (Exception expectedDisconnect) {
        // Test shutdown can interrupt or close the deliberately unfinished response.
      } finally { exchange.close(); }
    });
    server.start();
    try (var connection = new OnlineConnection(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))) {
      long deadline = System.nanoTime() + Duration.ofSeconds(4).toNanos();
      while (!connection.status().equals("disconnected") && System.nanoTime() < deadline) Thread.sleep(10);
      assertEquals("disconnected", connection.status());
      assertNull(connection.view());
    } finally { release.countDown(); server.stop(0); }
  }
}
