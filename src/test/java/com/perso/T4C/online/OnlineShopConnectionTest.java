package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

/** The shop commands of the native adapter against a local fake server: bodies, replies, refusals, no replay. */
class OnlineShopConnectionTest {
  private static final String TOKEN = "a".repeat(64);
  private static final String REGION = "{\"world\":0,\"key\":\"start\",\"locationEpoch\":0,\"originX\":2904,"
      + "\"originY\":1029,\"width\":80,\"height\":60}";

  private record Call(String path, String body, String protocol) {}

  private static void awaitTrue(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
    assertTrue(condition.getAsBoolean());
  }

  private static HttpServer server(OnlineConnection.Credentials credentials, AtomicLong revision,
      CopyOnWriteArrayList<Call> calls, AtomicReference<String> shopReply, AtomicInteger shopStatus) throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      int status = 200;
      String reply = "{}";
      switch (path) {
        case "/map" -> reply = "{\"protocolVersion\":" + com.perso.T4C.online.model.OnlineProtocol.VERSION
            + ",\"persistent\":true," + REGION.substring(1);
        case "/join" -> reply = "{\"token\":\"" + TOKEN + "\",\"id\":7,\"region\":" + REGION + "}";
        case "/state" -> reply = "{\"tick\":8,\"region\":" + REGION + ",\"characterRevision\":" + revision.get()
            + ",\"players\":[{\"id\":7,\"x\":40.5,\"y\":30.5,\"sequence\":0,\"hp\":27,\"maxHp\":40}]}";
        case "/character" -> reply = "{\"id\":\"" + credentials.characterId() + "\",\"name\":\"Shopper\",\"gold\":500,"
            + "\"experience\":0,\"revision\":" + revision.get() + ",\"locationEpoch\":0}";
        case "/corpses" -> reply = "[]";
        case "/shop-offers", "/shop-buy", "/shop-sell" -> {
          calls.add(new Call(path, request, exchange.getRequestHeaders().getFirst(
              com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER)));
          status = shopStatus.get();
          reply = shopReply.get();
          if (status == 200 && !path.equals("/shop-offers")) revision.incrementAndGet();
        }
        default -> { }
      }
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    server.start();
    return server;
  }

  @Test
  void listingBuyAndSellSendIdentityOnlyAndPublishTheServersNumbers() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var revision = new AtomicLong(1);
    var calls = new CopyOnWriteArrayList<Call>();
    var reply = new AtomicReference<String>();
    var status = new AtomicInteger(200);
    var server = server(credentials, revision, calls, reply, status);
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);

      reply.set("{\"revision\":1,\"gold\":500,\"npc\":\"Sigfried\",\"buy\":[{\"key\":\"item.rusted_short_sword\","
          + "\"price\":9,\"quantity\":0}],\"sell\":[{\"key\":\"item.torch\",\"price\":2,\"quantity\":3}]}");
      var listing = connection.shopOffers("Sigfried").toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertTrue(listing.accepted());
      assertNull(listing.messageKey());
      assertEquals("1 0 Sigfried", calls.getLast().body(), "A listing names only revision, epoch and the NPC");
      assertEquals(String.valueOf(com.perso.T4C.online.model.OnlineProtocol.VERSION), calls.getLast().protocol());
      assertEquals(9, listing.shop().buy().getFirst().price());
      assertEquals(3, listing.shop().sell().getFirst().quantity());
      assertEquals(1, listing.character().revision(), "A listing changes nothing");

      reply.set("{\"revision\":2,\"quantity\":2,\"total\":18}");
      var bought = connection.shopBuy("Sigfried", "item.rusted_short_sword", 2).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertTrue(bought.accepted());
      assertEquals("1 0 Sigfried item.rusted_short_sword 2", calls.getLast().body(),
          "A trade names no price: revision, epoch, NPC, item and count");
      assertEquals("online.shop.bought", bought.messageKey());
      assertEquals(List.of(2, 18), bought.args());
      assertEquals(2, bought.character().revision());

      reply.set("{\"revision\":3,\"quantity\":1,\"total\":4}");
      var sold = connection.shopSell("Sigfried", "item.rusted_short_sword", 1).toCompletableFuture().get(3, TimeUnit.SECONDS);
      assertEquals("2 0 Sigfried item.rusted_short_sword 1", calls.getLast().body());
      assertEquals("online.shop.sold", sold.messageKey());
      assertEquals(List.of(1, 4), sold.args());
      assertEquals(3, sold.character().revision());
    } finally { server.stop(0); }
  }

  @Test
  void definiteRefusalsBecomeReadableMessagesAndKeepTheConnection() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var calls = new CopyOnWriteArrayList<Call>();
    var reply = new AtomicReference<String>();
    var status = new AtomicInteger(409);
    var server = server(credentials, new AtomicLong(1), calls, reply, status);
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      for (var refusal : List.of("insufficient_gold:online.shop.not_enough_gold", "too_heavy:online.shop.too_heavy",
          "item_unavailable:online.shop.not_enough_items", "not_a_shop:online.shop.not_a_shop",
          "npc_unavailable:online.shop.unavailable", "revision:online.shop.busy", "brand_new:online.shop.refused")) {
        String[] parts = refusal.split(":");
        reply.set("{\"error\":\"" + parts[0] + "\"}");
        var refused = connection.shopBuy("Sigfried", "item.torch", 1).toCompletableFuture().get(3, TimeUnit.SECONDS);
        assertFalse(refused.accepted());
        assertEquals(parts[1], refused.messageKey());
        assertTrue(com.perso.T4C.i18n.I18n.has(parts[1]), parts[1]);
        assertEquals("connected", connection.status());
      }
      for (var key : List.of("online.shop.bought", "online.shop.sold", "online.talk.shop", "online.shop.title_buy",
          "online.shop.title_sell", "online.shop.gold", "online.shop.buy_row", "online.shop.sell_row",
          "online.shop.empty_buy", "online.shop.empty_sell", "online.shop.page", "online.shop.quantity",
          "online.shop.hint_buy", "online.shop.hint_sell", "online.shop.not_for_sale", "online.shop.not_sellable",
          "online.shop.unsupported_item", "online.shop.dead"))
        assertTrue(com.perso.T4C.i18n.I18n.has(key), key);
    } finally { server.stop(0); }
  }

  @Test
  void anAmbiguousTradeDisconnectsAndIsNeverReplayed() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var calls = new CopyOnWriteArrayList<Call>();
    var status = new AtomicInteger(503);
    var server = server(credentials, new AtomicLong(1), calls, new AtomicReference<>("{\"error\":\"busy\"}"), status);
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      var uncertain = connection.shopSell("Sigfried", "item.torch", 1).toCompletableFuture();
      assertThrows(java.util.concurrent.ExecutionException.class, () -> uncertain.get(4, TimeUnit.SECONDS));
      awaitTrue(() -> "disconnected".equals(connection.status()));
      assertEquals(1, calls.size(), "An ambiguous trade must never be automatically replayed");
    } finally { server.stop(0); }
  }

  @Test
  void argumentsAreValidatedBeforeAnythingIsSent() throws Exception {
    var credentials = new OnlineConnection.Credentials("b".repeat(64), UUID.randomUUID());
    var calls = new CopyOnWriteArrayList<Call>();
    var server = server(credentials, new AtomicLong(1), calls, new AtomicReference<>("{}"), new AtomicInteger(200));
    try (var connection = new OnlineConnection(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort()), credentials)) {
      awaitTrue(() -> connection.view() != null && connection.view().character() != null);
      assertThrows(IllegalArgumentException.class, () -> connection.shopOffers("bad npc"));
      assertThrows(IllegalArgumentException.class, () -> connection.shopBuy("Sigfried", "rusted_short_sword", 1));
      assertThrows(IllegalArgumentException.class, () -> connection.shopBuy("Sigfried", "item.torch", 0));
      assertThrows(IllegalArgumentException.class, () -> connection.shopSell("Sigfried", "item.torch", 100));
      assertTrue(calls.isEmpty());
    } finally { server.stop(0); }
  }
}
