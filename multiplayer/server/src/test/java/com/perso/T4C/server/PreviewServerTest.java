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
    if (token != null) builder.header("Authorization", "Bearer " + token);
    if (body != null) builder.header("Origin", url).header("Content-Type", "text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(body));
    return builder.build();
  }
  private JsonObject object(HttpResponse<String> response) { return JsonParser.parseString(response.body()).getAsJsonObject(); }

  @Test void twentyClientsReplicateAndReconnectOverRealHttpWithoutSharingCredentials() throws Exception {
    var map = new PreviewMap(new boolean[60][80], 0, 0, 30, 30);
    try (var server = new PreviewServer(map, 0); var client = HttpClient.newHttpClient()) {
      server.start();
      String url = server.url();
      var tokens = new ArrayList<String>();
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
      var moved = client.send(request(url, "/input", tokens.getFirst(), "1 1 0"), HttpResponse.BodyHandlers.ofString());
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
        assertEquals(401, client.send(request(url, "/unequip", null, revision + " HEAD"), HttpResponse.BodyHandlers.ofString()).statusCode());
        var unequipped = client.send(request(url, "/unequip", token, revision + " HEAD"), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, unequipped.statusCode());
        assertEquals(409, client.send(request(url, "/equip", token, revision + " " + headItem + " HEAD"), HttpResponse.BodyHandlers.ofString()).statusCode(), "A stale command must not overwrite committed gear");
        long afterUnequip = object(unequipped).get("revision").getAsLong();
        var equipped = client.send(request(url, "/equip", token, afterUnequip + " " + headItem + " HEAD"), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, equipped.statusCode());
        assertEquals(200, client.send(request(url, "/unequip", token, object(equipped).get("revision").getAsLong() + " HEAD"), HttpResponse.BodyHandlers.ofString()).statusCode());
        var committedGear = store.load(key.accountId(), character.id()).progress();
        assertFalse(committedGear.equipment().containsKey("HEAD"));
        assertEquals(prepared.progress().inventory(), committedGear.inventory(), "Equipment commands must never add or remove owned instances");
        var secondState=object(client.send(request(url,"/character",secondToken,null),HttpResponse.BodyHandlers.ofString()));
        assertEquals(second.id().toString(),secondState.get("id").getAsString());
        assertFalse(secondState.has("progress"),"Legacy profiles remain uninitialized");
        assertEquals(409,client.send(request(url,"/join",null,key.secret()+" "+character.id()),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(403,client.send(request(url,"/admin/shutdown",token,""),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",token,"0 1 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
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
        assertEquals(200,client.send(request(url,"/input",token,"1 0 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
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
      } finally { release.countDown(); }
    }
  }

  private String login(HttpClient client,String url,OnlineStore.AccountKey key,java.util.UUID character) throws Exception {
    var response=client.send(request(url,"/join",null,key.secret()+" "+character),HttpResponse.BodyHandlers.ofString());
    assertEquals(200,response.statusCode());
    return object(response).get("token").getAsString();
  }
}
