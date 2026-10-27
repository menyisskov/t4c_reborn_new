package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class OnlineDeathTest {
  private static final Random DROP = new Random() { @Override public double nextDouble() { return 0; } };

  @Test void penaltyPreservesEquippedAndUndroppableItemsAndRestoresOnlyHalfHealth() throws Exception {
    var p=fixture();
    var state=new OnlineStore.CharacterState(UUID.randomUUID(),UUID.randomUUID(),"Death rules",0,10,20,50,100,0,p);
    var result=OnlineDeath.prepare(state,ItemCatalog.loadBundled(),new CharacterProgress.Respawn(0,10,20),p.respawn(),DROP);
    assertEquals(5,result.transition().experienceLost());
    assertEquals(p.vitals().maxHp()/2,result.progress().vitals().hp());
    assertEquals(p.vitals().mana(),result.progress().vitals().mana());
    assertEquals(p.equipment(),result.progress().equipment());
    var catalog=ItemCatalog.loadBundled();
    for(var item:p.inventory()) {
      boolean protectedItem=p.equipment().containsValue(item.id()) || catalog.items().get(item.key()).undroppable();
      assertEquals(protectedItem,result.progress().inventory().contains(item));
      assertEquals(!protectedItem,result.transition().droppedItems().contains(item));
    }
    assertFalse(result.transition().droppedItems().isEmpty());
    assertEquals(0,result.transition().goldLost());
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void fatalDamagePenaltyAndCorpseRecoverAtomicallyAcrossRestartAndRetries() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var created=store.create(account.accountId(),"Death recovery",0,10,20,fixture(),50);
    var session=store.acquire(account.secret(),created.id());
    var saved=store.reward(session,UUID.randomUUID(),0,100).character();
    var life=store.loadOrCreateSpawn("death:"+UUID.randomUUID());
    saved=store.commitGameplayEvent(session,saved.revision(),UUID.randomUUID(),0,0,saved.progress(),
        new OnlineStore.CreatureProvocation(life.key(),life.eventId(),created.id(),0)).character();
    var death=OnlineDeath.prepare(saved,ItemCatalog.loadBundled(),new CharacterProgress.Respawn(0,10,20),new CharacterProgress.Respawn(0,3,4),DROP);
    var attack=new OnlineStore.MonsterAttack(life.key(),life.eventId(),created.id(),0,System.currentTimeMillis()+1500);
    long targetedRevision=store.loadOrCreateSpawn(life.key()).stateRevision();
    var bad=new OnlineStore.DeathTransition(101,death.transition().destination(),death.transition().corpsePosition(),death.transition().droppedItems(),0,0);
    final long revision=saved.revision();var event=UUID.randomUUID();
    assertThrows(OnlineStore.Conflict.class,()->store.commitMonsterAttack(session,revision,event,death.progress(),attack,bad));
    assertEquals(saved,store.load(account.accountId(),created.id()));
    assertEquals(0,store.loadOrCreateSpawn(life.key()).nextAttackAt(),"Failed death must roll back attack clock too");
    var committed=store.commitMonsterAttack(session,revision,event,death.progress(),attack,death.transition());
    assertEquals(95,committed.character().experience());assertEquals(3,committed.character().x());
    assertEquals(4,committed.character().y());
    var cleared=store.loadOrCreateSpawn(life.key());
    assertNull(cleared.targetCharacter());assertNull(cleared.targetEpoch());
    assertEquals("RETURN",cleared.mode());assertTrue(cleared.stateRevision()>targetedRevision);
    store.release(session);
    var restarted=OnlineStore.fromEnvironment();var rejoined=restarted.acquire(account.secret(),created.id());
    assertFalse(restarted.commitMonsterAttack(rejoined,revision,event,death.progress(),attack,death.transition()).applied());
    var corpse=restarted.listCorpses(rejoined).getFirst();
    assertEquals(death.transition().droppedItems(),corpse.items());assertEquals(event,corpse.id());
    assertThrows(OnlineStore.Conflict.class,()->restarted.recoverCorpse(rejoined,committed.character().revision(),event,null,0,1000,1000,UNLIMITED));
    assertEquals(committed.character(),restarted.load(account.accountId(),created.id()));
    var otherAccount=store.provisionAccount();var other=store.create(otherAccount.accountId(),"Other owner",0,10,20,fixture(),0);
    var otherSession=store.acquire(otherAccount.secret(),other.id());
    assertTrue(store.listCorpses(otherSession).isEmpty());
    assertThrows(OnlineStore.Conflict.class,()->store.recoverCorpse(otherSession,0,event,null,0,10,20,UNLIMITED));
    var recovered=restarted.recoverCorpse(rejoined,committed.character().revision(),event,null,0,10,20,UNLIMITED);
    assertEquals(new HashSet<>(created.progress().inventory()),new HashSet<>(recovered.character().progress().inventory()));
    assertEquals(95,recovered.character().experience());assertEquals(created.gold(),recovered.character().gold());
    assertTrue(restarted.listCorpses(rejoined).isEmpty());
    assertFalse(restarted.recoverCorpse(rejoined,committed.character().revision(),event,null,0,10,20,UNLIMITED).applied());
    store.release(otherSession);restarted.release(rejoined);
  }

  @Test void partialStackDropsSplitIdentityAndHugeStacksHaveBoundedWork() throws Exception {
    var p=fixture();var gson=new Gson();var tree=gson.toJsonTree(p).getAsJsonObject();
    var stack=tree.getAsJsonArray("inventory").get(p.inventory().size()-2).getAsJsonObject();
    stack.addProperty("quantity",Integer.MAX_VALUE);
    p=gson.fromJson(tree,CharacterProgress.class);var original=p.inventory().get(p.inventory().size()-2);
    var state=new OnlineStore.CharacterState(UUID.randomUUID(),UUID.randomUUID(),"Stack death",0,10,20,0,100,0,p);
    var catalog=ItemCatalog.loadBundled();
    var result=assertTimeoutPreemptively(java.time.Duration.ofSeconds(2),()->OnlineDeath.prepare(state,catalog,new CharacterProgress.Respawn(0,10,20),state.progress().respawn(),new Random(17)));
    var retained=result.progress().inventory().stream().filter(i->i.id().equals(original.id())).findFirst().orElseThrow();
    var dropped=result.transition().droppedItems().stream().filter(i->i.key().equals(original.key()) && i.charges()==original.charges()).findFirst().orElseThrow();
    assertNotEquals(original.id(),dropped.id());assertEquals(original.charges(),retained.charges());
    assertEquals(Integer.MAX_VALUE,(long)retained.quantity()+dropped.quantity());
    assertTrue(dropped.quantity()>0 && retained.quantity()>0);
  }

  private static final OnlineStore.CarryRules UNLIMITED = rules(Long.MAX_VALUE/4);

  /** One weight unit per item so the limit is exact: capacity is whatever the test says. */
  private static OnlineStore.CarryRules rules(long maximum) {
    return new OnlineStore.CarryRules() {
      public long maximumWeight(CharacterProgress progress) { return maximum; }
      public long unitWeight(String key) { return 1; }
    };
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void corpseItemsAreRecoveredIndividuallyUnderCarryWeightWithPartialTakes() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();var account=store.provisionAccount();
    var created=store.create(account.accountId(),"Corpse weight",0,10,20,fixture(),0);
    var session=store.acquire(account.secret(),created.id());
    var saved=created;
    var life=store.loadOrCreateSpawn("weight:"+UUID.randomUUID());
    saved=store.commitGameplayEvent(session,saved.revision(),UUID.randomUUID(),0,0,saved.progress(),
        new OnlineStore.CreatureProvocation(life.key(),life.eventId(),created.id(),0)).character();
    var death=OnlineDeath.prepare(saved,ItemCatalog.loadBundled(),new CharacterProgress.Respawn(0,10,20),new CharacterProgress.Respawn(0,10,20),DROP);
    var attack=new OnlineStore.MonsterAttack(life.key(),life.eventId(),created.id(),0,System.currentTimeMillis()+1500);
    var event=UUID.randomUUID();
    var dead=store.commitMonsterAttack(session,saved.revision(),event,death.progress(),attack,death.transition()).character();
    var dropped=death.transition().droppedItems();assertTrue(dropped.size()>=2,"fixture must drop at least two stacks");
    long carried=dead.progress().inventory().stream().mapToLong(i->i.quantity()).sum();
    // Wrong owner never sees or touches the corpse.
    var otherAccount=store.provisionAccount();var other=store.create(otherAccount.accountId(),"Not owner",0,10,20,fixture(),0);
    var otherSession=store.acquire(otherAccount.secret(),other.id());
    assertThrows(OnlineStore.Conflict.class,()->store.recoverCorpse(otherSession,0,event,null,0,10,20,UNLIMITED));
    // Room for the first stack only: it moves, the rest stays on the corpse.
    var tight=rules(carried+dropped.getFirst().quantity());
    var partial=store.recoverCorpse(session,dead.revision(),event,null,0,10,20,tight);
    assertTrue(partial.applied());
    assertTrue(partial.character().progress().inventory().contains(dropped.getFirst()));
    assertFalse(partial.character().progress().inventory().contains(dropped.get(1)));
    var left=store.listCorpses(session);assertEquals(1,left.size());
    assertEquals(dropped.subList(1,dropped.size()),left.getFirst().items());
    // A request that was already applied cannot take more by retrying with the old revision.
    assertThrows(OnlineStore.Conflict.class,()->store.recoverCorpse(session,dead.revision(),event,null,0,10,20,UNLIMITED));
    // A selected stack that does not fit is refused and nothing at all changes.
    var before=store.load(account.accountId(),created.id());
    var heavy=dropped.get(1);
    var refused=assertThrows(OnlineStore.Conflict.class,()->store.recoverCorpse(session,partial.character().revision(),event,heavy.id(),0,10,20,tight));
    assertEquals(OnlineStore.CORPSE_TOO_HEAVY,refused.getMessage());
    assertEquals(before,store.load(account.accountId(),created.id()));
    assertEquals(dropped.subList(1,dropped.size()),store.listCorpses(session).getFirst().items());
    // An item that is not on the corpse is unavailable, and the corpse is untouched.
    var missing=assertThrows(OnlineStore.Conflict.class,()->store.recoverCorpse(session,partial.character().revision(),event,UUID.randomUUID(),0,10,20,UNLIMITED));
    assertEquals(OnlineStore.CORPSE_ITEM_UNAVAILABLE,missing.getMessage());
    // With room, the selected stack moves alone.
    var one=store.recoverCorpse(session,partial.character().revision(),event,heavy.id(),0,10,20,UNLIMITED);
    assertTrue(one.character().progress().inventory().contains(heavy));
    if(dropped.size()>2) assertEquals(dropped.subList(2,dropped.size()),store.listCorpses(session).getFirst().items());
    // Taking the rest empties and closes the corpse; a repeat is acknowledged without applying.
    var rest=store.recoverCorpse(session,one.character().revision(),event,null,0,10,20,UNLIMITED);
    assertTrue(store.listCorpses(session).isEmpty());
    assertFalse(store.recoverCorpse(session,rest.character().revision(),event,null,0,10,20,UNLIMITED).applied());
    store.release(otherSession);store.release(session);
  }

  private static CharacterProgress fixture() throws Exception {
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3)).progress();
    var catalog=ItemCatalog.loadBundled();var gson=new Gson();var tree=gson.toJsonTree(p).getAsJsonObject();
    String ordinary=catalog.items().values().stream().filter(i->!i.undroppable()).map(i->i.key()).sorted().findFirst().orElseThrow();
    String protectedKey=catalog.items().values().stream().filter(i->i.undroppable()).map(i->i.key()).sorted().findFirst().orElseThrow();
    tree.getAsJsonArray("inventory").add(gson.toJsonTree(new CharacterProgress.Item(UUID.randomUUID(),ordinary,1,7)));
    tree.getAsJsonArray("inventory").add(gson.toJsonTree(new CharacterProgress.Item(UUID.randomUUID(),protectedKey,1,0)));
    return gson.fromJson(tree,CharacterProgress.class);
  }
}
