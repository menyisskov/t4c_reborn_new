package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRoster;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OnlineAccountClientTest {
  private static final String KEY="a".repeat(64);

  @Test void readsBoundedPagesInPostgresUuidOrderAndRechecksProtocolBeforeCredentials() throws Exception {
    try(var fixture=new Fixture();var client=new OnlineAccountClient(fixture.uri(),KEY)) {
      var page=client.page(null);
      assertEquals(20,page.characters().size());
      assertEquals(new UUID(Long.MIN_VALUE,0),page.nextAfter(),"Unsigned PostgreSQL order crosses Java's signed UUID boundary");
      assertThrows(UnsupportedOperationException.class,()->page.characters().clear());
      var finalPage=client.page(page.nextAfter());assertEquals(1,finalPage.characters().size());assertNull(finalPage.nextAfter());
      assertEquals(List.of("", "after="+page.nextAfter()),fixture.queries);
      assertEquals(2,fixture.accountRequests.get());assertEquals(0,fixture.credentialedHandshakes.get());
      assertEquals(0,fixture.unexpectedPaths.get());
      fixture.version=999;
      assertThrows(IOException.class,()->client.page(null));
      assertEquals(2,fixture.accountRequests.get(),"Restarted incompatible server must not receive credentials");
      fixture.version=OnlineProtocol.VERSION;fixture.persistent=false;
      assertThrows(IOException.class,()->client.page(null));assertEquals(2,fixture.accountRequests.get());
    }
  }

  @Test void rejectsRedirectsOversizedBodiesAndNonAdvancingPagesWithoutEchoingServerSecrets() throws Exception {
    try(var fixture=new Fixture();var client=new OnlineAccountClient(fixture.uri(),KEY)) {
      fixture.mode="redirect";
      assertEquals(302,assertThrows(OnlineAccountClient.Rejected.class,()->client.page(null)).statusCode());
      assertEquals(0,fixture.redirectRequests.get());
      fixture.mode="unauthorized";
      var rejected=assertThrows(OnlineAccountClient.Rejected.class,()->client.page(null));
      assertEquals(401,rejected.statusCode());assertFalse(rejected.getMessage().contains(KEY));assertNull(rejected.getCause());
      fixture.mode="oversized";
      assertThrows(IOException.class,()->client.page(null));
      fixture.mode="normal";
      assertThrows(IOException.class,()->client.page(new UUID(Long.MIN_VALUE,2)),"A page cannot move behind its requested cursor");
      fixture.mode="stalled";
      assertTimeoutPreemptively(java.time.Duration.ofSeconds(5),
          ()->assertThrows(IOException.class,()->client.page(null)),"The whole body transfer has a deadline");
    }
  }

  @Test void rejectsPlaintextRemoteEndpointsAndDoesNotRequestAfterClose() throws Exception {
    assertThrows(IllegalArgumentException.class,()->new OnlineAccountClient(URI.create("http://example.org"),KEY));
    try(var fixture=new Fixture()) {
      var client=new OnlineAccountClient(fixture.uri(),KEY);client.close();
      assertThrows(IOException.class,()->client.page(null));assertEquals(0,fixture.accountRequests.get());
    }
  }

  @Test void createsWithAuthenticatedJsonAfterAnonymousGateAndValidatesReply() throws Exception {
    var request=new OnlineCharacterCreation.Request(UUID.randomUUID(),"Paladin","paladin","female",false);
    try(var fixture=new Fixture();var client=new OnlineAccountClient(fixture.uri(),KEY)) {
      fixture.version=999;
      assertThrows(IOException.class,()->client.create(request));
      assertEquals(0,fixture.creationRequests.get(),"Incompatible map must not receive the account key or mutation");
      assertEquals(0,fixture.credentialedHandshakes.get());
      fixture.version=OnlineProtocol.VERSION;fixture.persistent=false;
      assertThrows(IOException.class,()->client.create(request));
      assertEquals(0,fixture.creationRequests.get());
      fixture.persistent=true;
      var created=client.create(request);
      assertEquals(request.requestId(),created.requestId());
      assertEquals(fixture.createdId,created.character().id());
      assertEquals("paladin",created.character().characterClass());
      assertEquals(1,fixture.creationRequests.get());
      var posted=com.google.gson.JsonParser.parseString(fixture.creationBodies.getFirst()).getAsJsonObject();
      assertEquals(Set.of("requestId","name","characterClass","gender","evil"),posted.keySet());
      assertEquals(request.requestId().toString(),posted.get("requestId").getAsString());
      assertEquals("Paladin",posted.get("name").getAsString());
      assertEquals("paladin",posted.get("characterClass").getAsString());
      assertEquals("female",posted.get("gender").getAsString());
      assertFalse(posted.get("evil").getAsBoolean());
      fixture.createMode="wrongId";
      assertThrows(IOException.class,()->client.create(new OnlineCharacterCreation.Request(
          UUID.randomUUID(),"Other","warrior","male",false)));
      fixture.createMode="reject";
      var refused=assertThrows(OnlineAccountClient.Rejected.class,()->client.create(new OnlineCharacterCreation.Request(
          UUID.randomUUID(),"Other","warrior","male",false)));
      assertEquals(409,refused.statusCode());assertFalse(refused.getMessage().contains(KEY));
      assertNull(refused.code()); // A non-code body (here an echoed secret) is never surfaced.
      fixture.createMode="taken";
      var taken=assertThrows(OnlineAccountClient.Rejected.class,()->client.create(new OnlineCharacterCreation.Request(
          UUID.randomUUID(),"Other","warrior","male",false)));
      assertEquals(409,taken.statusCode());assertEquals("name_taken",taken.code());
    }
  }

  @Test void uncertainCreateNeverRetriesUntilCallerReusesTheSameRequest() throws Exception {
    var request=new OnlineCharacterCreation.Request(UUID.randomUUID(),"Warrior","warrior","male",false);
    try(var fixture=new Fixture();var client=new OnlineAccountClient(fixture.uri(),KEY)) {
      fixture.createMode="lost";
      assertTimeoutPreemptively(java.time.Duration.ofSeconds(5),
          ()->assertThrows(IOException.class,()->client.create(request)));
      assertEquals(1,fixture.creationRequests.get(),"An uncertain mutation must not be sent again automatically");
      fixture.createMode="normal";
      var retried=client.create(request); // Explicit user retry retains the same idempotency identity.
      assertEquals(request.requestId(),retried.requestId());
      assertEquals(fixture.createdId,retried.character().id());
      assertEquals(2,fixture.creationRequests.get());
      assertEquals(fixture.creationBodies.getFirst(),fixture.creationBodies.getLast());
    }
  }

  private static final class Fixture implements AutoCloseable {
    final HttpServer server;
    final AtomicInteger accountRequests=new AtomicInteger(),credentialedHandshakes=new AtomicInteger(),
        unexpectedPaths=new AtomicInteger(),redirectRequests=new AtomicInteger(),creationRequests=new AtomicInteger();
    final List<String> queries=new CopyOnWriteArrayList<>(),creationBodies=new CopyOnWriteArrayList<>();
    final UUID createdId=UUID.randomUUID();
    volatile int version=OnlineProtocol.VERSION;
    volatile boolean persistent=true;
    volatile String mode="normal";
    volatile String createMode="normal";
    Fixture() throws IOException {
      server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
      server.createContext("/",exchange->{
        int status=200;String body;String path=exchange.getRequestURI().getPath();
        if(path.equals("/map")) {
          if(exchange.getRequestHeaders().getFirst("Authorization")!=null) credentialedHandshakes.incrementAndGet();
          body="{\"protocolVersion\":"+version+",\"persistent\":"+persistent+"}";
        } else if(path.equals("/account/characters") && exchange.getRequestMethod().equals("POST")) {
          creationRequests.incrementAndGet();
          String requestBody=new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
          creationBodies.add(requestBody);
          if(!("Bearer "+KEY).equals(exchange.getRequestHeaders().getFirst("Authorization"))
              || !Integer.toString(OnlineProtocol.VERSION).equals(exchange.getRequestHeaders().getFirst(OnlineProtocol.VERSION_HEADER))
              || !"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))
              || !uri().toString().equals(exchange.getRequestHeaders().getFirst("Origin"))) {
            status=401;body="bad auth";
          } else if(createMode.equals("reject")) {status=409;body=KEY;}
          else if(createMode.equals("taken")) {status=409;body="{\"error\":\"name_taken\"}";}
          else {
            var request=new Gson().fromJson(requestBody,OnlineCharacterCreation.Request.class);
            UUID replyId=createMode.equals("wrongId") ? UUID.randomUUID() : request.requestId();
            var summary=new OnlineRoster.CharacterSummary(createdId,request.name(),request.characterClass(),
                request.gender(),1,0,"ready");
            body=new Gson().toJson(new OnlineCharacterCreation.Result(replyId,summary));
            if(createMode.equals("lost")) {
              exchange.sendResponseHeaders(200,100);
              try(var output=exchange.getResponseBody()) {output.write('{');output.flush();}
              catch(IOException expected) { /* Deliberately incomplete response after a committed request. */ }
              return;
            }
          }
        } else if(path.equals("/account/characters")) {
          accountRequests.incrementAndGet();
          if(!("Bearer "+KEY).equals(exchange.getRequestHeaders().getFirst("Authorization"))
              || !Integer.toString(OnlineProtocol.VERSION).equals(exchange.getRequestHeaders().getFirst(OnlineProtocol.VERSION_HEADER))) {
            status=401;body="bad auth";
          } else {
            String query=exchange.getRequestURI().getRawQuery();queries.add(query==null ? "" : query);
            if(mode.equals("redirect")) { status=302;exchange.getResponseHeaders().set("Location",uri()+"/redirect-target");body=""; }
            else if(mode.equals("unauthorized")) { status=401;body=KEY; }
            else if(mode.equals("oversized")) body="x".repeat(64*1024+1);
            else {
              var entries=new ArrayList<OnlineRoster.CharacterSummary>();
              if(query==null) {
                for(int i=0;i<19;i++) entries.add(summary(new UUID(Long.MAX_VALUE,i)));
                entries.add(summary(new UUID(Long.MIN_VALUE,0)));
              } else entries.add(summary(new UUID(Long.MIN_VALUE,1)));
              body=new Gson().toJson(new OnlineRoster(entries,query==null ? entries.getLast().id() : null));
            }
          }
        } else if(path.equals("/redirect-target")) { redirectRequests.incrementAndGet();body="{}"; }
        else { unexpectedPaths.incrementAndGet();status=404;body="{}"; }
        if(path.equals("/account/characters") && mode.equals("stalled")) {
          exchange.sendResponseHeaders(200,100);
          try(var output=exchange.getResponseBody()) {
            output.write('{');output.flush();
            try { Thread.sleep(3000); } catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); }
          } catch(IOException cancelled) { /* The bounded client cancels this deliberately incomplete body. */ }
          return;
        }
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status,bytes.length==0 ? -1 : bytes.length);
        try(var output=exchange.getResponseBody()) { if(bytes.length>0) output.write(bytes); }
      });
      server.start();
    }
    private static OnlineRoster.CharacterSummary summary(UUID id) {
      return new OnlineRoster.CharacterSummary(id,"Account character","WARRIOR","male",1,0,"ready");
    }
    URI uri() { return URI.create("http://127.0.0.1:"+server.getAddress().getPort()); }
    @Override public void close() { server.stop(0); }
  }
}
