package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.quest.QuestRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineNpcHttpTest {
  @Test void authoredLetterRequiresCurrentNearbyOwnerAndCommitsOnlyOnceAcrossReconnect() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var owner=store.provisionAccount();var other=store.provisionAccount();
    var npc=NpcInteractionCatalog.loadBundled().npcs().getFirst();
    var start=plan.require(npc.world(),npc.x(),npc.y());
    double x=npc.x()+0.5,y=npc.y()+0.5;
    assertTrue(start.map().canStand(x-start.map().originX,y-start.map().originY));
    var character=store.create(owner.accountId(),"HTTP letter",npc.world(),x,y,prepared.progress(),prepared.gold());
    var second=plan.regions().get(1);var secondMap=second.map();
    var foreign=store.create(other.accountId(),"Other region",second.spec().world(),secondMap.originX+secondMap.spawnX,
        secondMap.originY+secondMap.spawnY,prepared.progress(),prepared.gold());
    var map=start.map();
    var distant=store.create(owner.accountId(),"Far from NPC",start.spec().world(),map.originX+map.spawnX,
        map.originY+map.spawnY,prepared.progress(),prepared.gold());
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=HttpClient.newHttpClient()) {
      server.start();String url=server.url();
      String token=login(client,url,owner,character.id());
      assertEquals(401,send(client,url,"/interact",null,"0 0 Kilhiam").statusCode());
      assertEquals(409,send(client,url,"/interact",token,"0 0 Kilhiam").statusCode(),"Assets must be ready before interaction");
      assertEquals(200,send(client,url,"/region-ready",token,"0").statusCode());
      var state=object(send(client,url,"/state",token,null));
      var actor=state.getAsJsonArray("npcs").get(0).getAsJsonObject();
      assertEquals(npc.id(),actor.get("id").getAsString());
      assertEquals(npc.x()-map.originX,actor.get("x").getAsDouble());
      assertFalse(actor.has("questFlags"));assertFalse(actor.has("inventory"));
      String foreignToken=login(client,url,other,foreign.id());send(client,url,"/region-ready",foreignToken,"0");
      assertTrue(object(send(client,url,"/state",foreignToken,null)).getAsJsonArray("npcs").isEmpty());
      assertEquals(409,send(client,url,"/interact",foreignToken,"0 0 Kilhiam").statusCode());
      String distantToken=login(client,url,owner,distant.id());send(client,url,"/region-ready",distantToken,"0");
      assertEquals(409,send(client,url,"/interact",distantToken,"0 0 Kilhiam").statusCode());
      assertEquals(409,send(client,url,"/interact",token,"0 1 Kilhiam").statusCode());
      assertEquals(409,send(client,url,"/interact",token,"99999 0 Kilhiam").statusCode());
      assertEquals(409,send(client,url,"/interact",token,"0 0 UnknownNpc").statusCode());
      assertEquals(400,send(client,url,"/interact",token,"9223372036854775808 0 Kilhiam").statusCode());
      long revision=object(send(client,url,"/character",token,null)).get("revision").getAsLong();
      var accepted=send(client,url,"/interact",token,revision+" 0 Kilhiam");assertEquals(200,accepted.statusCode(),accepted.body());
      var reply=object(accepted);assertTrue(reply.get("changed").getAsBoolean());
      assertEquals("npc.kilhiam.letter",reply.get("messageKey").getAsString());
      long committedRevision=reply.get("revision").getAsLong();assertTrue(committedRevision>revision);
      var saved=store.load(owner.accountId(),character.id());
      var letter=saved.progress().inventory().stream().filter(i->i.key().equals(CrimsonscaleLetterRules.ITEM)).findFirst().orElseThrow();
      assertEquals(1,saved.progress().questFlags().get(CrimsonscaleLetterRules.LETTER_FLAG));
      assertEquals(character.gold(),saved.gold());assertEquals(character.experience(),saved.experience());
      var unchanged=object(send(client,url,"/interact",token,committedRevision+" 0 Kilhiam"));
      assertFalse(unchanged.get("changed").getAsBoolean());
      assertEquals(committedRevision,unchanged.get("revision").getAsLong());
      assertEquals("npc.kilhiam.temple",unchanged.get("messageKey").getAsString());
      assertEquals(200,send(client,url,"/leave",token,"").statusCode());
      token=login(client,url,owner,character.id());send(client,url,"/region-ready",token,"0");
      revision=object(send(client,url,"/character",token,null)).get("revision").getAsLong();
      assertFalse(object(send(client,url,"/interact",token,revision+" 0 Kilhiam")).get("changed").getAsBoolean());
      var restored=store.load(owner.accountId(),character.id()).progress();
      assertEquals(1,restored.inventory().stream().filter(i->i.key().equals(letter.key())).count());
      assertTrue(restored.inventory().contains(letter));
      assertFalse(store.load(other.accountId(),foreign.id()).progress().questFlags().containsKey(CrimsonscaleLetterRules.LETTER_FLAG));
    }
  }

  @Test void questGiverTopicsAcceptProgressAndTurnInOnceOverHttp() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(11));
    var owner=store.provisionAccount();
    var giver=NpcInteractionCatalog.loadBundled().npcs().stream().filter(n->n.id().equals("WardenAelric")).findFirst().orElseThrow();
    var start=plan.require(giver.world(),giver.x(),giver.y());
    var character=store.create(owner.accountId(),"HTTP quest giver",giver.world(),giver.x()+0.5,giver.y()+0.5,prepared.progress(),prepared.gold());
    String url;
    // Session one: accept, read progress, read lore, and refuse an unknown topic. Nothing changes after accept.
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=HttpClient.newHttpClient()) {
      server.start();url=server.url();
      String token=login(client,url,owner,character.id());
      assertEquals(200,send(client,url,"/region-ready",token,"0").statusCode());
      long revision=object(send(client,url,"/character",token,null)).get("revision").getAsLong();
      var accepted=object(send(client,url,"/interact",token,revision+" 0 WardenAelric 0"));
      assertTrue(accepted.get("changed").getAsBoolean());
      assertTrue(accepted.get("messageKey").getAsString().startsWith("quest.renewed_wards"));
      long afterAccept=accepted.get("revision").getAsLong();assertTrue(afterAccept>revision);
      var progress=object(send(client,url,"/interact",token,afterAccept+" 0 WardenAelric 0"));
      assertFalse(progress.get("changed").getAsBoolean());
      assertEquals(afterAccept,progress.get("revision").getAsLong());
      var args=progress.getAsJsonArray("args");
      assertEquals(0,args.get(0).getAsInt());assertEquals(12,args.get(1).getAsInt());assertEquals(12,args.get(2).getAsInt());
      var lore=object(send(client,url,"/interact",token,afterAccept+" 0 WardenAelric 1"));
      assertFalse(lore.get("changed").getAsBoolean());
      assertEquals(409,send(client,url,"/interact",token,afterAccept+" 0 WardenAelric 2").statusCode(),"An unauthored topic is refused");
      assertEquals(400,send(client,url,"/interact",token,afterAccept+" 0 WardenAelric 100").statusCode(),"A three-digit topic is a malformed body");
      assertEquals(200,send(client,url,"/leave",token,"").statusCode());
    }
    // Session two: the quest completes in the database (as after twelve kills), and the greeting turns it in once.
    var definition=QuestCatalog.loadBundled().definition("renewed_wards");
    var fixture=store.acquire(owner.secret(),character.id());
    var saved=store.load(owner.accountId(),character.id());
    var flags=new java.util.HashMap<>(saved.progress().questFlags());
    flags.put(QuestRules.statusFlag(definition),QuestRules.STATUS_ACTIVE);
    flags.put(QuestRules.killsFlag(definition),definition.getRequiredKills());
    store.commitGameplayEvent(fixture,saved.revision(),java.util.UUID.randomUUID(),0,0,ProgressEdits.withFlags(saved.progress(),flags));
    store.release(fixture);
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=HttpClient.newHttpClient()) {
      server.start();
      String token=login(client,server.url(),owner,character.id());
      send(client,server.url(),"/region-ready",token,"0");
      long revision=object(send(client,server.url(),"/character",token,null)).get("revision").getAsLong();
      var turnedIn=object(send(client,server.url(),"/interact",token,revision+" 0 WardenAelric"));
      assertTrue(turnedIn.get("changed").getAsBoolean(),"The greeting turns in a ready quest");
      long committed=turnedIn.get("revision").getAsLong();assertTrue(committed>revision);
      var again=object(send(client,server.url(),"/interact",token,committed+" 0 WardenAelric"));
      assertFalse(again.get("changed").getAsBoolean());assertEquals(committed,again.get("revision").getAsLong());
      var completed=object(send(client,server.url(),"/interact",token,committed+" 0 WardenAelric 0"));
      assertFalse(completed.get("changed").getAsBoolean());
      var rewarded=store.load(owner.accountId(),character.id());
      assertEquals(prepared.gold()+500,rewarded.gold(),"Turn-in grants its gold exactly once");
    }
  }

  @Test void groundLootIsRegionLocalAndNearestFirstOverHttp() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(13));
    var near=store.provisionAccount();var far=store.provisionAccount();
    var first=plan.regions().getFirst();var firstMap=first.map();
    var second=plan.regions().get(1);var secondMap=second.map();
    double homeX=firstMap.originX+firstMap.spawnX+0.5,homeY=firstMap.originY+firstMap.spawnY+0.5;
    double otherX=secondMap.originX+secondMap.spawnX+0.5,otherY=secondMap.originY+secondMap.spawnY+0.5;
    var nearCharacter=store.create(near.accountId(),"Loot viewer",first.spec().world(),homeX,homeY,prepared.progress(),prepared.gold());
    var farCharacter=store.create(far.accountId(),"Loot far viewer",second.spec().world(),otherX,otherY,prepared.progress(),prepared.gold());
    var nearDrop=dropKilledBy(store,near,nearCharacter,first.spec().world(),homeX+1,homeY,prepared.progress());
    var farDrop=dropKilledBy(store,near,nearCharacter,first.spec().world(),homeX+5,homeY,prepared.progress());
    var otherDrop=dropKilledBy(store,far,farCharacter,second.spec().world(),otherX,otherY,prepared.progress());
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=HttpClient.newHttpClient()) {
      server.start();String url=server.url();
      String token=login(client,url,near,nearCharacter.id());
      send(client,url,"/region-ready",token,"0");
      var listed=object(send(client,url,"/state",token,null)).getAsJsonArray("loot");
      assertEquals(2,listed.size(),"Loot from the other region never appears");
      assertEquals(nearDrop.toString(),listed.get(0).getAsJsonObject().get("id").getAsString(),"Nearest drop first");
      assertEquals(farDrop.toString(),listed.get(1).getAsJsonObject().get("id").getAsString());
      assertEquals(homeX+1-firstMap.originX,listed.get(0).getAsJsonObject().get("x").getAsDouble(),"Coordinates are region-local");
      String otherToken=login(client,url,far,farCharacter.id());
      send(client,url,"/region-ready",otherToken,"0");
      var seen=object(send(client,url,"/state",otherToken,null)).getAsJsonArray("loot");
      assertEquals(1,seen.size());
      assertEquals(otherDrop.toString(),seen.get(0).getAsJsonObject().get("id").getAsString());
      assertFalse(seen.toString().contains(nearDrop.toString()));
    }
  }

  /** Commits one lethal kill by {@code killer} that leaves a single drop at the given tile. */
  private static java.util.UUID dropKilledBy(OnlineStore store,OnlineStore.AccountKey killer,OnlineStore.CharacterState character,
      int world,double x,double y,com.perso.T4C.online.model.CharacterProgress progress) throws Exception {
    var session=store.acquire(killer.secret(),character.id());
    try {
      var life=store.loadOrCreateSpawn("loot-http:"+java.util.UUID.randomUUID());
      var drop=new com.perso.T4C.online.model.LootDropDraft(java.util.UUID.randomUUID(),0,"item.leather_gloves",1,0,world,x,y);
      var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),10,10,0);
      assertTrue(store.commitCreatureStrike(session,store.load(killer.accountId(),character.id()).revision(),life.eventId(),0,0,
          progress,strike,null,java.util.List.of(drop)).applied());
      return drop.id();
    } finally { store.release(session); }
  }

  private static HttpResponse<String> send(HttpClient client,String url,String path,String token,String body) throws Exception {
    var request=HttpRequest.newBuilder(URI.create(url+path)).timeout(Duration.ofSeconds(5));
    if(token!=null) request.header("Authorization","Bearer "+token);
    if(path.equals("/join")) request.header(OnlineProtocol.VERSION_HEADER,Integer.toString(OnlineProtocol.VERSION));
    if(body!=null) request.header("Origin",url).header("Content-Type","text/plain").POST(HttpRequest.BodyPublishers.ofString(body));
    return client.send(request.build(),HttpResponse.BodyHandlers.ofString());
  }
  private static JsonObject object(HttpResponse<String> response) {
    assertEquals(200,response.statusCode(),response.body());return JsonParser.parseString(response.body()).getAsJsonObject();
  }
  private static String login(HttpClient client,String url,OnlineStore.AccountKey account,java.util.UUID id) throws Exception {
    return object(send(client,url,"/join",null,account.secret()+" "+id)).get("token").getAsString();
  }
}
