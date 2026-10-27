package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class OnlineNpcInteractionsTest {
  @Test void openingLetterDraftPreservesStateAndDoesNotRegrantAfterReload() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled(); var npc=catalog.require(CrimsonscaleLetterRules.NPC);
    var items=ItemCatalog.loadBundled(); var interactions=new OnlineNpcInteractions(catalog,items);
    var original=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var id=UUID.randomUUID();
    var draft=talk(interactions,state(original,0),npc,OptionalInt.empty(),()->id);
    assertTrue(draft.changed()); assertEquals("npc.kilhiam.letter",draft.messageKey());
    assertEquals(original.inventory().size()+1,draft.progress().inventory().size());
    var letter=draft.progress().inventory().getLast();
    assertEquals(id,letter.id()); assertEquals(CrimsonscaleLetterRules.ITEM,letter.key());
    assertEquals(1,letter.quantity()); assertEquals(items.items().get(letter.key()).nbCharges(),letter.charges());
    assertEquals(original.inventory(),draft.progress().inventory().subList(0,original.inventory().size()));
    assertEquals(original.vitals(),draft.progress().vitals());
    assertEquals(original.advancement(),draft.progress().advancement());
    assertEquals(original.equipment(),draft.progress().equipment());
    assertEquals(0,draft.gold()); assertEquals(0,draft.experience());
    var gson=new com.google.gson.Gson();
    var reloaded=gson.fromJson(gson.toJson(draft.progress()),CharacterProgress.class);
    var repeated=talk(interactions,state(reloaded,0),npc,OptionalInt.empty(),
        ()->{throw new AssertionError("A repeat must not allocate an item");});
    assertFalse(repeated.changed()); assertEquals(reloaded,repeated.progress());
    assertEquals("npc.kilhiam.temple",repeated.messageKey());
  }

  @Test void wardenAelricGreetingWithAReadyQuestCommitsTheRealTurnInExactlyOnce() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled(); var aelric=catalog.require("WardenAelric");
    var interactions=new OnlineNpcInteractions(catalog,ItemCatalog.loadBundled());
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var greeted=talk(interactions,state(progress,0),aelric,OptionalInt.empty(),UUID::randomUUID);
    assertFalse(greeted.changed()); assertEquals("npc.welcome.wardenaelric",greeted.messageKey()); assertEquals(progress,greeted.progress());
    var gson=new com.google.gson.Gson();
    var inProgress=gson.toJsonTree(progress).getAsJsonObject();
    inProgress.getAsJsonObject("questFlags").addProperty("quest.renewed_wards.status",1);
    inProgress.getAsJsonObject("questFlags").addProperty("quest.renewed_wards.kills",3);
    var active=gson.fromJson(inProgress,CharacterProgress.class);
    assertFalse(talk(interactions,state(active,0),aelric,OptionalInt.empty(),UUID::randomUUID).changed());
    inProgress.getAsJsonObject("questFlags").addProperty("quest.renewed_wards.kills",12);
    var ready=gson.fromJson(inProgress,CharacterProgress.class);
    var turnedIn=talk(interactions,state(ready,0),aelric,OptionalInt.empty(),UUID::randomUUID);
    assertTrue(turnedIn.changed());
    assertEquals(500,turnedIn.gold()); assertEquals(400,turnedIn.experience());
    assertEquals("quest.renewed_wards.completion",turnedIn.messageKey());
    assertEquals(2,turnedIn.progress().questFlags().get("quest.renewed_wards.status"));
    assertTrue(turnedIn.progress().spells().contains("spell.renew_armor"));
    // Once committed, the same greeting is a no-op with no further reward.
    var committed=gson.fromJson(gson.toJsonTree(turnedIn.progress()),CharacterProgress.class);
    var repeat=talk(interactions,state(committed,400),aelric,OptionalInt.empty(),
        ()->{throw new AssertionError("A completed quest must not allocate anything");});
    assertFalse(repeat.changed()); assertEquals(0,repeat.gold()); assertEquals(0,repeat.experience());
    assertEquals(committed,repeat.progress());
  }

  @Test void wardenAelricTopicsReadTheOfferAndProgressWithoutRewards() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled(); var aelric=catalog.require("WardenAelric");
    var interactions=new OnlineNpcInteractions(catalog,ItemCatalog.loadBundled());
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var offered=talk(interactions,state(progress,0),aelric,OptionalInt.of(0),UUID::randomUUID);
    assertTrue(offered.changed()); assertEquals("quest.renewed_wards.offer",offered.messageKey());
    assertEquals(0,offered.gold()); assertEquals(0,offered.experience());
    var gson=new com.google.gson.Gson();
    var accepted=gson.fromJson(gson.toJsonTree(offered.progress()),CharacterProgress.class);
    var again=talk(interactions,state(accepted,0),aelric,OptionalInt.of(0),UUID::randomUUID);
    assertFalse(again.changed()); assertEquals("message.quest_progress_dialog",again.messageKey());
    assertEquals(List.of(0,12,12),again.args());
    assertEquals(0,again.gold());
    var lore=talk(interactions,state(accepted,0),aelric,OptionalInt.of(1),UUID::randomUUID);
    assertFalse(lore.changed()); assertEquals("npc.topic.wardenaelric.1",lore.messageKey());
    assertThrows(IllegalArgumentException.class,()->talk(interactions,state(accepted,0),aelric,OptionalInt.of(7),UUID::randomUUID));
  }

  @Test void kilhiamHasNoTopicsAndInteractionChecksAuthoritativeLocationAndSightBeforeRewarding() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();var npc=catalog.npcs().getFirst();
    var interactions=new OnlineNpcInteractions(catalog,ItemCatalog.loadBundled());
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var saved=state(progress,0);
    assertThrows(IllegalArgumentException.class,()->talk(interactions,saved,npc,OptionalInt.of(0),UUID::randomUUID));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(saved,npc.world(),npc.x()+100,npc.y(),npc.id(),true,OptionalInt.empty(),UUID::randomUUID,0,new Random(1)));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(saved,npc.world(),npc.x(),npc.y(),npc.id(),false,OptionalInt.empty(),UUID::randomUUID,0,new Random(1)));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(saved,npc.world()+1,npc.x(),npc.y(),npc.id(),true,OptionalInt.empty(),UUID::randomUUID,0,new Random(1)));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(saved,npc.world(),npc.x(),npc.y(),"OtherNpc",true,OptionalInt.empty(),UUID::randomUUID,0,new Random(1)));
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void questFlagAndLetterCommitTogetherAndReceiptRetryCannotDuplicateReward() throws Exception {
    var store=com.perso.T4C.server.persistence.OnlineStore.fromEnvironment();store.migrate();
    var account=store.provisionAccount();var catalog=NpcInteractionCatalog.loadBundled();var npc=catalog.npcs().getFirst();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var created=store.create(account.accountId(),"Letter transaction",npc.world(),npc.x(),npc.y(),progress,0);
    var interactions=new OnlineNpcInteractions(catalog,ItemCatalog.loadBundled());
    var session=store.acquire(account.secret(),created.id());
    var draft=talk(interactions,session.character(),npc,OptionalInt.empty(),UUID::randomUUID);
    var event=UUID.randomUUID();
    var committed=store.commitGameplayEvent(session,created.revision(),event,0,0,draft.progress());
    assertTrue(committed.applied());store.release(session);
    var rejoined=store.acquire(account.secret(),created.id());
    try {
      assertFalse(store.commitGameplayEvent(rejoined,created.revision(),event,0,0,draft.progress()).applied());
      var saved=store.load(account.accountId(),created.id());
      assertEquals(draft.progress(),saved.progress());assertEquals(created.gold(),saved.gold());assertEquals(created.experience(),saved.experience());
      assertFalse(talk(interactions,saved,npc,OptionalInt.empty(),UUID::randomUUID).changed());
    } finally { store.release(rejoined); }
  }

  private static OnlineNpcInteractions.Result talk(OnlineNpcInteractions interactions,OnlineStore.CharacterState saved,
      com.perso.T4C.online.model.NpcInteractionContent.Npc npc,OptionalInt topic,java.util.function.Supplier<UUID> instance) {
    return interactions.prepare(saved,npc.world(),npc.x(),npc.y(),npc.id(),true,topic,instance,100,new Random(1));
  }

  private static OnlineStore.CharacterState state(CharacterProgress progress,long experience) {
    return new OnlineStore.CharacterState(UUID.randomUUID(),UUID.randomUUID(),"Tester",0,3,3,0,experience,0,progress);
  }
}
