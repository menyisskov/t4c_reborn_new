package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.perso.T4C.server.persistence.OnlineStore;

class PreviewServerTest {
  private HttpRequest request(String url, String path, String token, String body) {
    var builder = HttpRequest.newBuilder(URI.create(url + path)).timeout(Duration.ofSeconds(5));
    if (path.equals("/join")) builder.header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER,
        Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION));
    if (token != null) builder.header("Authorization", "Bearer " + token);
    if (body != null) builder.header("Origin", url).header("Content-Type", "text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(body));
    return builder.build();
  }
  private JsonObject object(HttpResponse<String> response) { return JsonParser.parseString(response.body()).getAsJsonObject(); }

  @Test void operatorMetricsRequirePrivateLocalControlAndReportBoundedUnits() throws Exception {
    var map = new PreviewMap(new boolean[8][8],0,0,3,3);
    String admin = "a".repeat(64);
    try(var server = new PreviewServer(map,0,null,admin); var client = HttpClient.newHttpClient()) {
      server.start(); String url = server.url();
      assertEquals(401,client.send(request(url,"/admin/metrics",null,null),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(403,client.send(request(url,"/admin/metrics","b".repeat(64),null),HttpResponse.BodyHandlers.ofString()).statusCode());
      var response=client.send(request(url,"/admin/metrics",admin,null),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,response.statusCode());
      var metrics=object(response);
      assertTrue(metrics.get("processUptimeMillis").getAsLong()>=0);
      assertTrue(metrics.get("heapUsedBytes").getAsLong()>=0);
      assertTrue(metrics.get("availableProcessors").getAsInt()>0);
      var simulation=metrics.getAsJsonObject("simulation");
      assertEquals(1_000_000_000L/PreviewWorld.TICK_RATE,simulation.get("stepNanos").getAsLong());
      assertTrue(simulation.get("completedTicks").getAsLong()>=0);
      assertFalse(response.body().contains(admin));
      assertEquals(200,client.send(request(url,"/health",null,null),HttpResponse.BodyHandlers.ofString()).statusCode());
    }
  }

  @Test void twentyClientsReplicateAndReconnectOverRealHttpWithoutSharingCredentials() throws Exception {
    var map = new PreviewMap(new boolean[60][80], 0, 0, 30, 30);
    try (var server = new PreviewServer(map, 0); var client = HttpClient.newHttpClient()) {
      server.start();
      String url = server.url();
      var tokens = new ArrayList<String>();
      assertEquals(com.perso.T4C.online.model.OnlineProtocol.VERSION,
          object(client.send(request(url, "/map", null, null), HttpResponse.BodyHandlers.ofString())).get("protocolVersion").getAsInt());
      // Rejected versions must not allocate sessions: all twenty slots remain available below.
      for (String version : new String[] {"", "999"}) {
        var incompatible = HttpRequest.newBuilder(URI.create(url + "/join"))
            .header("Origin", url).header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.noBody());
        if (!version.isEmpty()) incompatible.header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER, version);
        assertEquals(426, client.send(incompatible.build(), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
      for (int i = 0; i < PreviewWorld.MAX_PLAYERS; i++) {
        var joined = client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, joined.statusCode()); tokens.add(object(joined).get("token").getAsString());
      }
      assertEquals(409, client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString()).statusCode());
      long startTick = 0, endTick = 0;
      for (int round = 0; round < 20; round++) {
        var pending = new ArrayList<CompletableFuture<HttpResponse<String>>>();
        for (String token : tokens)
          pending.add(client.sendAsync(request(url, "/state", token, null), HttpResponse.BodyHandlers.ofString()));
        CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).get();
        for (var future : pending) {
          var result = future.get(); assertEquals(200, result.statusCode());
          assertEquals(PreviewWorld.MAX_PLAYERS, object(result).getAsJsonArray("players").size());
          for (String token : tokens) assertFalse(result.body().contains(token));
          endTick = object(result).get("tick").getAsLong();
        }
        if (round == 0) startTick = endTick;
      }
      assertTrue(endTick > startTick);
      var moved = client.send(request(url, "/input", tokens.getFirst(), "0 1 1 0"), HttpResponse.BodyHandlers.ofString());
      assertEquals(200, moved.statusCode());
      // Observe the same actor position from another authenticated client after a later server tick.
      JsonObject state;
      long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
      do {
        state = object(client.send(request(url, "/state", tokens.get(1), null), HttpResponse.BodyHandlers.ofString()));
      } while (state.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble() <= map.spawnX
          && System.nanoTime() < deadline);
      assertTrue(state.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble() > map.spawnX);
      assertEquals(200, client.send(request(url, "/leave", tokens.getFirst(), ""), HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(401, client.send(request(url, "/state", tokens.getFirst(), null), HttpResponse.BodyHandlers.ofString()).statusCode());
      var joined = client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString());
      assertEquals(200, joined.statusCode()); assertNotEquals(tokens.getFirst(), object(joined).get("token").getAsString());
    }
  }

  @Test void rejectsCrossOriginUnauthorizedAndMalformedCommandsAndCanRestart() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 3, 3);
    int port;
    try (var client = HttpClient.newHttpClient()) {
      try (var server = new PreviewServer(map, 0)) {
        server.start(); String url = server.url(); port = URI.create(url).getPort();
        var foreign = HttpRequest.newBuilder(URI.create(url + "/join")).header("Origin", "https://example.org")
            .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(403, client.send(foreign, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(401, client.send(request(url, "/state", null, null), HttpResponse.BodyHandlers.ofString()).statusCode());
        String token = object(client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString())).get("token").getAsString();
        assertEquals(400, client.send(request(url, "/input", token, "1 99 0"), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(400, client.send(request(url, "/input", token, "1 1 0 9999 9999"), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(413, client.send(request(url, "/input", token, "x".repeat(65)), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
      try (var restarted = new PreviewServer(map, port)) {
        restarted.start();
        assertEquals(200, client.send(request(restarted.url(), "/health", null, null), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void persistentPlayersSaveOnLogoutAndAdminShutdownThenRestoreOnRestart() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var key=store.provisionAccount(); var other=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.PALADIN,"female",false,new java.util.Random(1));
    var character=store.create(key.accountId(),"HTTP saved player",0,30.5,30.5,prepared.progress(),prepared.gold());
    var second=store.create(other.accountId(),"HTTP second player",0,32.5,30.5);
    var map=new PreviewMap(new boolean[60][80],0,0,30,30);
    String admin="c".repeat(64);
    double savedX;
    long savedAppearance;
    int port;
    try (var client=HttpClient.newHttpClient()) {
      try (var server=new PreviewServer(map,0,store,admin)) {
        server.start(); String url=server.url(); port=URI.create(url).getPort();
        String token=login(client,url,key,character.id());
        String secondToken=login(client,url,other,second.id());
        assertEquals(401,client.send(request(url,"/character",null,null),HttpResponse.BodyHandlers.ofString()).statusCode());
        var privateResponse=client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,privateResponse.statusCode());
        var privateState=object(privateResponse);
        assertEquals(character.id().toString(),privateState.get("id").getAsString());
        assertEquals(prepared.gold(),privateState.get("gold").getAsLong());
        assertEquals(prepared.progress(),new com.google.gson.Gson().fromJson(privateState.get("progress"),
            com.perso.T4C.online.model.CharacterProgress.class));
        assertFalse(privateResponse.body().contains(key.secret()));
        assertFalse(privateResponse.body().contains(key.accountId().toString()));
        long revision = privateState.get("revision").getAsLong();
        var headItem = prepared.progress().equipment().get("HEAD");
        assertEquals(401, client.send(request(url, "/unequip", null, revision + " 0 HEAD"), HttpResponse.BodyHandlers.ofString()).statusCode());
        var unequipped = client.send(request(url, "/unequip", token, revision + " 0 HEAD"), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, unequipped.statusCode());
        assertEquals(409, client.send(request(url, "/equip", token, revision + " 0 " + headItem + " HEAD"), HttpResponse.BodyHandlers.ofString()).statusCode(), "A stale command must not overwrite committed gear");
        long afterUnequip = object(unequipped).get("revision").getAsLong();
        var equipped = client.send(request(url, "/equip", token, afterUnequip + " 0 " + headItem + " HEAD"), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, equipped.statusCode());
        assertEquals(200, client.send(request(url, "/unequip", token, object(equipped).get("revision").getAsLong() + " 0 HEAD"), HttpResponse.BodyHandlers.ofString()).statusCode());
        var committedGear = store.load(key.accountId(), character.id()).progress();
        assertFalse(committedGear.equipment().containsKey("HEAD"));
        assertEquals(prepared.progress().inventory(), committedGear.inventory(), "Equipment commands must never add or remove owned instances");
        var secondState=object(client.send(request(url,"/character",secondToken,null),HttpResponse.BodyHandlers.ofString()));
        assertEquals(second.id().toString(),secondState.get("id").getAsString());
        assertFalse(secondState.has("progress"),"Legacy profiles remain uninitialized");
        assertEquals(409,client.send(request(url,"/join",null,key.secret()+" "+character.id()),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(403,client.send(request(url,"/admin/shutdown",token,""),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",token,"0 0 1 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        long deadline=System.nanoTime()+Duration.ofSeconds(3).toNanos();
        JsonObject snapshot;
        do {
          snapshot=object(client.send(request(url,"/state",secondToken,null),HttpResponse.BodyHandlers.ofString()));
        } while (snapshot.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble()<=character.x() && System.nanoTime()<deadline);
        assertEquals(2,snapshot.getAsJsonArray("players").size());
        assertFalse(snapshot.toString().contains("inventory"));
        assertFalse(snapshot.toString().contains(character.id().toString()));
        assertTrue(snapshot.has("characterRevision"));
        assertTrue(snapshot.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble()>character.x());
        assertEquals(200,client.send(request(url,"/input",token,"0 1 0 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        var stopped=object(client.send(request(url,"/state",token,null),HttpResponse.BodyHandlers.ofString()));
        savedX=stopped.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble();
        savedAppearance=stopped.getAsJsonArray("players").get(0).getAsJsonObject().get("appearance").getAsLong();
        assertEquals(0,savedAppearance,"Saved female appearance must override temporary player ID");
        assertEquals(200,client.send(request(url,"/leave",token,""),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(savedX,store.load(key.accountId(),character.id()).x());
        assertEquals(200,client.send(request(url,"/admin/shutdown",admin,""),HttpResponse.BodyHandlers.ofString()).statusCode());
        server.await();
      }
      try (var restarted=new PreviewServer(map,port,store)) {
        restarted.start();
        String token=login(client,restarted.url(),key,character.id());
        login(client,restarted.url(),other,second.id()); // Shutdown released the second player's lease.
        var snapshot=object(client.send(request(restarted.url(),"/state",token,null),HttpResponse.BodyHandlers.ofString()));
        assertEquals(savedX,snapshot.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble());
        assertEquals(savedAppearance,snapshot.getAsJsonArray("players").get(0).getAsJsonObject().get("appearance").getAsLong());
        var restored=store.load(key.accountId(),character.id());
        var expected = new com.google.gson.Gson().toJsonTree(prepared.progress()).getAsJsonObject();
        expected.getAsJsonObject("equipment").remove("HEAD");
        assertEquals(new com.google.gson.Gson().fromJson(expected, com.perso.T4C.online.model.CharacterProgress.class),restored.progress());
        assertEquals(prepared.gold(),restored.gold(),"Reconnect must not grant the starter kit twice");
      }
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void databaseWorkerStallDoesNotBlockSimulationOrSnapshots() throws Exception {
    var pause=new java.util.concurrent.atomic.AtomicBoolean();
    var entered=new java.util.concurrent.CountDownLatch(1);
    var release=new java.util.concurrent.CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if (pause.get()) {
        entered.countDown();
        try { if (!release.await(5,java.util.concurrent.TimeUnit.SECONDS)) throw new java.sql.SQLException("Test gate timeout"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new java.sql.SQLException(e); }
      }
      return java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
    });
    store.migrate(); var key=store.provisionAccount();
    var character=store.create(key.accountId(),"Worker isolation",0,3.5,3.5);
    try (var server=new PreviewServer(new PreviewMap(new boolean[8][8],0,0,3,3),0,store); var client=HttpClient.newHttpClient()) {
      server.start(); String token=login(client,server.url(),key,character.id()); pause.set(true);
      try {
        assertTrue(entered.await(8,java.util.concurrent.TimeUnit.SECONDS));
        var start=object(client.send(request(server.url(),"/state",token,null),HttpResponse.BodyHandlers.ofString())).get("tick").getAsLong();
        long end=start;
        for (int i=0;i<5;i++) end=object(client.send(request(server.url(),"/state",token,null),HttpResponse.BodyHandlers.ofString())).get("tick").getAsLong();
        assertTrue(end>start,"World must continue while storage is blocked");
        try(var closers=java.util.concurrent.Executors.newFixedThreadPool(2)) {
          var calling=new java.util.concurrent.CountDownLatch(2);
          var first=closers.submit(()-> { calling.countDown();server.close(); });
          var second=closers.submit(()-> { calling.countDown();server.close(); });
          try {
            assertTrue(calling.await(2,java.util.concurrent.TimeUnit.SECONDS));
            assertThrows(java.util.concurrent.TimeoutException.class,()->first.get(100,java.util.concurrent.TimeUnit.MILLISECONDS));
            assertThrows(java.util.concurrent.TimeoutException.class,()->second.get(100,java.util.concurrent.TimeUnit.MILLISECONDS));
          } finally { pause.set(false);release.countDown(); }
          first.get(5,java.util.concurrent.TimeUnit.SECONDS);second.get(5,java.util.concurrent.TimeUnit.SECONDS);
        }
        var recovered=store.acquire(key.secret(),character.id());store.release(recovered);
      } finally { release.countDown(); }
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void corpseHttpRecoveryKeepsOwnershipDistanceAndItemIdentityAcrossRetries() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var owner=store.provisionAccount(); var stranger=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.WARRIOR,"male",false,new java.util.Random(3));
    var catalog=ItemCatalog.loadBundled(); var gson=new com.google.gson.Gson();
    var tree=gson.toJsonTree(prepared.progress()).getAsJsonObject();
    String key=catalog.items().values().stream().filter(i->!i.undroppable()).map(i->i.key()).sorted().findFirst().orElseThrow();
    var ownedItem=new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),key,1,7);
    tree.getAsJsonArray("inventory").add(gson.toJsonTree(ownedItem));
    var progress=gson.fromJson(tree,com.perso.T4C.online.model.CharacterProgress.class);
    var character=store.create(owner.accountId(),"HTTP corpse owner",0,30.5,30.5,progress,50);
    var other=store.create(stranger.accountId(),"HTTP corpse stranger",0,30.5,30.5,prepared.progress(),0);
    var fixture=store.acquire(owner.secret(),character.id());
    var life=store.loadOrCreateSpawn("http-death:"+java.util.UUID.randomUUID());
    var saved=store.commitGameplayEvent(fixture,character.revision(),java.util.UUID.randomUUID(),0,0,progress,
        new OnlineStore.CreatureProvocation(life.key(),life.eventId(),character.id(),0)).character();
    var death=OnlineDeath.prepare(saved,catalog,
        new com.perso.T4C.online.model.CharacterProgress.Respawn(0,30.5,30.5),
        new com.perso.T4C.online.model.CharacterProgress.Respawn(0,3.5,3.5),
        new java.util.Random() { @Override public double nextDouble() { return 0; } });
    var corpse=java.util.UUID.randomUUID();
    store.commitMonsterAttack(fixture,saved.revision(),corpse,death.progress(),
        new OnlineStore.MonsterAttack(life.key(),life.eventId(),character.id(),0,System.currentTimeMillis()+1500),death.transition());
    store.release(fixture);
    try (var server=new PreviewServer(new PreviewMap(new boolean[60][80],0,0,3,3),0,store);
         var client=HttpClient.newHttpClient()) {
      server.start(); String url=server.url();
      String token=login(client,url,owner,character.id()); String foreign=login(client,url,stranger,other.id());
      assertEquals(401,client.send(request(url,"/corpses",null,null),HttpResponse.BodyHandlers.ofString()).statusCode());
      var listed=client.send(request(url,"/corpses",token,null),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,listed.statusCode());
      var summaries=JsonParser.parseString(listed.body()).getAsJsonArray();
      assertEquals(1,summaries.size());
      var summary=summaries.get(0).getAsJsonObject();
      assertEquals(corpse.toString(),summary.get("id").getAsString());
      assertEquals(death.transition().droppedItems().size(),summary.get("itemCount").getAsInt());
      assertFalse(summary.has("items")); assertFalse(listed.body().contains(character.id().toString()));
      assertFalse(listed.body().contains(ownedItem.id().toString()));
      assertTrue(JsonParser.parseString(client.send(request(url,"/corpses",foreign,null),HttpResponse.BodyHandlers.ofString()).body()).getAsJsonArray().isEmpty());
      assertEquals(409,client.send(request(url,"/recover",foreign,"0 0 "+corpse),HttpResponse.BodyHandlers.ofString()).statusCode());
      long revision=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      assertEquals(409,client.send(request(url,"/recover",token,revision+" 1 "+corpse),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(request(url,"/recover",token,"9223372036854775808 1 "+corpse),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(200,client.send(request(url,"/leave",token,""),HttpResponse.BodyHandlers.ofString()).statusCode());
      var reposition=store.acquire(owner.secret(),character.id());
      store.checkpoint(reposition,reposition.character().revision(),0,30.5,30.5); store.release(reposition);
      token=login(client,url,owner,character.id());
      assertEquals(200,client.send(request(url,"/corpses",token,null),HttpResponse.BodyHandlers.ofString()).statusCode());
      revision=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      assertEquals(409,client.send(request(url,"/recover",token,"0 1 "+corpse),HttpResponse.BodyHandlers.ofString()).statusCode());
      var recovered=client.send(request(url,"/recover",token,revision+" 1 "+corpse),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,recovered.statusCode(),recovered.body());
      long acknowledged=object(recovered).get("revision").getAsLong(); assertTrue(acknowledged>revision);
      var committed=store.load(owner.accountId(),character.id());
      assertTrue(committed.revision()>=acknowledged);
      assertEquals(new java.util.HashSet<>(progress.inventory()),new java.util.HashSet<>(committed.progress().inventory()));
      assertEquals(1,committed.progress().inventory().stream().filter(i->i.id().equals(ownedItem.id())).count());
      var retry=client.send(request(url,"/recover",token,acknowledged+" 1 "+corpse),HttpResponse.BodyHandlers.ofString());
      assertTrue(retry.statusCode()==200 || retry.statusCode()==409,"Retry may acknowledge or reject an already recovered corpse");
      assertEquals(committed.progress().inventory(),store.load(owner.accountId(),character.id()).progress().inventory());
      assertTrue(JsonParser.parseString(client.send(request(url,"/corpses",token,null),HttpResponse.BodyHandlers.ofString()).body()).getAsJsonArray().isEmpty());
    }
  }

  private static HttpRequest pickupRequest(String url, String token, String version, String body) {
    var builder = HttpRequest.newBuilder(URI.create(url + "/pickup")).timeout(Duration.ofSeconds(5))
        .header("Origin", url).header("Content-Type", "text/plain");
    if (version != null) builder.header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER, version);
    if (token != null) builder.header("Authorization", "Bearer " + token);
    return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
  }

  @Test void pickupRejectsOldProtocolAndOversizedBodiesAndLootListIsEmptyWithoutStorage() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 3, 3);
    String current = Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    String loot = "1 0 " + java.util.UUID.randomUUID();
    try (var server = new PreviewServer(map, 0); var client = HttpClient.newHttpClient()) {
      server.start(); String url = server.url();
      // The version gate runs before the storage and route checks, so an old client never reaches a command.
      assertEquals(426, client.send(pickupRequest(url, "a".repeat(64), null, loot), HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(426, client.send(pickupRequest(url, "a".repeat(64), "4", loot), HttpResponse.BodyHandlers.ofString()).statusCode());
      // Without a database there is no pickup route; with the current protocol the request still parses first.
      assertEquals(404, client.send(pickupRequest(url, "a".repeat(64), current, loot), HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(413, client.send(pickupRequest(url, "a".repeat(64), current, "x".repeat(97)), HttpResponse.BodyHandlers.ofString()).statusCode());
      String token = object(client.send(request(url, "/join", null, ""), HttpResponse.BodyHandlers.ofString())).get("token").getAsString();
      var state = object(client.send(request(url, "/state", token, null), HttpResponse.BodyHandlers.ofString()));
      assertTrue(state.getAsJsonArray("loot").isEmpty(), "Ground loot needs persistent sessions");
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void pickupHttpClaimsOwnedDropOnceAndShowsOnlyPublicOwnership() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var owner=store.provisionAccount(); var stranger=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.WARRIOR,"male",false,new java.util.Random(7));
    var character=store.create(owner.accountId(),"HTTP pickup owner",0,30.5,30.5,prepared.progress(),prepared.gold());
    var other=store.create(stranger.accountId(),"HTTP pickup stranger",0,30.5,30.5,prepared.progress(),0);
    var fixture=store.acquire(owner.secret(),character.id());
    var life=store.loadOrCreateSpawn("http-pickup:"+java.util.UUID.randomUUID());
    var drop=new com.perso.T4C.online.model.LootDropDraft(java.util.UUID.randomUUID(),0,"item.leather_gloves",1,0,0,30.5,30.5);
    var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),10,10,0);
    assertTrue(store.commitCreatureStrike(fixture,character.revision(),life.eventId(),0,0,prepared.progress(),strike,null,java.util.List.of(drop)).applied());
    store.release(fixture);
    String current=Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server=new PreviewServer(new PreviewMap(new boolean[60][80],0,0,30,30),0,store); var client=HttpClient.newHttpClient()) {
      server.start(); String url=server.url();
      String token=login(client,url,owner,character.id()); String foreign=login(client,url,stranger,other.id());
      var state=object(client.send(request(url,"/state",token,null),HttpResponse.BodyHandlers.ofString()));
      var listed=state.getAsJsonArray("loot"); assertEquals(1,listed.size());
      var entry=listed.get(0).getAsJsonObject();
      assertEquals(drop.id().toString(),entry.get("id").getAsString());
      assertEquals(30.5,entry.get("x").getAsDouble()); assertEquals("item.leather_gloves",entry.get("key").getAsString());
      assertTrue(entry.get("ownerPlayerId").isJsonPrimitive(),"The owner's own view names their public player ID");
      assertFalse(state.toString().contains(character.id().toString()));
      assertFalse(state.toString().contains(owner.accountId().toString()));
      var seenByStranger=object(client.send(request(url,"/state",foreign,null),HttpResponse.BodyHandlers.ofString()))
          .getAsJsonArray("loot").get(0).getAsJsonObject();
      assertTrue(seenByStranger.get("ownerPlayerId").isJsonNull(),"Other owners are never named");
      assertTrue(seenByStranger.get("ownerRemainingMs").getAsLong()>0);
      long revision=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      var uuid=drop.id();
      assertEquals(400,client.send(pickupRequest(url,token,current,"abc"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(pickupRequest(url,token,current,revision+" 0 not-a-uuid"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(pickupRequest(url,token,current,"9223372036854775808 0 "+uuid),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(413,client.send(pickupRequest(url,token,current,"x".repeat(97)),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(426,client.send(pickupRequest(url,token,"4",revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(401,client.send(pickupRequest(url,null,current,revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(409,client.send(pickupRequest(url,token,current,(revision+1)+" 0 "+uuid),HttpResponse.BodyHandlers.ofString()).statusCode(),"A stale revision is refused before any claim");
      var refused=client.send(pickupRequest(url,foreign,current,revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,refused.statusCode(),"Only the owner may claim during the owner window");
      assertTrue(refused.body().contains("loot_reserved"));
      var claimed=client.send(pickupRequest(url,token,current,revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,claimed.statusCode(),claimed.body());
      assertTrue(object(claimed).get("revision").getAsLong()>revision);
      assertTrue(object(client.send(request(url,"/state",token,null),HttpResponse.BodyHandlers.ofString())).getAsJsonArray("loot").isEmpty());
      assertEquals(409,client.send(pickupRequest(url,token,current,revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString()).statusCode(),"A replay never claims twice");
      var saved=store.load(owner.accountId(),character.id());
      assertEquals(1,saved.progress().inventory().stream().filter(item->item.id().equals(uuid)).count());
      assertTrue(store.listGroundLoot(0,new OnlineStore.TileRect(0,0,1000,1000)).stream().noneMatch(loot->loot.id().equals(uuid)));
    }
  }

  private String login(HttpClient client,String url,OnlineStore.AccountKey key,java.util.UUID character) throws Exception {
    var response=client.send(request(url,"/join",null,key.secret()+" "+character),HttpResponse.BodyHandlers.ofString());
    assertEquals(200,response.statusCode());
    return object(response).get("token").getAsString();
  }
}
