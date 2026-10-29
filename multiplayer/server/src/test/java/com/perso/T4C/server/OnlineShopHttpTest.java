package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.server.persistence.OnlineStore;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** The whole vendor flow over HTTP against a real database: hosted shopkeeper, listing, buy, sell, refusals. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineShopHttpTest {
  @Test void sigfriedSellsAndBuysThroughTheServerWithServerSidePricesOnly() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(5));
    var owner=store.provisionAccount();
    var npc=NpcInteractionCatalog.loadBundled().require("Sigfried");
    var region=plan.require(npc.world(),npc.x(),npc.y());
    var map=region.map();
    double x=-1,y=-1;
    search:
    for(int dx=-2;dx<=2;dx++) for(int dy=-2;dy<=2;dy++)
      if(map.canStand(npc.x()+dx+0.5-map.originX,npc.y()+dy+0.5-map.originY)) { x=npc.x()+dx+0.5;y=npc.y()+dy+0.5;break search; }
    assertTrue(x>=0,"a standable tile beside Sigfried");
    var rich=store.create(owner.accountId(),"Shop buyer",npc.world(),x,y,prepared.progress(),100_000_000L);
    var poor=store.create(owner.accountId(),"Shop poor",npc.world(),x,y,prepared.progress(),10);
    var distant=store.create(owner.accountId(),"Shop far",region.spec().world(),map.originX+map.spawnX,
        map.originY+map.spawnY,prepared.progress(),1000);
    try(var server=new PreviewServer(plan,0,store,null,ServerTransport.local());var client=HttpClient.newHttpClient()) {
      server.start();String url=server.url();
      assertEquals(401,send(client,url,"/shop-offers",null,"0 0 Sigfried").statusCode());
      String token=login(client,url,owner,rich.id());
      assertEquals(200,send(client,url,"/region-ready",token,"0").statusCode());
      var state=object(send(client,url,"/state",token,null));
      assertTrue(java.util.stream.StreamSupport.stream(state.getAsJsonArray("npcs").spliterator(),false)
          .anyMatch(view->"Sigfried".equals(view.getAsJsonObject().get("id").getAsString())),"Sigfried is hosted in the starter region");
      long revision=revision(client,url,token);

      var greeting=object(send(client,url,"/interact",token,revision+" 0 Sigfried"));
      assertEquals("npc.welcome.sigfried",greeting.get("messageKey").getAsString());
      assertFalse(greeting.get("changed").getAsBoolean());
      assertEquals(409,send(client,url,"/interact",token,revision+" 0 Sigfried 0").statusCode(),"no authored topics online");

      // Listing: server prices, no client input beyond identity.
      var offers=object(send(client,url,"/shop-offers",token,revision+" 0 Sigfried"));
      assertEquals("Sigfried",offers.get("npc").getAsString());
      assertEquals(100_000_000L,offers.get("gold").getAsLong());
      var sword=lineFor(offers.getAsJsonArray("buy"),"item.rusted_short_sword");
      assertEquals(9,sword.get("price").getAsLong());

      // Refusals that never touch the database.
      assertEquals(426,sendWithoutProtocol(client,url,"/shop-offers",token,revision+" 0 Sigfried").statusCode());
      assertEquals(409,send(client,url,"/shop-offers",token,revision+" 0 Kilhiam").statusCode(),"a reachable NPC without a shop");
      assertEquals(409,send(client,url,"/shop-offers",token,revision+" 0 UnknownNpc").statusCode());
      assertEquals(409,send(client,url,"/shop-offers",token,revision+" 1 Sigfried").statusCode(),"wrong region epoch");
      assertEquals(400,send(client,url,"/shop-offers",token,revision+" 0").statusCode());
      assertEquals(400,send(client,url,"/shop-buy",token,revision+" 0 Sigfried item.rusted_short_sword 2 999").statusCode(),"no client price slot");
      assertEquals(400,send(client,url,"/shop-buy",token,revision+" 0 Sigfried item.rusted_short_sword 0").statusCode());
      assertEquals(409,send(client,url,"/shop-buy",token,revision+" 0 Sigfried item.healing_potion 1").statusCode(),"not in his stock");
      assertEquals(409,send(client,url,"/shop-buy",token,revision+" 0 Sigfried item.no_such_item 1").statusCode());
      String farToken=login(client,url,owner,distant.id());send(client,url,"/region-ready",farToken,"0");
      assertEquals(409,send(client,url,"/shop-offers",farToken,"0 0 Sigfried").statusCode(),"out of conversation range");
      assertEquals(409,send(client,url,"/shop-buy",farToken,"0 0 Sigfried item.rusted_short_sword 1").statusCode());

      // Buy: the server charges its own price and commits gold and the stack together.
      var bought=send(client,url,"/shop-buy",token,revision+" 0 Sigfried item.rusted_short_sword 2");
      assertEquals(200,bought.statusCode(),bought.body());
      var receipt=object(bought);
      assertEquals(18,receipt.get("total").getAsLong());assertEquals(2,receipt.get("quantity").getAsInt());
      assertTrue(receipt.get("revision").getAsLong()>revision);
      var saved=store.load(owner.accountId(),rich.id());
      assertEquals(100_000_000L-18,saved.gold());
      assertEquals(2,saved.progress().inventory().stream().filter(i->i.key().equals("item.rusted_short_sword"))
          .mapToInt(i->i.quantity()).sum());

      // A lost response retried with the old revision can never buy twice.
      assertEquals(409,send(client,url,"/shop-buy",token,revision+" 0 Sigfried item.rusted_short_sword 2").statusCode());
      assertEquals(100_000_000L-18,store.load(owner.accountId(),rich.id()).gold());

      // Sell one back at the single-player resale price.
      long after=receipt.get("revision").getAsLong();
      var sold=object(send(client,url,"/shop-sell",token,after+" 0 Sigfried item.rusted_short_sword 1"));
      assertEquals(4,sold.get("total").getAsLong());
      assertEquals(100_000_000L-18+4,store.load(owner.accountId(),rich.id()).gold());
      long afterSale=sold.get("revision").getAsLong();
      assertEquals(409,send(client,url,"/shop-sell",token,afterSale+" 0 Sigfried item.rusted_short_sword 2").statusCode(),"only one is left");
      assertEquals(409,send(client,url,"/shop-sell",token,afterSale+" 0 Sigfried item.healing_potion 1").statusCode(),"nothing to sell");

      // Weight: a hundred axes cannot be carried by this character.
      var items=ItemCatalog.loadBundled();
      long axeWeight=items.items().get("item.rusted_hand_axe").weight();
      assertTrue(axeWeight*99>com.perso.T4C.item.CarryCapacityRules.maximumWeight(prepared.progress().attributes().strength()+200));
      assertEquals(409,send(client,url,"/shop-buy",token,afterSale+" 0 Sigfried item.rusted_hand_axe 99").statusCode());
      assertEquals(100_000_000L-18+4,store.load(owner.accountId(),rich.id()).gold(),"a refusal changes nothing");

      // Not enough gold.
      assertEquals(200,send(client,url,"/leave",token,"").statusCode());
      String poorToken=login(client,url,owner,poor.id());send(client,url,"/region-ready",poorToken,"0");
      long poorRevision=revision(client,url,poorToken);
      assertEquals(409,send(client,url,"/shop-buy",poorToken,poorRevision+" 0 Sigfried item.rusted_long_sword 1").statusCode());
      assertEquals(10,store.load(owner.accountId(),poor.id()).gold());
    }
  }

  private static JsonObject lineFor(com.google.gson.JsonArray lines,String key) {
    for(var line:lines) if(key.equals(line.getAsJsonObject().get("key").getAsString())) return line.getAsJsonObject();
    return fail("Missing line "+key);
  }
  private static long revision(HttpClient client,String url,String token) throws Exception {
    return object(send(client,url,"/character",token,null)).get("revision").getAsLong();
  }
  private static HttpResponse<String> send(HttpClient client,String url,String path,String token,String body) throws Exception {
    var request=HttpRequest.newBuilder(URI.create(url+path)).timeout(Duration.ofSeconds(5));
    if(token!=null) request.header("Authorization","Bearer "+token);
    if(path.equals("/join") || path.startsWith("/shop-")) request.header(OnlineProtocol.VERSION_HEADER,Integer.toString(OnlineProtocol.VERSION));
    if(body!=null) request.header("Origin",url).header("Content-Type","text/plain").POST(HttpRequest.BodyPublishers.ofString(body));
    return client.send(request.build(),HttpResponse.BodyHandlers.ofString());
  }
  private static HttpResponse<String> sendWithoutProtocol(HttpClient client,String url,String path,String token,String body) throws Exception {
    var request=HttpRequest.newBuilder(URI.create(url+path)).timeout(Duration.ofSeconds(5))
        .header("Authorization","Bearer "+token).header("Origin",url).header("Content-Type","text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(body));
    return client.send(request.build(),HttpResponse.BodyHandlers.ofString());
  }
  private static JsonObject object(HttpResponse<String> response) {
    assertEquals(200,response.statusCode(),response.body());return JsonParser.parseString(response.body()).getAsJsonObject();
  }
  private static String login(HttpClient client,String url,OnlineStore.AccountKey account,java.util.UUID id) throws Exception {
    return object(send(client,url,"/join",null,account.secret()+" "+id)).get("token").getAsString();
  }
}
