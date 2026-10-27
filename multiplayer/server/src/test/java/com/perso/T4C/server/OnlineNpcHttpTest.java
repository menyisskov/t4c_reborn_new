package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
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
