package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.GroundLootView;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
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
    var interactionBody=new AtomicReference<String>();
    var letterGiven=new java.util.concurrent.atomic.AtomicBoolean();
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
          boolean newLetter=false;
          if(path.equals("/interact")) {
            interactionBody.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            newLetter=letterGiven.compareAndSet(false,true);
            if(newLetter) revision.incrementAndGet();
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
                        + "\"npcs\":[{\"id\":\"Kilhiam\",\"displayName\":\"${npc.kilhiam}\",\"x\":57,\"y\":29,\"parts\":{\"BODY\":\"WoWhiteRobe\"}}],"
                        + "\"creatures\":[{\"id\":1,\"definition\":\"Guard\",\"x\":21,\"y\":37,"
                        + "\"health\":55,\"maxHealth\":55,\"walkPattern\":\"Warrio#l\"}]}";
                case "/character" ->
                    "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Online tester\",\"gold\":"
                        + (200 + revision.get()) + ",\"experience\":0,\"revision\":" + revision.get() + ",\"locationEpoch\":"+regionEpoch.get()+"}";
                case "/corpses" -> "[{\"id\":\""+corpseId+"\",\"world\":0,\"x\":2944,\"y\":1059,\"itemCount\":1,\"gold\":0}]";
                case "/equip", "/unequip", "/attack", "/recover" -> "{\"revision\":" + revision.get() + "}";
                case "/interact" -> "{\"revision\":"+revision.get()+",\"messageKey\":\"npc.kilhiam."+(newLetter ? "letter" : "temple")+"\",\"changed\":"+newLetter+"}";
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
        assertEquals("Kilhiam",connection.view().snapshot().npcs().getFirst().id());
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

          var letter=connection.interact("Kilhiam").toCompletableFuture().get(3,TimeUnit.SECONDS);
          assertTrue(letter.accepted());assertEquals("npc.kilhiam.letter",letter.messageKey());
          assertEquals("5 0 Kilhiam",interactionBody.get(),"Dialogue sends only revision, epoch and authored identity");
          assertEquals(6,letter.character().revision());
          var greeting=connection.interact("Kilhiam").toCompletableFuture().get(3,TimeUnit.SECONDS);
          assertTrue(greeting.accepted());assertEquals("npc.kilhiam.temple",greeting.messageKey());
          assertEquals(6,greeting.character().revision(),"A read-only greeting may acknowledge the same revision");
          assertEquals(greeting.character(),connection.view().character());

          // Move after the state reply but before its private-state read. Refresh reads only.
          moveOnCharacter.set(true); revision.set(7);
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

  private static final String LOOT_TOKEN = "a".repeat(64);

  private static String regionJson(long epoch) {
    return "{\"world\":" + epoch + ",\"key\":\"region-" + epoch + "\",\"locationEpoch\":" + epoch
        + ",\"originX\":" + (epoch == 0 ? 2904 : 264) + ",\"originY\":" + (epoch == 0 ? 1029 : 353)
        + ",\"width\":80,\"height\":60}";
  }

  private static void awaitTrue(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
    assertTrue(condition.getAsBoolean());
  }

  @Test
  void groundLootIsStagedByLocationAndPickupRefusalsStaySingleFlight() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var revision = new AtomicLong(1);
    var regionEpoch = new AtomicLong();
    var claimed = new AtomicBoolean();
    var holdPickup = new AtomicBoolean();
    var pickupAccepted = new AtomicBoolean();
    var releasePickup = new CountDownLatch(1);
    var pickupCalls = new AtomicInteger();
    var pickupBodies = new CopyOnWriteArrayList<String>();
    var lootA = UUID.randomUUID();
    var lootB = UUID.randomUUID();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      String loot = regionEpoch.get() == 0
          ? (claimed.get() ? "[]"
              : "[{\"id\":\"" + lootA + "\",\"x\":21,\"y\":37,\"key\":\"item.leather_gloves\","
                  + "\"ownerPlayerId\":null,\"ownerRemainingMs\":4000}]")
          : "[{\"id\":\"" + lootB + "\",\"x\":3,\"y\":4,\"key\":\"item.leather_gloves\","
              + "\"ownerPlayerId\":7,\"ownerRemainingMs\":0}]";
      int status = 200;
      String reply = "{}";
      switch (path) {
        case "/map" -> reply = "{\"protocolVersion\":" + com.perso.T4C.online.model.OnlineProtocol.VERSION
            + ",\"persistent\":true," + regionJson(regionEpoch.get()).substring(1);
        case "/join" -> reply = "{\"token\":\"" + LOOT_TOKEN + "\",\"id\":7,\"region\":" + regionJson(0) + "}";
        case "/state" -> reply = "{\"tick\":8,\"region\":" + regionJson(regionEpoch.get())
            + ",\"characterRevision\":" + revision.get()
            + ",\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0,\"hp\":27,\"maxHp\":40}],\"loot\":" + loot + "}";
        case "/character" -> reply = "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Loot tester\",\"gold\":0,"
            + "\"experience\":0,\"revision\":" + revision.get() + ",\"locationEpoch\":" + regionEpoch.get() + "}";
        case "/corpses" -> reply = "[]";
        case "/pickup" -> {
          pickupCalls.incrementAndGet();
          pickupBodies.add(request);
          try { if (holdPickup.get()) releasePickup.await(3, TimeUnit.SECONDS); }
          catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
          if (pickupAccepted.get()) {
            claimed.set(true);
            reply = "{\"revision\":" + revision.incrementAndGet() + "}";
          } else {
            status = 409;
            reply = "{\"error\":\"loot_reserved\"}";
          }
        }
        default -> { }
      }
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    server.start();
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().snapshot().loot().size() == 1);
      var seen = connection.view().snapshot().loot().getFirst();
      assertEquals(lootA, seen.id());
      assertEquals("item.leather_gloves", seen.key());
      assertEquals(21.0, seen.x());
      assertEquals(37.0, seen.y(), "Loot uses region-local tiles like creatures");
      assertNull(seen.ownerPlayerId());
      assertEquals(4000, seen.ownerRemainingMs());
      assertEquals(0, connection.view().map().locationEpoch());

      var refused = connection.pickUp(lootA).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertFalse(refused.accepted());
      assertEquals("online.loot.reserved", refused.messageKey());
      assertEquals("connected", connection.status(), "A refused pickup is a normal conflict, not a broken connection");
      assertEquals("1 0 " + lootA, pickupBodies.getFirst(), "Pickup sends only revision, epoch and loot identity");

      holdPickup.set(true);
      pickupAccepted.set(true);
      var inFlight = connection.pickUp(lootA).toCompletableFuture();
      awaitTrue(() -> pickupCalls.get() == 2);
      assertThrows(IllegalStateException.class, () -> connection.pickUp(lootA), "Only one pickup is in flight");
      releasePickup.countDown();
      var picked = inFlight.get(3, TimeUnit.SECONDS);
      assertTrue(picked.accepted());
      assertNull(picked.messageKey());
      assertEquals(2, picked.character().revision());
      awaitTrue(() -> connection.view().snapshot().loot().isEmpty());

      holdPickup.set(false);
      pickupAccepted.set(false);
      regionEpoch.set(1);
      awaitTrue(() -> connection.view() != null && connection.view().map().locationEpoch() == 1
          && !connection.view().snapshot().loot().isEmpty());
      assertEquals(List.of(lootB), connection.view().snapshot().loot().stream().map(GroundLootView::id).toList(),
          "Drops from the old location never survive a region change");
      long currentRevision = connection.view().character().revision();
      var moved = connection.pickUp(lootB).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertFalse(moved.accepted());
      assertEquals("online.loot.reserved", moved.messageKey());
      assertEquals(currentRevision + " 1 " + lootB, pickupBodies.getLast(),
          "Pickup intent names the location epoch of the drop it was staged in");
    } finally {
      releasePickup.countDown();
      server.stop(0);
    }
  }

  @Test
  void ambiguousPickupDisconnectsWithoutReplay() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var lootA = UUID.randomUUID();
    var pickupCalls = new AtomicInteger();
    var release = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      exchange.getRequestBody().readAllBytes();
      if (path.equals("/pickup")) {
        pickupCalls.incrementAndGet();
        // Hold past the client's bounded request budget so the outcome is unknown to the client.
        try { release.await(5, TimeUnit.SECONDS); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        exchange.close();
        return;
      }
      String reply = switch (path) {
        case "/map" -> "{\"protocolVersion\":" + com.perso.T4C.online.model.OnlineProtocol.VERSION
            + ",\"persistent\":true," + regionJson(0).substring(1);
        case "/join" -> "{\"token\":\"" + LOOT_TOKEN + "\",\"id\":7,\"region\":" + regionJson(0) + "}";
        case "/state" -> "{\"tick\":8,\"region\":" + regionJson(0) + ",\"characterRevision\":1,"
            + "\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0,\"hp\":27,\"maxHp\":40}],"
            + "\"loot\":[{\"id\":\"" + lootA + "\",\"x\":21,\"y\":37,\"key\":\"item.leather_gloves\","
            + "\"ownerPlayerId\":null,\"ownerRemainingMs\":0}]}";
        case "/character" -> "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Loot tester\",\"gold\":0,"
            + "\"experience\":0,\"revision\":1,\"locationEpoch\":0}";
        case "/corpses" -> "[]";
        default -> "{}";
      };
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    server.start();
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && !connection.view().snapshot().loot().isEmpty());
      var uncertain = connection.pickUp(lootA).toCompletableFuture();
      assertThrows(java.util.concurrent.ExecutionException.class, () -> uncertain.get(6, TimeUnit.SECONDS));
      awaitTrue(() -> "disconnected".equals(connection.status()));
      assertNull(connection.view());
      assertEquals(1, pickupCalls.get(), "An ambiguous pickup must never be automatically replayed");
      assertThrows(IllegalStateException.class, () -> connection.pickUp(lootA));
    } finally {
      release.countDown();
      server.stop(0);
    }
  }

  @Test
  void castAndUseSendCommittedRevisionAndEpochMapRefusalsAndAcceptTheCommitReply() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var revision = new AtomicLong(1);
    var bodies = new CopyOnWriteArrayList<String>();
    var potion = UUID.randomUUID();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      int status = 200;
      String reply = "{}";
      switch (path) {
        case "/map" -> reply = "{\"protocolVersion\":" + com.perso.T4C.online.model.OnlineProtocol.VERSION
            + ",\"persistent\":true," + regionJson(0).substring(1);
        case "/join" -> reply = "{\"token\":\"" + LOOT_TOKEN + "\",\"id\":7,\"region\":" + regionJson(0) + "}";
        case "/state" -> reply = "{\"tick\":8,\"region\":" + regionJson(0) + ",\"characterRevision\":" + revision.get()
            + ",\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0,\"hp\":27,\"maxHp\":40}],\"loot\":[]}";
        case "/character" -> reply = "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Caster\",\"gold\":0,"
            + "\"experience\":0,\"revision\":" + revision.get() + ",\"locationEpoch\":0}";
        case "/corpses" -> reply = "[]";
        case "/cast" -> {
          bodies.add("cast " + request);
          status = 409;
          reply = "{\"error\":\"not_enough_mana\"}";
        }
        case "/use" -> {
          bodies.add("use " + request);
          reply = "{\"revision\":" + revision.incrementAndGet()
              + ",\"healthGained\":9,\"manaSpent\":0,\"manaGained\":0}";
        }
        default -> { }
      }
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    server.start();
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      assertThrows(IllegalArgumentException.class, () -> connection.cast("heal_light"));
      assertThrows(IllegalArgumentException.class, () -> connection.cast("spell.Bad Key"));

      var refused = connection.cast("spell.heal_light").toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertFalse(refused.accepted());
      assertEquals("online.ability.not_enough_mana", refused.messageKey());
      assertEquals("connected", connection.status(), "A refused cast is a normal conflict");
      assertEquals("cast 1 0 spell.heal_light", bodies.getFirst());

      var used = connection.useItem(potion).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertTrue(used.accepted());
      assertNull(used.messageKey());
      assertEquals(2, used.character().revision());
      assertEquals("use 1 0 " + potion, bodies.getLast());
      assertEquals(2, bodies.size());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void ambiguousCastDisconnectsWithoutReplay() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var castCalls = new AtomicInteger();
    var release = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      exchange.getRequestBody().readAllBytes();
      if (path.equals("/cast")) {
        castCalls.incrementAndGet();
        try { release.await(5, TimeUnit.SECONDS); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        exchange.close();
        return;
      }
      String reply = switch (path) {
        case "/map" -> "{\"protocolVersion\":" + com.perso.T4C.online.model.OnlineProtocol.VERSION
            + ",\"persistent\":true," + regionJson(0).substring(1);
        case "/join" -> "{\"token\":\"" + LOOT_TOKEN + "\",\"id\":7,\"region\":" + regionJson(0) + "}";
        case "/state" -> "{\"tick\":8,\"region\":" + regionJson(0) + ",\"characterRevision\":1,"
            + "\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0,\"hp\":27,\"maxHp\":40}],\"loot\":[]}";
        case "/character" -> "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Caster\",\"gold\":0,"
            + "\"experience\":0,\"revision\":1,\"locationEpoch\":0}";
        case "/corpses" -> "[]";
        default -> "{}";
      };
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    server.start();
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      var uncertain = connection.cast("spell.heal_light").toCompletableFuture();
      assertThrows(java.util.concurrent.ExecutionException.class, () -> uncertain.get(6, TimeUnit.SECONDS));
      awaitTrue(() -> "disconnected".equals(connection.status()));
      assertEquals(1, castCalls.get(), "An ambiguous cast must never be automatically replayed");
      assertThrows(IllegalStateException.class, () -> connection.cast("spell.heal_light"));
    } finally {
      release.countDown();
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
