package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class OnlineNpcInteractionsTest {
  @Test void openingLetterDraftPreservesStateAndDoesNotRegrantAfterReload() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled(); var npc=catalog.require(CrimsonscaleLetterRules.NPC);
    var items=ItemCatalog.loadBundled(); var interactions=new OnlineNpcInteractions(catalog,items);
    var original=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var id=UUID.randomUUID();
    var draft=interactions.prepare(original,npc.world(),npc.x(),npc.y(),npc.id(),true,()->id);
    assertTrue(draft.changed()); assertEquals("npc.kilhiam.letter",draft.messageKey());
    assertEquals(original.inventory().size()+1,draft.progress().inventory().size());
    var letter=draft.progress().inventory().getLast();
    assertEquals(id,letter.id()); assertEquals(CrimsonscaleLetterRules.ITEM,letter.key());
    assertEquals(1,letter.quantity()); assertEquals(items.items().get(letter.key()).nbCharges(),letter.charges());
    assertEquals(original.inventory(),draft.progress().inventory().subList(0,original.inventory().size()));
    assertEquals(original.vitals(),draft.progress().vitals());
    assertEquals(original.advancement(),draft.progress().advancement());
    assertEquals(original.equipment(),draft.progress().equipment());
    var gson=new com.google.gson.Gson();
    var reloaded=gson.fromJson(gson.toJson(draft.progress()),com.perso.T4C.online.model.CharacterProgress.class);
    var repeated=interactions.prepare(reloaded,npc.world(),npc.x(),npc.y(),npc.id(),true,
        ()->{throw new AssertionError("A repeat must not allocate an item");});
    assertFalse(repeated.changed()); assertEquals(reloaded,repeated.progress());
    assertEquals("npc.kilhiam.temple",repeated.messageKey());
    assertFalse(CrimsonscaleLetterRules.hasUnsupportedCombatFlags(reloaded.questFlags()));
    assertTrue(CrimsonscaleLetterRules.hasUnsupportedCombatFlags(Map.of("unimplemented.quest",1)));
  }

  @Test void interactionChecksAuthoritativeLocationAndSightBeforeRewarding() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();var npc=catalog.npcs().getFirst();
    var interactions=new OnlineNpcInteractions(catalog,ItemCatalog.loadBundled());
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(progress,npc.world(),npc.x()+100,npc.y(),npc.id(),true,UUID::randomUUID));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(progress,npc.world(),npc.x(),npc.y(),npc.id(),false,UUID::randomUUID));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(progress,npc.world()+1,npc.x(),npc.y(),npc.id(),true,UUID::randomUUID));
    assertThrows(IllegalArgumentException.class,()->interactions.prepare(progress,npc.world(),npc.x(),npc.y(),"OtherNpc",true,UUID::randomUUID));
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void questFlagAndLetterCommitTogetherAndReceiptRetryCannotDuplicateReward() throws Exception {
    var store=com.perso.T4C.server.persistence.OnlineStore.fromEnvironment();store.migrate();
    var account=store.provisionAccount();var catalog=NpcInteractionCatalog.loadBundled();var npc=catalog.npcs().getFirst();
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var created=store.create(account.accountId(),"Letter transaction",npc.world(),npc.x(),npc.y(),progress,0);
    var interactions=new OnlineNpcInteractions(catalog,ItemCatalog.loadBundled());
    var session=store.acquire(account.secret(),created.id());
    var draft=interactions.prepare(session.character().progress(),npc.world(),npc.x(),npc.y(),npc.id(),true,UUID::randomUUID);
    var event=UUID.randomUUID();
    var committed=store.commitGameplayEvent(session,created.revision(),event,0,0,draft.progress());
    assertTrue(committed.applied());store.release(session);
    var rejoined=store.acquire(account.secret(),created.id());
    try {
      assertFalse(store.commitGameplayEvent(rejoined,created.revision(),event,0,0,draft.progress()).applied());
      var saved=store.load(account.accountId(),created.id());
      assertEquals(draft.progress(),saved.progress());assertEquals(created.gold(),saved.gold());assertEquals(created.experience(),saved.experience());
      assertFalse(interactions.prepare(saved.progress(),npc.world(),npc.x(),npc.y(),npc.id(),true,UUID::randomUUID).changed());
    } finally { store.release(rejoined); }
  }
}
