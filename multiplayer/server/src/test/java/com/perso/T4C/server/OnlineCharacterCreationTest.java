package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.server.persistence.OnlineStore;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineCharacterCreationTest {
  private static final Gson JSON=new Gson();

  @Test void authenticatedCreationIsStrictIdempotentAndDoesNotAcquireGameplayLease() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var owner=store.provisionAccount();var other=store.provisionAccount();
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());
        var client=HttpClient.newHttpClient()) {
      server.start();String url=server.url(),id=UUID.randomUUID().toString();
      String body="{\"requestId\":\""+id+"\",\"name\":\"  Hero  \",\"characterClass\":\"warrior\",\"gender\":\"male\",\"evil\":false}";
      assertEquals(426,client.send(post(url,owner.secret(),body,null,"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(415,client.send(post(url,owner.secret(),body,version(),"text/plain"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(401,client.send(post(url,"0".repeat(64),body,version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      var first=client.send(post(url,owner.secret(),body,version(),"application/json"),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,first.statusCode(),first.body());
      var result=JSON.fromJson(first.body(),OnlineCharacterCreation.Result.class);
      assertEquals(UUID.fromString(id),result.requestId());assertEquals("Hero",result.character().name());
      assertEquals("warrior",result.character().characterClass());assertEquals("ready",result.character().availability());
      var replay=client.send(post(url,owner.secret(),body,version(),"application/json"),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,replay.statusCode());
      assertEquals(result.character().id(),JSON.fromJson(replay.body(),OnlineCharacterCreation.Result.class).character().id());
      String changed=body.replace("Hero","Other");
      var conflict=client.send(post(url,owner.secret(),changed,version(),"application/json"),HttpResponse.BodyHandlers.ofString());
      assertEquals(409,conflict.statusCode());assertTrue(conflict.body().contains("creation_conflict"));
      assertEquals(400,client.send(post(url,owner.secret(),body.substring(0,body.length()-1)+",\"gold\":999}",version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(post(url,owner.secret(),body.replace("\"evil\":false","\"evil\":\"false\""),version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(post(url,owner.secret(),body.replace("\"warrior\"","\"archer\""),version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(post(url,owner.secret(),body.replace("\"evil\":false","\"evil\":false,\"evil\":true"),version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(HttpRequest.newBuilder(URI.create(url+"/account/characters?x=1"))
          .timeout(Duration.ofSeconds(5)).header("Content-Type","application/json")
          .header("Origin",url)
          .header(OnlineProtocol.VERSION_HEADER,version()).header("Authorization","Bearer "+owner.secret())
          .POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString()).statusCode());
      var saved=store.load(owner.accountId(),result.character().id());
      assertNotNull(saved);assertEquals(0,saved.revision());assertNotNull(saved.progress());
      var start=plan.regions().getFirst().map();
      assertEquals(start.originX+start.spawnX,saved.x());
      assertEquals(start.originY+start.spawnY,saved.y());
      var lease=store.acquire(owner.secret(),saved.id());
      var whileHeld=client.send(post(url,owner.secret(),body,version(),"application/json"),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,whileHeld.statusCode());
      var held=JSON.fromJson(whileHeld.body(),OnlineCharacterCreation.Result.class);
      assertEquals(saved.id(),held.character().id());
      assertEquals("in_use",held.character().availability());
      assertEquals(saved,store.load(owner.accountId(),saved.id()),"Replay cannot reroll kit or advance revision");
      store.release(lease);
      assertThrows(OnlineStore.Conflict.class,()->store.load(other.accountId(),saved.id()));
      var rotated=store.rotateAccountKey(owner.accountId());
      assertEquals(401,client.send(post(url,owner.secret(),body,version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(200,client.send(post(url,rotated.secret(),body,version(),"application/json"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertFalse(first.body().contains(owner.secret()));assertFalse(first.body().contains(owner.accountId().toString()));
      assertFalse(first.body().contains("inventory"));assertFalse(first.body().contains("gold"));
    }
  }

  @Test void invalidKeyIsRejectedBeforeHeldCreationBody() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var owner=store.provisionAccount();
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local())) {
      server.start();var uri=URI.create(server.url());
      try(var socket=heldCreation(uri,server.url(),"0".repeat(64))) {
        socket.setSoTimeout(2500);
        String line=new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream(),StandardCharsets.US_ASCII)).readLine();
        assertTrue(line.contains("401"),line);
      }
      try(var one=heldCreation(uri,server.url(),owner.secret());
          var two=heldCreation(uri,server.url(),owner.secret());
          var client=HttpClient.newHttpClient()) {
        // These two authenticated requests occupy the account body permits without sending bytes.
        // A third invalid key must be rejected at admission (503), before its DB auth could return 401.
        Thread.sleep(500);
        var third=client.send(post(server.url(),"0".repeat(64),"{}",version(),"application/json"),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(503,third.statusCode(),third.body());
      }
    }
  }

  private static Socket heldCreation(URI uri,String origin,String key) throws Exception {
    var socket=new Socket(uri.getHost(),uri.getPort());
    var writer=new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.US_ASCII);
    writer.write("POST /account/characters HTTP/1.1\r\nHost: "+uri.getHost()+":"+uri.getPort()
        +"\r\nOrigin: "+origin+"\r\nContent-Type: application/json\r\n"+OnlineProtocol.VERSION_HEADER+": "+version()
        +"\r\nAuthorization: Bearer "+key+"\r\nContent-Length: 512\r\n\r\n");
    writer.flush();
    return socket;
  }

  private static String version() { return Integer.toString(OnlineProtocol.VERSION); }
  private static HttpRequest post(String url,String key,String body,String protocol,String type) {
    var builder=HttpRequest.newBuilder(URI.create(url+"/account/characters")).timeout(Duration.ofSeconds(5))
        .header("Content-Type",type).header("Origin",url).header("Authorization","Bearer "+key);
    if(protocol!=null) builder.header(OnlineProtocol.VERSION_HEADER,protocol);
    return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
  }
}
