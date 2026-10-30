package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.Socket;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.perso.T4C.server.persistence.OnlineStore;

class PreviewServerTest {
  @Test void statAllocationParserRejectsMalformedAndDuplicateEntries() {
    assertEquals(java.util.Map.of("strength", 2, "wisdom", 1),
        PreviewServer.parseStatAllocation("strength:2,wisdom:1"));
    for (String invalid : new String[] {"", "strength", "strength:-1", "strength:1,strength:2",
        "strength:2147483648", "strength:1,endurance:1,dexterity:1,wisdom:1,intelligence:1,strength:1"}) {
      assertThrows(PreviewWorld.Rejected.class, () -> PreviewServer.parseStatAllocation(invalid), invalid);
    }
    assertEquals(java.util.Map.of("unknown", 1), PreviewServer.parseStatAllocation("unknown:1"),
        "Known stat names are validated by authoritative progression rules");
    assertThrows(PreviewWorld.Rejected.class,
        () -> PreviewServer.parseStatAllocation("null"));
  }

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
  private static JsonObject trafficRow(JsonObject metrics,String route,String method,String status) {
    for(var value:metrics.getAsJsonObject("httpTraffic").getAsJsonArray("rows")) {
      var row=value.getAsJsonObject();
      if(route.equals(row.get("route").getAsString()) && method.equals(row.get("method").getAsString())
          && status.equals(row.get("selectedStatus").getAsString())) return row;
    }
    return null;
  }

  @Test void slowPublicAndForgedSessionBodiesDoNotStallTwoJoinedPlayers() throws Exception {
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var sockets=new ArrayList<Socket>();
    try(var server=new PreviewServer(map,0);var client=HttpClient.newHttpClient()) {
      server.start();String url=server.url();int port=URI.create(url).getPort();
      var joinedFirst=object(client.send(request(url,"/join",null,""),HttpResponse.BodyHandlers.ofString()));
      var joinedSecond=object(client.send(request(url,"/join",null,""),HttpResponse.BodyHandlers.ofString()));
      String first=joinedFirst.get("token").getAsString(),second=joinedSecond.get("token").getAsString();
      long firstId=joinedFirst.get("id").getAsLong(),secondId=joinedSecond.get("id").getAsLong();
      try {
        for(int i=0;i<8;i++) sockets.add(slowBody(port,url,"/join",null));
        for(int i=0;i<8;i++) sockets.add(slowBody(port,url,"/input","f".repeat(64)));
        sockets.add(slowBody(port,url,"/unknown",null));
        sockets.add(slowBody(port,url,"/join",null,1000));
        Thread.sleep(150);
        assertEquals(200,client.send(request(url,"/health",null,null),HttpResponse.BodyHandlers.ofString()).statusCode(),
            "Slow public bodies must not block the read-only health diagnostic");
        for(int i=8;i<16;i++) assertTrue(responseLine(sockets.get(i)).contains(" 401 "),
            "A forged Bearer token must be refused before its body arrives");
        assertTrue(responseLine(sockets.get(16)).contains(" 404 "),"Unknown routes must fail before body reads");
        assertTrue(responseLine(sockets.get(17)).contains(" 413 "),"Oversized declared bodies must fail early");
        long began=System.nanoTime();
        var before=object(client.send(request(url,"/state",first,null),HttpResponse.BodyHandlers.ofString()));
        assertEquals(2,before.getAsJsonArray("players").size(),"Slow joins must not allocate actors");
        assertEquals(200,client.send(request(url,"/state",second,null),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",first,"0 1 1 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",second,"0 1 0 1"),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-began)<2500,
            "Two valid sessions waited behind held unauthenticated bodies");
        Thread.sleep(120);
        var after=object(client.send(request(url,"/state",first,null),HttpResponse.BodyHandlers.ofString()));
        assertEquals(2,after.getAsJsonArray("players").size());
        double firstX=Double.NaN,secondY=Double.NaN;
        for(var player:after.getAsJsonArray("players")) {
          var actor=player.getAsJsonObject();
          if(actor.get("id").getAsLong()==firstId) firstX=actor.get("x").getAsDouble();
          if(actor.get("id").getAsLong()==secondId) secondY=actor.get("y").getAsDouble();
        }
        assertTrue(firstX>3.5 && secondY>3.5,"Both private movement intents must reach the world");
        assertEquals(200,client.send(request(url,"/input",first,"0 2 0 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",second,"0 2 0 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        var closing=CompletableFuture.runAsync(server::close);
        closing.get(5,TimeUnit.SECONDS);
      } finally { for(var socket:sockets) socket.close(); }
    }
  }

  private static Socket slowBody(int port,String origin,String path,String token) throws Exception {
    return slowBody(port,origin,path,token,64);
  }

  private static Socket slowBody(int port,String origin,String path,String token,int length) throws Exception {
    var socket=new Socket();socket.connect(new InetSocketAddress("127.0.0.1",port),1000);
    String headers="POST "+path+" HTTP/1.1\r\nHost: 127.0.0.1:"+port+"\r\nOrigin: "+origin
        +"\r\nContent-Type: text/plain\r\nContent-Length: "+length+"\r\n"
        +(path.equals("/join") ? com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER+": "
            +com.perso.T4C.online.model.OnlineProtocol.VERSION+"\r\n" : "Authorization: Bearer "+token+"\r\n")
        +"Connection: close\r\n\r\n";
    socket.getOutputStream().write(headers.getBytes(StandardCharsets.US_ASCII));
    socket.getOutputStream().flush();
    return socket;
  }

  private static String responseLine(Socket socket) throws Exception {
    socket.setSoTimeout(4000);
    var bytes=new java.io.ByteArrayOutputStream();
    for(int i=0;i<80;i++) {
      int next=socket.getInputStream().read();
      if(next<0 || next=='\n') break;
      bytes.write(next);
    }
    return bytes.toString(StandardCharsets.US_ASCII);
  }

  /**
   * Reads the status line, then the rest of the response up to the server closing the connection
   * (these requests send {@code Connection: close}). Closing the client socket after only the
   * status line races the server still writing its body: a failed write makes the handler send
   * a 503 and re-label the request, so the metrics rows the test then reads would be nondeterministic.
   */
  private static String fullResponseLine(Socket socket) throws Exception {
    String line=responseLine(socket);
    try { socket.getInputStream().readAllBytes(); } catch(java.io.IOException closedOrSlow) { /* the server closed first */ }
    return line;
  }

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
      var health=client.send(request(url,"/health",null,null),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,health.statusCode());
      var joined=client.send(request(url,"/join",null,""),HttpResponse.BodyHandlers.ofString());
      String token=object(joined).get("token").getAsString();
      String input="0 1 1 0";
      var moved=client.send(request(url,"/input",token,input),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,moved.statusCode());
      String marker="private-route-marker";
      int port=URI.create(url).getPort();
      try(var unknown=slowBody(port,url,"/"+marker,null);
          var forged=slowBody(port,url,"/input","f".repeat(64));
          var oversized=slowBody(port,url,"/join",null,1000)) {
        assertTrue(fullResponseLine(unknown).contains(" 404 "));
        assertTrue(fullResponseLine(forged).contains(" 401 "));
        assertTrue(fullResponseLine(oversized).contains(" 413 "));
      }
      JsonObject traffic=null;
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      do {
        traffic=object(client.send(request(url,"/admin/metrics",admin,null),HttpResponse.BodyHandlers.ofString()));
        if(trafficRow(traffic,"other","POST","404")!=null
            && trafficRow(traffic,"/input","POST","401")!=null
            && trafficRow(traffic,"/join","POST","413")!=null) break;
        Thread.sleep(25);
      } while(System.nanoTime()<deadline);
      assertNotNull(trafficRow(traffic,"other","POST","404"));
      assertNotNull(trafficRow(traffic,"/input","POST","401"));
      assertNotNull(trafficRow(traffic,"/join","POST","413"));
      var inputRow=trafficRow(traffic,"/input","POST","200");
      assertNotNull(inputRow);
      assertEquals(input.getBytes(StandardCharsets.US_ASCII).length,inputRow.get("requestBodyBytesRead").getAsLong());
      assertEquals(moved.body().getBytes(StandardCharsets.UTF_8).length,
          inputRow.get("responseBodyBytesFullyWritten").getAsLong());
      assertEquals(health.body().getBytes(StandardCharsets.UTF_8).length,
          trafficRow(traffic,"/health","GET","200").get("responseBodyBytesFullyWritten").getAsLong());
      for(var row:new JsonObject[] {trafficRow(traffic,"other","POST","404"),
          trafficRow(traffic,"/input","POST","401"),trafficRow(traffic,"/join","POST","413")})
        assertEquals(0,row.get("requestBodyBytesRead").getAsLong(),"Early refusal must precede body reads");
      String report=traffic.toString();
      assertFalse(report.contains(marker));
      assertFalse(report.contains(admin));
      assertFalse(report.contains(token));
      // Both handlers are inside a body read at once. Their completions must keep their own
      // route, status, and byte counts even when the HTTP context is shared.
      try(var heldInput=slowBody(port,url,"/input",token,7);
          var heldJoin=slowBody(port,url,"/join",null,1)) {
        long activeDeadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
        do {
          traffic=object(client.send(request(url,"/admin/metrics",admin,null),HttpResponse.BodyHandlers.ofString()));
          if(traffic.getAsJsonObject("httpTraffic").get("inflight").getAsLong()>=3) break;
          Thread.sleep(25);
        } while(System.nanoTime()<activeDeadline);
        assertTrue(traffic.getAsJsonObject("httpTraffic").get("inflight").getAsLong()>=3);
        heldInput.getOutputStream().write("0 2 0 0".getBytes(StandardCharsets.US_ASCII));
        heldInput.getOutputStream().flush();
        heldJoin.getOutputStream().write('x');
        heldJoin.getOutputStream().flush();
        assertTrue(fullResponseLine(heldInput).contains(" 200 "));
        assertTrue(fullResponseLine(heldJoin).contains(" 400 "));
      }
      long completedDeadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
      do {
        traffic=object(client.send(request(url,"/admin/metrics",admin,null),HttpResponse.BodyHandlers.ofString()));
        var joinedBad=trafficRow(traffic,"/join","POST","400");
        var acceptedInput=trafficRow(traffic,"/input","POST","200");
        if(joinedBad!=null && acceptedInput!=null && acceptedInput.get("completed").getAsLong()==2) break;
        Thread.sleep(25);
      } while(System.nanoTime()<completedDeadline);
      assertEquals(1,trafficRow(traffic,"/join","POST","400").get("requestBodyBytesRead").getAsLong());
      assertEquals(14,trafficRow(traffic,"/input","POST","200").get("requestBodyBytesRead").getAsLong());
      assertEquals(2,trafficRow(traffic,"/input","POST","200").get("completed").getAsLong());
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
      try (var restarted = PreviewServerRestarts.onSamePort(map, port, null)) {
        restarted.start();
        assertEquals(200, client.send(request(restarted.url(), "/health", null, null), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void blockedAccountRostersDoNotOccupyTwoGameplaySessions() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var first=store.provisionAccount();var second=store.provisionAccount();
    var firstCharacter=store.create(first.accountId(),"Roster gameplay A",0,3.5,3.5);
    var secondCharacter=store.create(second.accountId(),"Roster gameplay B",0,4.5,3.5);
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    try(var server=new PreviewServer(map,0,store);var client=HttpClient.newHttpClient();
        var blocker=java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL"),
            System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"))) {
      server.start();String url=server.url();
      String firstToken=login(client,url,first,firstCharacter.id());
      String secondToken=login(client,url,second,secondCharacter.id());
      blocker.setAutoCommit(false);
      try(var lock=blocker.createStatement()) { lock.execute("LOCK TABLE online_accounts IN ACCESS EXCLUSIVE MODE"); }
      var rosterOne=client.sendAsync(rosterRequest(url,first.secret()),HttpResponse.BodyHandlers.ofString());
      var rosterTwo=client.sendAsync(rosterRequest(url,second.secret()),HttpResponse.BodyHandlers.ofString());
      try {
        long waitUntil=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(1500);
        int waiting;
        do {
          try(var query=blocker.createStatement()) {
            query.execute("SELECT pg_stat_clear_snapshot()");
            try(var rows=query.executeQuery("""
                SELECT count(*) FROM pg_stat_activity
                WHERE datname=current_database() AND wait_event_type='Lock'
                  AND query ILIKE '%online_accounts%' AND pid<>pg_backend_pid()
                """)) { rows.next();waiting=rows.getInt(1); }
          }
          if(waiting<2) Thread.sleep(10);
        } while(waiting<2 && System.nanoTime()<waitUntil);
        assertEquals(2,waiting,"Both roster requests must be blocked by the database fixture");
        assertEquals(503,client.send(rosterRequest(url,first.secret()),HttpResponse.BodyHandlers.ofString()).statusCode(),
            "A third roster must not queue behind the blocked pair");
        long began=System.nanoTime();
        assertEquals(2,object(client.send(request(url,"/state",firstToken,null),HttpResponse.BodyHandlers.ofString()))
            .getAsJsonArray("players").size());
        assertEquals(200,client.send(request(url,"/state",secondToken,null),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",firstToken,"0 1 1 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",secondToken,"0 1 0 1"),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-began)<1500,
            "Gameplay requests stalled behind blocked account reads");
        assertEquals(200,client.send(request(url,"/input",firstToken,"0 2 0 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200,client.send(request(url,"/input",secondToken,"0 2 0 0"),HttpResponse.BodyHandlers.ofString()).statusCode());
      } finally { blocker.commit(); }
      assertEquals(200,rosterOne.get(3,TimeUnit.SECONDS).statusCode());
      assertEquals(200,rosterTwo.get(3,TimeUnit.SECONDS).statusCode());
    }
  }

  private static HttpRequest rosterRequest(String url,String key) {
    return HttpRequest.newBuilder(URI.create(url+"/account/characters")).timeout(Duration.ofSeconds(5))
        .header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER,
            Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION))
        .header("Authorization","Bearer "+key).GET().build();
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
        // The grid rule finishes the tile it started, so wait until the actor rests on a tile centre.
        JsonObject stopped;
        long restDeadline=System.nanoTime()+Duration.ofSeconds(3).toNanos();
        do {
          stopped=object(client.send(request(url,"/state",token,null),HttpResponse.BodyHandlers.ofString()));
          double restingX=stopped.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble();
          if (Math.abs(restingX-Math.floor(restingX)-0.5)<1e-9) break;
          Thread.sleep(20);
        } while (System.nanoTime()<restDeadline);
        savedX=stopped.getAsJsonArray("players").get(0).getAsJsonObject().get("x").getAsDouble();
        savedAppearance=stopped.getAsJsonArray("players").get(0).getAsJsonObject().get("appearance").getAsLong();
        assertEquals(0,savedAppearance,"Saved female appearance must override temporary player ID");
        assertEquals(200,client.send(request(url,"/leave",token,""),HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(savedX,store.load(key.accountId(),character.id()).x());
        assertEquals(200,client.send(request(url,"/admin/shutdown",admin,""),HttpResponse.BodyHandlers.ofString()).statusCode());
        server.await();
      }
      try (var restarted=PreviewServerRestarts.onSamePort(map,port,store)) {
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
      var exposedItems = summary.getAsJsonArray("items");
      assertEquals(death.transition().droppedItems().size(), exposedItems.size());
      var exposedOwnedItem = exposedItems.asList().stream().map(e -> e.getAsJsonObject())
          .filter(item -> ownedItem.id().toString().equals(item.get("id").getAsString())).findFirst().orElseThrow();
      assertEquals(ownedItem.key(), exposedOwnedItem.get("key").getAsString());
      assertEquals(ownedItem.quantity(), exposedOwnedItem.get("quantity").getAsInt());
      assertEquals(ownedItem.charges(), exposedOwnedItem.get("charges").getAsInt());
      assertFalse(listed.body().contains(character.id().toString()));
      var foreignCorpses = JsonParser.parseString(
          client.send(request(url,"/corpses",foreign,null),HttpResponse.BodyHandlers.ofString()).body()).getAsJsonArray();
      assertTrue(foreignCorpses.isEmpty());
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
      var entry=lootEntry(state,drop.id());
      assertEquals(drop.id().toString(),entry.get("id").getAsString());
      assertEquals(30.5,entry.get("x").getAsDouble()); assertEquals("item.leather_gloves",entry.get("key").getAsString());
      assertTrue(entry.get("ownerPlayerId").isJsonPrimitive(),"The owner's own view names their public player ID");
      assertFalse(state.toString().contains(character.id().toString()));
      assertFalse(state.toString().contains(owner.accountId().toString()));
      var seenByStranger=lootEntry(object(client.send(request(url,"/state",foreign,null),HttpResponse.BodyHandlers.ofString())),drop.id());
      assertTrue(!seenByStranger.has("ownerPlayerId") || seenByStranger.get("ownerPlayerId").isJsonNull(),"Other owners are never named");
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
      long strangerRevision=object(client.send(request(url,"/character",foreign,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      var refused=client.send(pickupRequest(url,foreign,current,strangerRevision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,refused.statusCode(),"Only the owner may claim during the owner window");
      assertTrue(refused.body().contains("loot_reserved"),refused.body());
      var claimed=client.send(pickupRequest(url,token,current,revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,claimed.statusCode(),claimed.body());
      assertTrue(object(claimed).get("revision").getAsLong()>revision);
      var afterClaim=object(client.send(request(url,"/state",token,null),HttpResponse.BodyHandlers.ofString())).getAsJsonArray("loot");
      for(var visible:afterClaim) assertNotEquals(uuid.toString(),visible.getAsJsonObject().get("id").getAsString(),
          "The claimed drop must leave the public region view even when other drops remain");
      assertEquals(409,client.send(pickupRequest(url,token,current,revision+" 0 "+uuid),HttpResponse.BodyHandlers.ofString()).statusCode(),"A replay never claims twice");
      var saved=store.load(owner.accountId(),character.id());
      assertEquals(1,saved.progress().inventory().stream().filter(item->item.id().equals(uuid)).count());
      assertTrue(store.listGroundLoot(0,new OnlineStore.TileRect(0,0,1000,1000)).stream().noneMatch(loot->loot.id().equals(uuid)));
    }
  }

  private static HttpRequest abilityRequest(String url, String path, String token, String version, String body) {
    var builder = HttpRequest.newBuilder(URI.create(url + path)).timeout(Duration.ofSeconds(5))
        .header("Origin", url).header("Content-Type", "text/plain");
    if (version != null) builder.header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER, version);
    if (token != null) builder.header("Authorization", "Bearer " + token);
    return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
  }

  @Test void castAndUseRoutesRequireCurrentProtocolAndStorageAndBoundTheirBodies() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 3, 3);
    String current = Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server = new PreviewServer(map, 0); var client = HttpClient.newHttpClient()) {
      server.start(); String url = server.url();
      for (String path : new String[] {"/cast", "/use"}) {
        String body = "1 0 " + ("/cast".equals(path) ? "spell.heal_light" : java.util.UUID.randomUUID());
        assertEquals(426, client.send(abilityRequest(url, path, "a".repeat(64), null, body), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(426, client.send(abilityRequest(url, path, "a".repeat(64), "4", body), HttpResponse.BodyHandlers.ofString()).statusCode());
        // Without a database there is no ability route.
        assertEquals(404, client.send(abilityRequest(url, path, "a".repeat(64), current, body), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(413, client.send(abilityRequest(url, path, "a".repeat(64), current, "x".repeat(129)), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
    }
  }

  @Test void shopRoutesRequireCurrentProtocolAndStorageAndBoundTheirBodies() throws Exception {
    var map = new PreviewMap(new boolean[8][8], 0, 0, 3, 3);
    String current = Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server = new PreviewServer(map, 0); var client = HttpClient.newHttpClient()) {
      server.start(); String url = server.url();
      for (String path : new String[] {"/shop-offers", "/shop-buy", "/shop-sell"}) {
        String body = "1 0 Sigfried" + ("/shop-offers".equals(path) ? "" : " item.torch 1");
        assertEquals(426, client.send(abilityRequest(url, path, "a".repeat(64), null, body), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(426, client.send(abilityRequest(url, path, "a".repeat(64), "4", body), HttpResponse.BodyHandlers.ofString()).statusCode());
        // Without a database there is no shop route.
        assertEquals(404, client.send(abilityRequest(url, path, "a".repeat(64), current, body), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(413, client.send(abilityRequest(url, path, "a".repeat(64), current, "x".repeat(257)), HttpResponse.BodyHandlers.ofString()).statusCode());
      }
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void castAndPotionHttpSpendResourcesRefuseDefinitelyAndKeepExpiriesAcrossReconnect() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var owner=store.provisionAccount(); var poor=store.provisionAccount();
    var base=OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.WARRIOR,"male",false,new java.util.Random(7)).progress();
    var potion=new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),"item.critical_healing_potion",1,1);
    // A day-old regeneration anchor: time spent logged out must not heal on join.
    var stale=new java.util.HashMap<String,Long>(); stale.put(OnlineRegeneration.ANCHOR,System.currentTimeMillis()-86_400_000L);
    var hurt=ProgressEdits.withInventory(ProgressEdits.withCooldowns(ProgressEdits.withSpells(
        ProgressEdits.withVitals(base,new com.perso.T4C.online.model.CharacterProgress.Vitals(10,500,30,30)),
        java.util.Set.of("spell.heal_light")),stale),java.util.stream.Stream.concat(base.inventory().stream(),java.util.stream.Stream.of(potion)).toList());
    var broke=ProgressEdits.withSpells(ProgressEdits.withVitals(base,
        new com.perso.T4C.online.model.CharacterProgress.Vitals(10,500,0,0)),java.util.Set.of("spell.heal_light"));
    var caster=store.create(owner.accountId(),"HTTP caster",0,30.5,30.5,hurt,0);
    var allyProgress=ProgressEdits.withVitals(base,new com.perso.T4C.online.model.CharacterProgress.Vitals(2,500,17,25));
    var ally=store.create(owner.accountId(),"HTTP ally",0,31.5,30.5,allyProgress,0);
    var poorCaster=store.create(poor.accountId(),"HTTP poor",0,30.5,30.5,broke,0);
    String current=Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server=new PreviewServer(new PreviewMap(new boolean[60][80],0,0,30,30),0,store); var client=HttpClient.newHttpClient()) {
      server.start(); String url=server.url();
      var casterJoin=join(client,url,owner,caster.id());String token=casterJoin.get("token").getAsString();
      var allyJoined=join(client,url,owner,ally.id());String allyToken=allyJoined.get("token").getAsString();
      var joined=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString()));
      assertTrue(joined.getAsJsonObject("progress").getAsJsonObject("vitals").get("hp").getAsInt()<=16,"Logged-out time never heals");
      long revision=joined.get("revision").getAsLong();
      assertEquals(400,client.send(abilityRequest(url,"/cast",token,current,"abc"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(abilityRequest(url,"/cast",token,current,revision+" 0 Spell.Heal"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(401,client.send(abilityRequest(url,"/cast",null,current,revision+" 0 spell.heal_light"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(409,client.send(abilityRequest(url,"/cast",token,current,(revision+1)+" 0 spell.heal_light"),HttpResponse.BodyHandlers.ofString()).statusCode(),"A stale revision commits nothing");
      var unknown=client.send(abilityRequest(url,"/cast",token,current,revision+" 0 spell.fireball_of_doom"),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,unknown.statusCode()); assertTrue(unknown.body().contains("not_learned"),unknown.body());
      var cast=client.send(abilityRequest(url,"/cast",token,current,
          revision+" 0 spell.heal_light a:"+allyJoined.get("id").getAsLong()),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,cast.statusCode(),cast.body());
      assertTrue(object(cast).get("revision").getAsLong()>revision);
      assertEquals(2,object(cast).get("manaSpent").getAsInt());
      var after=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString()));
      var vitals=after.getAsJsonObject("progress").getAsJsonObject("vitals");
      assertTrue(vitals.get("hp").getAsInt()<=16,"A player-target heal does not heal its caster");
      assertTrue(vitals.get("mana").getAsInt()>=28,"Two mana spent, at most natural regeneration since");
      var allyVitals=object(client.send(request(url,"/character",allyToken,null),HttpResponse.BodyHandlers.ofString()))
          .getAsJsonObject("progress").getAsJsonObject("vitals");
      assertTrue(allyVitals.get("hp").getAsInt()>2,"The live nearby target receives the committed heal");
      long next=after.get("revision").getAsLong();
      // Exhaustion: a second cast and movement are definite refusals that keep the player connected.
      var again=client.send(abilityRequest(url,"/cast",token,current,next+" 0 spell.heal_light"),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,again.statusCode()); assertTrue(again.body().contains("exhausted"),again.body());
      var moved=client.send(request(url,"/input",token,"0 1 1 0"),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,moved.statusCode()); assertTrue(moved.body().contains("exhausted"),moved.body());
      assertEquals(200,client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString()).statusCode());
      var committed=store.load(owner.accountId(),caster.id()).progress().cooldowns();
      long mental=committed.get(OnlineCasting.MENTAL_EXHAUSTION);
      assertEquals(200,client.send(request(url,"/leave",token,""),HttpResponse.BodyHandlers.ofString()).statusCode());
      // Reconnecting never shortens or resets an absolute expiry.
      String again2=login(client,url,owner,caster.id());
      long rejoinRevision=object(client.send(request(url,"/character",again2,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      assertTrue(store.load(owner.accountId(),caster.id()).progress().cooldowns().get(OnlineCasting.MENTAL_EXHAUSTION)>=mental);
      if (System.currentTimeMillis()<mental) {
        var stillExhausted=client.send(abilityRequest(url,"/cast",again2,current,rejoinRevision+" 0 spell.heal_light"),HttpResponse.BodyHandlers.ofString());
        assertEquals(409,stillExhausted.statusCode()); assertTrue(stillExhausted.body().contains("exhausted"));
      }
      // Potion: owned loose instance, spends its only charge; a foreign instance is refused.
      long waitMs=mental-System.currentTimeMillis(); if (waitMs>0) Thread.sleep(waitMs+50);
      var potionState=object(client.send(request(url,"/character",again2,null),HttpResponse.BodyHandlers.ofString()));
      long potionRevision=potionState.get("revision").getAsLong();
      assertEquals(409,client.send(abilityRequest(url,"/use",again2,current,potionRevision+" 0 "+java.util.UUID.randomUUID()),HttpResponse.BodyHandlers.ofString()).statusCode());
      var used=client.send(abilityRequest(url,"/use",again2,current,potionRevision+" 0 "+potion.id()),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,used.statusCode(),used.body());
      assertEquals(7,object(used).get("manaSpent").getAsInt());
      assertTrue(store.load(owner.accountId(),caster.id()).progress().inventory().stream().noneMatch(i->i.id().equals(potion.id())));
      // Mana refusal for a character that cannot afford the spell.
      String poorToken=login(client,url,poor,poorCaster.id());
      long poorRevision=object(client.send(request(url,"/character",poorToken,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      var refused=client.send(abilityRequest(url,"/cast",poorToken,current,poorRevision+" 0 spell.heal_light"),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,refused.statusCode()); assertTrue(refused.body().contains("not_enough_mana"),refused.body());
      assertEquals(200,client.send(request(url,"/character",poorToken,null),HttpResponse.BodyHandlers.ofString()).statusCode(),"A refusal keeps the player connected");
      assertEquals(0,store.load(poor.accountId(),poorCaster.id()).progress().vitals().mana());
    }
  }

  /** T4C-0319: every plain restore potion the offline game lets a player drink works over HTTP, one item per drink. */
  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void restorePotionsHealAndRestoreManaConsumeOneAndRefuseAtFullOverHttp() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var owner=store.provisionAccount();
    var base=OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.WARRIOR,"male",false,new java.util.Random(7)).progress();
    var healing=new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),"item.healing_potion",3,1);
    var mana=new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),"item.potion_of_mana",2,1);
    var buff=new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),"item.potion_of_fortitude",1,1);
    var hurt=ProgressEdits.withInventory(ProgressEdits.withVitals(base,
        new com.perso.T4C.online.model.CharacterProgress.Vitals(10,500,5,100)),
        java.util.stream.Stream.concat(base.inventory().stream(),java.util.stream.Stream.of(healing,mana,buff)).toList());
    var drinker=store.create(owner.accountId(),"HTTP drinker",0,30.5,30.5,hurt,0);
    var fullOwner=store.provisionAccount();
    var spare=new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),"item.potion_of_mana",1,1);
    var fullMana=ProgressEdits.withInventory(ProgressEdits.withVitals(base,
        new com.perso.T4C.online.model.CharacterProgress.Vitals(500,500,100,100)),
        java.util.stream.Stream.concat(base.inventory().stream(),java.util.stream.Stream.of(spare)).toList());
    var topped=store.create(fullOwner.accountId(),"HTTP topped",0,30.5,30.5,fullMana,0);
    String current=Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server=new PreviewServer(new PreviewMap(new boolean[60][80],0,0,30,30),0,store); var client=HttpClient.newHttpClient()) {
      server.start(); String url=server.url();
      String token=login(client,url,owner,drinker.id());
      long revision=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      // Healing Potion: its authored 50 points, exactly one unit of the stack, no exhaustion afterwards.
      var drunk=client.send(abilityRequest(url,"/use",token,current,revision+" 0 "+healing.id()),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,drunk.statusCode(),drunk.body());
      assertEquals(50,object(drunk).get("healthGained").getAsInt());
      var afterHeal=store.load(owner.accountId(),drinker.id());
      assertEquals(2,afterHeal.progress().inventory().stream().filter(i->i.id().equals(healing.id())).findFirst().orElseThrow().quantity());
      assertTrue(afterHeal.progress().vitals().hp()>=60);
      // Potion of Mana: the authored 25 mana, one unit, straight after the heal (potions carry no exhaustion).
      var restored=client.send(abilityRequest(url,"/use",token,current,afterHeal.revision()+" 0 "+mana.id()),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,restored.statusCode(),restored.body());
      assertEquals(25,object(restored).get("manaGained").getAsInt());
      var afterMana=store.load(owner.accountId(),drinker.id());
      assertEquals(1,afterMana.progress().inventory().stream().filter(i->i.id().equals(mana.id())).findFirst().orElseThrow().quantity());
      assertTrue(afterMana.progress().vitals().mana()>=30);
      // A buff potion has no server-side rule: refused with a definite code, nothing consumed.
      var refusedBuff=client.send(abilityRequest(url,"/use",token,current,afterMana.revision()+" 0 "+buff.id()),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,refusedBuff.statusCode()); assertTrue(refusedBuff.body().contains("unsupported_item")||refusedBuff.body().contains("no_effect"),refusedBuff.body());
      assertEquals(1,store.load(owner.accountId(),drinker.id()).progress().inventory().stream().filter(i->i.id().equals(buff.id())).findFirst().orElseThrow().quantity());
      // A full mana pool: the potion is refused as offline and keeps its unit.
      String toppedToken=login(client,url,fullOwner,topped.id());
      long toppedRevision=object(client.send(request(url,"/character",toppedToken,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      var refusedFull=client.send(abilityRequest(url,"/use",toppedToken,current,toppedRevision+" 0 "+spare.id()),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,refusedFull.statusCode()); assertTrue(refusedFull.body().contains("no_mana_needed"),refusedFull.body());
      assertEquals(1,store.load(fullOwner.accountId(),topped.id()).progress().inventory().stream().filter(i->i.id().equals(spare.id())).findFirst().orElseThrow().quantity());
    }
  }

  /** A character that died at the map centre: its droppable items wait on a corpse right beside it. */
  private record Corpse(OnlineStore.CharacterState dead,java.util.UUID id,java.util.List<com.perso.T4C.online.model.CharacterProgress.Item> items) {}

  private static Corpse dieBesideStart(OnlineStore store,OnlineStore.AccountKey account,String name,boolean overweight) throws Exception {
    var base=OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.WARRIOR,"male",false,new java.util.Random(3)).progress();
    var catalog=ItemCatalog.loadBundled();
    String ordinary=catalog.items().values().stream().filter(i->!i.undroppable()).map(i->i.key()).sorted().findFirst().orElseThrow();
    var inventory=new java.util.ArrayList<>(base.inventory());
    inventory.add(new com.perso.T4C.online.model.CharacterProgress.Item(java.util.UUID.randomUUID(),ordinary,1,7));
    var progress=new com.perso.T4C.online.model.CharacterProgress(base.version(),base.characterClass(),base.gender(),base.attributes(),
        base.vitals(),base.advancement(),inventory,base.equipment(),base.skills(),base.spells(),base.questFlags(),base.storage(),
        base.storageGold(),base.respawn(),base.buffs(),base.cooldowns(),base.hiddenUntil(),base.companion());
    var created=store.create(account.accountId(),name,0,30.5,30.5,progress,0);
    var session=store.acquire(account.secret(),created.id());
    try {
      var life=store.loadOrCreateSpawn("http-recover:"+java.util.UUID.randomUUID());
      var provoked=store.commitGameplayEvent(session,created.revision(),java.util.UUID.randomUUID(),0,0,created.progress(),
          new OnlineStore.CreatureProvocation(life.key(),life.eventId(),created.id(),0)).character();
      var here=new com.perso.T4C.online.model.CharacterProgress.Respawn(0,30.5,30.5);
      var death=OnlineDeath.prepare(provoked,catalog,here,here,new java.util.Random() { @Override public double nextDouble() { return 0; } });
      var attack=new OnlineStore.MonsterAttack(life.key(),life.eventId(),created.id(),0,System.currentTimeMillis()+1500);
      var corpseId=java.util.UUID.randomUUID();
      var dead=store.commitMonsterAttack(session,provoked.revision(),corpseId,death.progress(),attack,death.transition()).character();
      assertFalse(death.transition().droppedItems().isEmpty(),"the fixture must leave something on the corpse");
      if(overweight) {
        var fixture=OnlineOverweightFixture.size(dead.progress(),catalog);
        dead=store.commitGameplayEvent(session,dead.revision(),java.util.UUID.randomUUID(),0,0,
            OnlineOverweightFixture.apply(dead.progress(),fixture)).character();
      }
      return new Corpse(dead,corpseId,death.transition().droppedItems());
    } finally { store.release(session); }
  }

  /** A death moves the character, so its location epoch is no longer zero. */
  private long epochOf(HttpClient client,String url,String token) throws Exception {
    return object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("locationEpoch").getAsLong();
  }

  /** The corpse list is read right after joining; retry only while the session has not seen it yet. */
  private HttpResponse<String> recoverWhenKnown(HttpClient client,String url,String token,String current,String body) throws Exception {
    HttpResponse<String> response=null;
    // Listing the corpses is what makes the session aware of them, as the client does.
    assertEquals(200,client.send(request(url,"/corpses",token,null),HttpResponse.BodyHandlers.ofString()).statusCode());
    for(int attempt=0;attempt<40;attempt++) {
      response=client.send(abilityRequest(url,"/recover",token,current,body),HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()!=409 || !response.body().contains("corpse_unavailable")) return response;
      Thread.sleep(100);
    }
    return response;
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void recoverHttpReportsTheRealCorpseRefusalInsteadOfBusy() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate();
    var owner=store.provisionAccount(); var stranger=store.provisionAccount(); var heavyAccount=store.provisionAccount();
    var corpse=dieBesideStart(store,owner,"HTTP recover owner",false);
    var other=store.create(stranger.accountId(),"HTTP recover stranger",0,30.5,30.5,
        OnlineCharacterCreator.prepare(com.perso.T4C.helper.CharacterClass.WARRIOR,"male",false,new java.util.Random(7)).progress(),0);
    var heavyCorpse=dieBesideStart(store,heavyAccount,"HTTP recover heavy",true);
    String current=Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server=new PreviewServer(new PreviewMap(new boolean[60][80],0,0,30,30),0,store); var client=HttpClient.newHttpClient()) {
      server.start(); String url=server.url();
      String token=login(client,url,owner,corpse.dead().id());
      long revision=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      // Another player's corpse is not visible to this session.
      String foreign=login(client,url,stranger,other.id());
      long foreignRevision=object(client.send(request(url,"/character",foreign,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      var notYours=client.send(abilityRequest(url,"/recover",foreign,current,foreignRevision+" "+epochOf(client,url,foreign)+" "+corpse.id()),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,notYours.statusCode(),notYours.body()); assertTrue(notYours.body().contains("corpse_unavailable"),notYours.body());
      // An item that is not on the corpse is a definite refusal, not a busy server.
      var missing=recoverWhenKnown(client,url,token,current,revision+" "+epochOf(client,url,token)+" "+corpse.id()+" "+java.util.UUID.randomUUID());
      assertEquals(409,missing.statusCode(),missing.body()); assertTrue(missing.body().contains("item_unavailable"),missing.body());
      assertEquals(200,client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString()).statusCode(),"A refusal keeps the player connected");
      assertEquals(corpse.dead(),store.load(owner.accountId(),corpse.dead().id()),"The refusal changed nothing");
      // A character already over its carry limit cannot take a stack.
      String heavyToken=login(client,url,heavyAccount,heavyCorpse.dead().id());
      long heavyRevision=object(client.send(request(url,"/character",heavyToken,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      var heavyBefore=store.load(heavyAccount.accountId(),heavyCorpse.dead().id());
      var tooHeavy=recoverWhenKnown(client,url,heavyToken,current,heavyRevision+" "+epochOf(client,url,heavyToken)+" "+heavyCorpse.id()+" "+heavyCorpse.items().getFirst().id());
      assertEquals(409,tooHeavy.statusCode(),tooHeavy.body()); assertTrue(tooHeavy.body().contains("too_heavy"),tooHeavy.body());
      assertEquals(heavyBefore,store.load(heavyAccount.accountId(),heavyCorpse.dead().id()),"The refusal changed nothing");
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void recoverHttpStillAnswersBusyWhenTheCommitGenuinelyTimesOut() throws Exception {
    var admin=OnlineStore.fromEnvironment(); admin.migrate();
    var owner=admin.provisionAccount();
    var corpse=dieBesideStart(admin,owner,"HTTP recover slow",false);
    var slow=new java.util.concurrent.atomic.AtomicBoolean();
    var properties=new java.util.Properties();
    properties.setProperty("user",System.getenv("T4C_DB_USER")); properties.setProperty("password",System.getenv("T4C_DB_PASSWORD"));
    var store=new OnlineStore(() -> {
      if(slow.get()) { try { Thread.sleep(2600); } catch(InterruptedException e) { Thread.currentThread().interrupt(); } }
      return java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL"),properties);
    });
    String current=Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION);
    try (var server=new PreviewServer(new PreviewMap(new boolean[60][80],0,0,30,30),0,store); var client=HttpClient.newHttpClient()) {
      server.start(); String url=server.url();
      String token=login(client,url,owner,corpse.dead().id());
      long revision=object(client.send(request(url,"/character",token,null),HttpResponse.BodyHandlers.ofString())).get("revision").getAsLong();
      assertEquals(200,client.send(request(url,"/corpses",token,null),HttpResponse.BodyHandlers.ofString()).statusCode());
      long epoch=epochOf(client,url,token);
      slow.set(true);
      var response=client.send(HttpRequest.newBuilder(URI.create(url+"/recover")).timeout(Duration.ofSeconds(8))
          .header("Origin",url).header("Content-Type","text/plain")
          .header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER,current).header("Authorization","Bearer "+token)
          .POST(HttpRequest.BodyPublishers.ofString(revision+" "+epoch+" "+corpse.id())).build(),HttpResponse.BodyHandlers.ofString());
      slow.set(false);
      assertEquals(503,response.statusCode(),response.body()); assertTrue(response.body().contains("busy"),response.body());
    }
  }

  private String login(HttpClient client,String url,OnlineStore.AccountKey key,java.util.UUID character) throws Exception {
    return join(client,url,key,character).get("token").getAsString();
  }

  private com.google.gson.JsonObject join(HttpClient client,String url,OnlineStore.AccountKey key,java.util.UUID character) throws Exception {
    var response=client.send(request(url,"/join",null,key.secret()+" "+character),HttpResponse.BodyHandlers.ofString());
    assertEquals(200,response.statusCode());
    return object(response);
  }

  private static com.google.gson.JsonObject lootEntry(com.google.gson.JsonObject state,java.util.UUID id) {
    for (var element:state.getAsJsonArray("loot"))
      if (element.getAsJsonObject().get("id").getAsString().equals(id.toString())) return element.getAsJsonObject();
    throw new AssertionError("Loot "+id+" is not listed");
  }
}
