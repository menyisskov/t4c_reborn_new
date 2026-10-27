package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.*;
import com.perso.T4C.server.persistence.OnlineStore;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class AccountRosterTest {
  @Test void peerBudgetRecoversAndMemoryStaysBoundedUnderNewPeerSpray() {
    var now=new java.util.concurrent.atomic.AtomicLong();var limiter=new AccountRosterService.PeerLimiter(now::get);
    for(int i=0;i<40;i++) limiter.admit("peer");
    assertEquals(429,assertThrows(PreviewWorld.Rejected.class,()->limiter.admit("peer")).status);
    now.set(TimeUnit.SECONDS.toNanos(1));assertThrows(PreviewWorld.Rejected.class,()->limiter.admit("peer"));
    now.set(TimeUnit.SECONDS.toNanos(2));limiter.admit("peer");
    for(int i=1;i<1024;i++) limiter.admit("peer"+i);
    assertEquals(429,assertThrows(PreviewWorld.Rejected.class,()->limiter.admit("extra")).status);
    now.addAndGet(TimeUnit.MINUTES.toNanos(6));limiter.admit("extra");
  }

  @Test void blockedAccountReadsHaveBoundedSeparateWorkersAndFailBusyWithoutUnboundedQueue() throws Exception {
    var entered=new CountDownLatch(2);var release=new CountDownLatch(1);
    var store=new OnlineStore(()-> {
      entered.countDown();try { if(!release.await(3,TimeUnit.SECONDS)) throw new java.sql.SQLException("Test timeout"); }
      catch(InterruptedException e) { Thread.currentThread().interrupt();throw new java.sql.SQLException(e); }
      throw new java.sql.SQLException("Test unavailable");
    });
    try(var service=new AccountRosterService(store,HostedRegionPlan.single(new PreviewMap(new boolean[8][8],0,0,3,3)))) {
      var pending=new ArrayList<CompletableFuture<OnlineRoster>>();
      for(int i=0;i<22;i++) pending.add(service.request("peer","0".repeat(64),null));
      assertTrue(entered.await(1,TimeUnit.SECONDS));
      assertEquals(503,assertThrows(PreviewWorld.Rejected.class,()->service.request("peer","0".repeat(64),null)).status);
      release.countDown();for(var result:pending) assertThrows(ExecutionException.class,()->result.get(2,TimeUnit.SECONDS));
    } finally { release.countDown(); }
  }

  @Test void closeCancelsQueuedAndRunningReadsAndWaitsForWorkerExit() throws Exception {
    var entered=new CountDownLatch(1);var exited=new CountDownLatch(1);
    var store=new OnlineStore(()-> {
      entered.countDown();try { new CountDownLatch(1).await();throw new java.sql.SQLException("Unexpected release"); }
      catch(InterruptedException stopping) { Thread.currentThread().interrupt();throw new java.sql.SQLException(stopping); }
      finally { exited.countDown(); }
    });
    try(var service=new AccountRosterService(store,HostedRegionPlan.single(new PreviewMap(new boolean[8][8],0,0,3,3)))) {
    var reads=new ArrayList<CompletableFuture<OnlineRoster>>();
    for(int i=0;i<4;i++) reads.add(service.request("peer","0".repeat(64),null));
    var auth=service.authenticate("peer","0".repeat(64));
    assertTrue(entered.await(1,TimeUnit.SECONDS));service.close();service.close();
    assertEquals(0,exited.getCount());
    for(var read:reads) assertTrue(read.isCompletedExceptionally());
    assertTrue(auth.isCompletedExceptionally());
    assertEquals(503,assertThrows(PreviewWorld.Rejected.class,()->service.request("peer","0".repeat(64),null)).status);
    }
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void authenticatedPagesIncludeLegacyRowsWithoutLeasesOrForeignStateAndRotationRevokesReads() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var owner=store.provisionAccount();var other=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2));
    var expected=new HashSet<UUID>();UUID legacy=null,playable=null;
    for(int i=0;i<21;i++) {
      var created=store.create(owner.accountId(),"Roster "+i,0,i==0 ? 3.5 : 2944.5,i==0 ? 3.5 : 1059.5,i==0 ? null : prepared.progress(),0);
      expected.add(created.id());if(i==0) legacy=created.id();else playable=created.id();
    }
    var heldLease=store.acquire(owner.secret(),playable);
    var foreign=store.create(other.accountId(),"Foreign roster",0,3.5,3.5,null,0);
    // Use the real plan so prepared default respawn is available. Legacy positions remain listed.
    var plan=HostedRegionPlan.startingSlice(java.nio.file.Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=HttpClient.newHttpClient()) {
      server.start();String url=server.url();
      assertEquals(426,client.send(request(url,null,null,"999"),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(401,client.send(request(url,"0".repeat(64),null,version()),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(400,client.send(request(url,owner.secret(),"after=bad&after=bad",version()),HttpResponse.BodyHandlers.ofString()).statusCode());
      var firstResponse=client.send(request(url,owner.secret(),null,version()),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,firstResponse.statusCode());var first=new Gson().fromJson(firstResponse.body(),OnlineRoster.class);
      assertEquals(20,first.characters().size());assertNotNull(first.nextAfter());
      var secondResponse=client.send(request(url,owner.secret(),"after="+first.nextAfter(),version()),HttpResponse.BodyHandlers.ofString());
      assertEquals(200,secondResponse.statusCode());var second=new Gson().fromJson(secondResponse.body(),OnlineRoster.class);
      assertEquals(1,second.characters().size());assertNull(second.nextAfter());
      var all=new ArrayList<>(first.characters());all.addAll(second.characters());
      assertEquals(expected,all.stream().map(OnlineRoster.CharacterSummary::id).collect(java.util.stream.Collectors.toSet()));
      final var playableId=playable;assertEquals("in_use",all.stream().filter(c->c.id().equals(playableId)).findFirst().orElseThrow().availability());
      assertTrue(all.stream().anyMatch(c->c.availability().equals("ready")));
      final var legacyId=legacy;assertEquals("uninitialized",all.stream().filter(c->c.id().equals(legacyId)).findFirst().orElseThrow().availability());
      for(String response:List.of(firstResponse.body(),secondResponse.body())) {
        assertFalse(response.contains(foreign.id().toString()));assertFalse(response.contains(owner.accountId().toString()));
        assertFalse(response.contains(owner.secret()));assertFalse(response.contains("session_id"));assertFalse(response.contains("inventory"));
      }
      store.release(heldLease);var lease=store.acquire(owner.secret(),playable);store.release(lease); // Listing acquired no play lease.
      var replacement=store.rotateAccountKey(owner.accountId());
      assertEquals(401,client.send(request(url,owner.secret(),null,version()),HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(200,client.send(request(url,replacement.secret(),null,version()),HttpResponse.BodyHandlers.ofString()).statusCode());
    }
  }
  private static String version() { return Integer.toString(OnlineProtocol.VERSION); }
  private static HttpRequest request(String url,String key,String query,String protocol) {
    var builder=HttpRequest.newBuilder(URI.create(url+"/account/characters"+(query==null ? "" : "?"+query)))
        .timeout(java.time.Duration.ofSeconds(5)).header(OnlineProtocol.VERSION_HEADER,protocol);
    if(key!=null) builder.header("Authorization","Bearer "+key);return builder.build();
  }
}
