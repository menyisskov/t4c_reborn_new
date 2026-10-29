package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.server.persistence.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class HostedTransferTest {
  @Test void transferReceiptEpochAndLeaseSurviveRestartWithoutReplayingOrLosingProgress() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var created=store.create(account.accountId(),"Transfer receipt",0,2931.5,1056.5,p,10);
    var session=store.acquire(account.secret(),created.id());
    var draft=new PortalTransfer(16,"start","lighthaven-interior",new CharacterProgress.Respawn(0,2931.5,1056.5),
        new CharacterProgress.Respawn(1,304,383),0,System.currentTimeMillis()+350);
    var id=UUID.randomUUID();
    assertThrows(OnlineStore.Conflict.class,()->store.commitTransfer(session,1,id,draft));
    assertEquals(created,store.load(account.accountId(),created.id()));
    var moved=store.commitTransfer(session,0,id,draft).character();
    assertEquals(1,moved.world());assertEquals(1,moved.locationEpoch());assertEquals(p,moved.progress());
    store.release(session);var restarted=OnlineStore.fromEnvironment();var recovered=restarted.acquire(account.secret(),created.id());
    assertFalse(restarted.commitTransfer(recovered,0,id,draft).applied());
    var changed=new PortalTransfer(16,"start","lighthaven-interior",draft.source(),new CharacterProgress.Respawn(1,305,383),0,draft.cooldownUntil());
    assertThrows(OnlineStore.Conflict.class,()->restarted.commitTransfer(recovered,moved.revision(),id,changed));
    assertThrows(OnlineStore.Conflict.class,()->restarted.commitTransfer(session,moved.revision(),UUID.randomUUID(),draft));
    assertEquals(moved,restarted.load(account.accountId(),created.id()));restarted.release(recovered);
  }

  @Test void twoPlayersTransferIndependentlyKeepIdentityRequireAcknowledgementAndReconnectAtDestination() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var first=store.create(account.accountId(),"Portal first",0,2944.5,1059.5,p,0);
    var second=store.create(account.accountId(),"Portal second",0,2945.5,1059.5,p,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      var gson=new com.google.gson.Gson();var invalid=gson.toJsonTree(p).getAsJsonObject();
      invalid.add("respawn",gson.toJsonTree(new CharacterProgress.Respawn(999,1,1)));
      var unsupported=store.create(account.accountId(),"Unhosted respawn",0,2944.5,1059.5,gson.fromJson(invalid,CharacterProgress.class),0);
      var unsupportedSession=store.acquire(account.secret(),unsupported.id());
      assertEquals("unavailable_respawn",assertThrows(PreviewWorld.Rejected.class,()->hosted.sessions.join(unsupportedSession,System.nanoTime())).getMessage());
      store.release(unsupportedSession);
      var a=hosted.sessions.join(store.acquire(account.secret(),first.id()),System.nanoTime());
      var b=hosted.sessions.join(store.acquire(account.secret(),second.id()),System.nanoTime());
      hosted.sessions.regionReady(a.token(),0);hosted.sessions.regionReady(b.token(),0);
      var start=hosted.actor(a.token());start.world.relocate(a.token(),2931.5-start.map.originX,1056.5-start.map.originY);
      await(hosted,()->hosted.sessions.region(a.token()).world()==1);
      assertEquals(a.id(),hosted.actor(a.token()).world.position(a.token()).id());
      assertEquals(1,hosted.actor(a.token()).world.snapshot(a.token()).players().size());
      assertEquals(b.id(),hosted.actor(b.token()).world.snapshot(b.token()).players().getFirst().id());
      assertFalse(hosted.sessions.belongs(a.token(),hosted.actor(b.token()).world));
      assertThrows(PreviewWorld.Rejected.class,()->hosted.sessions.requireEpoch(a.token(),0));
      assertThrows(PreviewWorld.Rejected.class,()->hosted.sessions.regionReady(a.token(),0));
      var dungeon=hosted.actor(a.token());var before=dungeon.world.position(a.token());
      dungeon.world.input(a.token(),1,1,0);hosted.step();
      assertEquals(before.x(),dungeon.world.position(a.token()).x(),"Map loading cannot move the frozen actor");
      hosted.sessions.regionReady(a.token(),1);
      // Capacity belongs to the realm even when the first two actors occupy different worlds.
      var fillers=new ArrayList<String>();
      for(int i=2;i<PreviewWorld.MAX_PLAYERS;i++) {
        var extra=store.create(account.accountId(),"Capacity "+i,0,2944.5,1059.5,p,0);
        fillers.add(hosted.sessions.join(store.acquire(account.secret(),extra.id()),System.nanoTime()).token());
      }
      var excess=store.create(account.accountId(),"Capacity excess",0,2944.5,1059.5,p,0);
      var excessSession=store.acquire(account.secret(),excess.id());
      assertThrows(PreviewWorld.Rejected.class,()->hosted.sessions.join(excessSession,System.nanoTime()));store.release(excessSession);
      for(var token:fillers) { var done=hosted.sessions.leave(token);await(hosted,done::isDone);done.get(); }
      var left=hosted.sessions.leave(a.token());await(hosted,left::isDone);left.get();
      var loaded=store.load(account.accountId(),first.id());assertEquals(1,loaded.world());assertEquals(1,loaded.locationEpoch());
      var rejoined=hosted.sessions.join(store.acquire(account.secret(),first.id()),System.nanoTime());
      assertEquals(1,hosted.sessions.region(rejoined.token()).world());hosted.sessions.regionReady(rejoined.token(),1);
      var returned=hosted.actor(rejoined.token());returned.world.relocate(rejoined.token(),306-returned.map.originX,381-returned.map.originY);
      await(hosted,()->hosted.sessions.region(rejoined.token()).world()==0);
      assertEquals(2,hosted.sessions.region(rejoined.token()).locationEpoch());
      assertEquals(2,hosted.actor(b.token()).world.snapshot(b.token()).players().size());
      hosted.sessions.regionReady(rejoined.token(),2);
    }
  }

  @Test void caveMouthLeadsIntoTheSkeletonCaveAndItsOnlyExitReturnsToTheKillCircle() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var created=store.create(account.accountId(),"Cave walker",0,2944.5,1059.5,p,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      var joined=hosted.sessions.join(store.acquire(account.secret(),created.id()),System.nanoTime());
      hosted.sessions.regionReady(joined.token(),0);
      var start=hosted.actor(joined.token());
      start.world.relocate(joined.token(),2770.5-start.map.originX,1094.5-start.map.originY);
      await(hosted,()->hosted.sessions.region(joined.token()).world()==1);
      assertEquals(HostedRegionPlan.CAVE_KEY,hosted.sessions.region(joined.token()).key());
      assertEquals(1,hosted.sessions.region(joined.token()).locationEpoch());
      hosted.sessions.regionReady(joined.token(),1);
      var cave=hosted.actor(joined.token());
      var at=cave.world.position(joined.token());
      assertEquals(134,Math.floor(at.x()+cave.map.originX),"Lands on the authored cave tile");assertEquals(667,Math.floor(at.y()+cave.map.originY));
      cave.world.relocate(joined.token(),136.5-cave.map.originX,665.5-cave.map.originY);
      await(hosted,()->hosted.sessions.region(joined.token()).world()==0);
      assertEquals(2,hosted.sessions.region(joined.token()).locationEpoch());
      var back=hosted.actor(joined.token()).world.position(joined.token());
      assertEquals(2772,Math.floor(back.x()+hosted.actor(joined.token()).map.originX));
      var left=hosted.sessions.leave(joined.token());await(hosted,left::isDone);left.get();
    }
  }

  @Test void sewerStairsCarryAWalkerDownThroughEveryLevelAndBackUpToTheInterior() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var created=store.create(account.accountId(),"Sewer walker",0,2944.5,1059.5,p,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      var joined=hosted.sessions.join(store.acquire(account.secret(),created.id()),System.nanoTime());
      hosted.sessions.regionReady(joined.token(),0);
      // door 16 into the interior, then stairs 0, 2, 4 down and 5, 3, 1 back up.
      String[] keys={HostedRegionPlan.INTERIOR_KEY,HostedRegionPlan.SEWER_UPPER_KEY,HostedRegionPlan.SEWER_MIDDLE_KEY,HostedRegionPlan.SEWER_LOWER_KEY,
          HostedRegionPlan.SEWER_MIDDLE_KEY,HostedRegionPlan.SEWER_UPPER_KEY,HostedRegionPlan.INTERIOR_KEY};
      int[][] sources={{2931,1056},{415,395},{367,93},{205,289},{155,527},{123,330},{170,199}};
      for(int i=0;i<keys.length;i++) {
        var from=hosted.actor(joined.token());
        from.world.relocate(joined.token(),sources[i][0]+.5-from.map.originX,sources[i][1]+.5-from.map.originY);
        final int step=i;
        await(hosted,()->hosted.sessions.region(joined.token()).locationEpoch()==step+1);
        assertEquals(keys[i],hosted.sessions.region(joined.token()).key(),"Hop "+i);
        hosted.sessions.regionReady(joined.token(),i+1);
      }
      var saved=hosted.actor(joined.token()).world.position(joined.token());
      assertEquals(416,Math.floor(saved.x()+hosted.actor(joined.token()).map.originX),"Stair 1 lands beside stair 0 in the interior");
      var left=hosted.sessions.leave(joined.token());await(hosted,left::isDone);left.get();
      var loaded=store.load(account.accountId(),created.id());
      assertEquals(1,loaded.world());assertEquals(7,loaded.locationEpoch());
    }
  }

  @Test void walkingOntoARefusedKillCirclePortalKeepsTheSessionAndPositionInTheStartRegion() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var created=store.create(account.accountId(),"Refused portal walker",0,2944.5,1059.5,p,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled())) {
      var joined=hosted.sessions.join(store.acquire(account.secret(),created.id()),System.nanoTime());
      hosted.sessions.regionReady(joined.token(),0);
      var start=hosted.actor(joined.token());int exercised=0;
      for(var portal:plan.refusedPortals) {
        double x=portal.sourceX()+.5-start.map.originX,y=portal.sourceY()+.5-start.map.originY;
        if(!start.map.canStand(x,y)) continue;
        start.world.relocate(joined.token(),x,y);
        for(int i=0;i<PreviewWorld.TICK_RATE*2;i++) hosted.step();
        assertSame(start,hosted.actor(joined.token()),"Portal "+portal.id()+" must not move the actor to another region");
        assertEquals(0,hosted.sessions.region(joined.token()).world());
        assertEquals(0,hosted.sessions.region(joined.token()).locationEpoch(),"No transfer committed for portal "+portal.id());
        assertEquals(x,start.world.position(joined.token()).x(),1e-9);assertEquals(y,start.world.position(joined.token()).y(),1e-9);
        exercised++;
      }
      assertTrue(exercised>0,"At least one refused source must be walkable so the refusal is actually exercised");
      assertNotNull(hosted.sessions.character(joined.token()),"Session stays connected");
      var left=hosted.sessions.leave(joined.token());await(hosted,left::isDone);left.get();
      assertEquals(0,store.load(account.accountId(),created.id()).world());
    }
  }

  @Test void portalEntryQueuesBehindSlowCheckpointAndLogoutKeepsCommittedDestination() throws Exception {
    var pause=new java.util.concurrent.atomic.AtomicBoolean();
    var entered=new java.util.concurrent.CountDownLatch(1);var release=new java.util.concurrent.CountDownLatch(1);
    var store=new OnlineStore(()-> {
      if(pause.get()) { entered.countDown();try {
        if(!release.await(5,java.util.concurrent.TimeUnit.SECONDS)) throw new java.sql.SQLException("Test gate timeout");
      } catch(InterruptedException error) { Thread.currentThread().interrupt();throw new java.sql.SQLException(error); } }
      var properties=new java.util.Properties();properties.setProperty("user",System.getenv("T4C_DB_USER"));properties.setProperty("password",System.getenv("T4C_DB_PASSWORD"));
      return java.sql.DriverManager.getConnection(System.getenv("T4C_DB_URL"),properties);
    });
    store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var created=store.create(account.accountId(),"Pending portal logout",0,2944.5,1059.5,p,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var hosted=new HostedWorlds(plan,store,ItemCatalog.loadBundled(),MonsterCatalog.loadBundled());
    try {
      var joined=hosted.sessions.join(store.acquire(account.secret(),created.id()),System.nanoTime());
      hosted.sessions.regionReady(joined.token(),0);pause.set(true);
      for(int i=0;i<PreviewWorld.TICK_RATE*5;i++) hosted.sessions.step();
      assertTrue(entered.await(2,java.util.concurrent.TimeUnit.SECONDS));
      var source=hosted.actor(joined.token());source.world.relocate(joined.token(),2931.5-source.map.originX,1056.5-source.map.originY);hosted.step();
      source.world.input(joined.token(),1,1,0);source.world.step();
      assertEquals(2931.5-source.map.originX,source.world.position(joined.token()).x(),"Portal entry freezes even while checkpoint is waiting");
      var closed=hosted.sessions.leave(joined.token());assertFalse(closed.isDone());
      pause.set(false);release.countDown();await(hosted,closed::isDone);closed.get();
      var saved=store.load(account.accountId(),created.id());assertEquals(1,saved.world());assertEquals(1,saved.locationEpoch());
      assertEquals(304.5,saved.x());assertEquals(383.5,saved.y(),"Arrival is the centre of the authored tile, not its corner");
      var rejoined=store.acquire(account.secret(),created.id());store.release(rejoined);
    } finally { pause.set(false);release.countDown();hosted.close(); }
  }

  @Test void realHttpMovementTriggersAuthoredDoorAndFencesOldRegionCommands() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2)).progress();
    var first=store.create(account.accountId(),"HTTP door walker",0,2944.5,1059.5,p,0);
    var second=store.create(account.accountId(),"HTTP outside watcher",0,2944.5,1059.5,p,0);
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=java.net.http.HttpClient.newHttpClient()) {
      server.start();var a=http(client,server.url(),"/join",null,account.secret()+" "+first.id());
      var b=http(client,server.url(),"/join",null,account.secret()+" "+second.id());
      String token=a.get("token").getAsString(),other=b.get("token").getAsString();
      http(client,server.url(),"/region-ready",token,"0");http(client,server.url(),"/region-ready",other,"0");
      var state=walkPortal(client,server.url(),token,a.get("id").getAsLong(),plan.require(0,2944,1059).map(),2931-plan.require(0,2944,1059).map().originX,1056-plan.require(0,2944,1059).map().originY,0,1);
      assertEquals(1,state.getAsJsonObject("region").get("world").getAsInt());
      assertEquals(1,state.getAsJsonObject("region").get("locationEpoch").getAsLong());
      assertEquals(a.get("id"),state.getAsJsonArray("players").get(0).getAsJsonObject().get("id"));
      assertEquals(1,http(client,server.url(),"/state",other,null).getAsJsonArray("players").size());
      var map=http(client,server.url(),"/map",token,null);assertEquals(1,map.get("world").getAsInt());assertEquals(1,map.get("locationEpoch").getAsLong());
      assertEquals(1,http(client,server.url(),"/character",token,null).get("locationEpoch").getAsLong());
      var stale=client.send(request(server.url(),"/input",token,"0 1 1 0"),java.net.http.HttpResponse.BodyHandlers.ofString());
      assertEquals(409,stale.statusCode());
      http(client,server.url(),"/region-ready",token,"1");
      http(client,server.url(),"/leave",token,"");
      var rejoined=http(client,server.url(),"/join",null,account.secret()+" "+first.id());
      assertEquals(1,rejoined.getAsJsonObject("region").get("world").getAsInt());
      String returnedToken=rejoined.get("token").getAsString();http(client,server.url(),"/region-ready",returnedToken,"1");
      var returned=walkPortal(client,server.url(),returnedToken,rejoined.get("id").getAsLong(),plan.require(1,304,383).map(),306-plan.require(1,304,383).map().originX,381-plan.require(1,304,383).map().originY,1,0);
      assertEquals(2,returned.getAsJsonObject("region").get("locationEpoch").getAsLong());
      http(client,server.url(),"/region-ready",returnedToken,"2");
      assertEquals(2,http(client,server.url(),"/state",other,null).getAsJsonArray("players").size());
    }
  }
  /** Exercise only network direction inputs; route uses the real server collision crop. */
  private static com.google.gson.JsonObject walkPortal(java.net.http.HttpClient client,String url,String token,long ownId,
      PreviewMap map,int goalX,int goalY,long epoch,int destinationWorld) throws Exception {
    int lastX=0,lastY=0;
    long sequence=2,deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(35);
    int goal=goalY*map.width+goalX;
    com.google.gson.JsonObject state=null;
    while(System.nanoTime()<deadline) {
      state=http(client,url,"/state",token,null);
      if(state.getAsJsonObject("region").get("world").getAsInt()==destinationWorld) return state;
      var own=actor(state,ownId);
      double ox=own.get("x").getAsDouble(),oy=own.get("y").getAsDouble();
      // The server finishes a claimed tile (single-player's rule). While the actor glides, plan from the
      // tile it is arriving on (the last direction sent), so the next input is already queued at arrival.
      int tileX=(int)Math.round(ox-.5),tileY=(int)Math.round(oy-.5);
      if(Math.abs(ox-Math.floor(ox)-.5)>1e-6) tileX=lastX>0 ? (int)Math.ceil(ox-.5) : lastX<0 ? (int)Math.floor(ox-.5) : tileX;
      if(Math.abs(oy-Math.floor(oy)-.5)>1e-6) tileY=lastY>0 ? (int)Math.ceil(oy-.5) : lastY<0 ? (int)Math.floor(oy-.5) : tileY;
      int start=tileY*map.width+tileX;
      if(start==goal) { Thread.sleep(50);continue; }
      var previous=new int[map.width*map.height];Arrays.fill(previous,-1);previous[start]=start;
      var queue=new ArrayDeque<Integer>();queue.add(start);
      while(!queue.isEmpty() && previous[goal]<0) {
        int node=queue.remove(),x=node%map.width,y=node/map.width;
        for(var delta:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
          int nx=x+delta[0],ny=y+delta[1];if(!map.canStand(nx+.5,ny+.5)) continue;
          int next=ny*map.width+nx;if(previous[next]>=0) continue;previous[next]=node;queue.add(next);
        }
      }
      // A mid-glide position does not reveal its direction, so a wrong arrival-tile guess can name a
      // tile that cannot reach the portal. Replan from the next /state; only a tile-centred actor
      // (an unambiguous start) may fail the reachability assertion.
      boolean centred=Math.abs(ox-Math.floor(ox)-.5)<=1e-6 && Math.abs(oy-Math.floor(oy)-.5)<=1e-6;
      if(previous[goal]<0 && !centred) { Thread.sleep(20);continue; }
      assertTrue(previous[goal]>=0,"Authored portal must be reachable from tile "+tileX+","+tileY+" (actor "+ox+","+oy+", last move "+lastX+","+lastY+", standable="+map.canStand(tileX+.5,tileY+.5)+", region "+state.getAsJsonObject("region")+", hp "+own+")");
      int next=goal;while(previous[next]!=start) next=previous[next];
      int moveX=Integer.compare(next%map.width,start%map.width),moveY=Integer.compare(next/map.width,start/map.width);
      lastX=moveX;lastY=moveY;
      var response=client.send(request(url,"/input",token,epoch+" "+sequence+++" "+moveX+" "+moveY),java.net.http.HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()==409) assertEquals("region_epoch",com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject().get("error").getAsString());
      else assertEquals(200,response.statusCode(),"Movement rejected");
      // Retain intent across a simulation tick. Immediate input/stop can both be drained
      // before a single tick on fast CI, so wall-clock HTTP speed must not determine motion.
      Thread.sleep(50);
    }
    fail("Direction-only authored portal traversal timed out at "+actor(state,ownId).get("x")+","+actor(state,ownId).get("y"));return state;
  }

  private static com.google.gson.JsonObject actor(com.google.gson.JsonObject state,long id) {
    for(var entry:state.getAsJsonArray("players")) if(entry.getAsJsonObject().get("id").getAsLong()==id) return entry.getAsJsonObject();
    throw new AssertionError("Own actor missing");
  }
  private static java.net.http.HttpRequest request(String url,String path,String token,String body) {
    var builder=java.net.http.HttpRequest.newBuilder(java.net.URI.create(url+path)).timeout(java.time.Duration.ofSeconds(5));
    if(token!=null) builder.header("Authorization","Bearer "+token);
    if(body!=null) builder.header("Origin",url).header("Content-Type","text/plain").POST(java.net.http.HttpRequest.BodyPublishers.ofString(body));
    if(path.equals("/join")) builder.header(com.perso.T4C.online.model.OnlineProtocol.VERSION_HEADER,Integer.toString(com.perso.T4C.online.model.OnlineProtocol.VERSION));
    return builder.build();
  }
  private static com.google.gson.JsonObject http(java.net.http.HttpClient client,String url,String path,String token,String body) throws Exception {
    var response=client.send(request(url,path,token,body),java.net.http.HttpResponse.BodyHandlers.ofString());
    assertEquals(200,response.statusCode(),path+" rejected");return com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
  }

  private static void await(HostedWorlds hosted,BooleanSupplier complete) throws Exception {
    long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(8);
    while(!complete.getAsBoolean() && System.nanoTime()<deadline) { hosted.step();Thread.sleep(10); }
    assertTrue(complete.getAsBoolean(),"Asynchronous region transition timed out");
  }
}
