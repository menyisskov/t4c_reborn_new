package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class OnlineSneakTest {
  private static final long NOW=10_000L;

  @Test void activationCommitsHiddenSentinelAndCooldownWithoutChangingSource() {
    var before=progress(Map.of("sneak",35),Map.of());
    var hidden=OnlineSneak.activate(before,NOW);
    assertEquals(Long.MAX_VALUE,hidden.hiddenUntil());
    assertEquals(NOW+OnlineSneak.COOLDOWN_MILLIS,hidden.cooldowns().get("sneak"));
    assertEquals(0,before.hiddenUntil());
    assertFalse(before.cooldowns().containsKey("sneak"));
  }

  @Test void deadUnlearnedStunnedAndCoolingDownAttemptsRefuseBeforeDrafting() {
    assertRefused(progress(Map.of(),Map.of()),"not_learned");
    var dead=progress(Map.of("sneak",1),Map.of());
    assertRefused(withVitals(dead,new CharacterProgress.Vitals(0,dead.vitals().maxHp(),dead.vitals().mana(),dead.vitals().maxMana())),"dead");
    assertRefused(progress(Map.of("sneak",1),Map.of("stun",NOW+10)),"stunned");
    assertRefused(progress(Map.of("sneak",1),Map.of("sneak",NOW+10)),"cooldown");
  }

  @Test void movingUpkeepUsesLearnedSkillAndWitnessCountToRevealOnFailedRoll() throws Exception {
    var items=ItemCatalog.loadBundled();
    var hidden=OnlineSneak.activate(progress(Map.of("sneak",1),Map.of()),NOW);
    var worst=new Random(1) { @Override public int nextInt(int bound) { return bound-1; } };
    assertFalse(OnlineSneak.remainsHidden(hidden,items,NOW,0,worst));
    assertTrue(OnlineSneak.remainsHidden(OnlineSneak.activate(progress(Map.of("sneak",100),Map.of()),NOW),items,NOW,0,new Random(2) {
      @Override public int nextInt(int bound) { return 0; }
    }));
    assertFalse(OnlineSneak.remainsHidden(hidden,items,NOW,3,new Random(3) { @Override public int nextInt(int bound) { return 0; } }));
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void activationRefusalDoesNotWriteAndSuccessfulSneakPersistsForDetectHiddenObservers() throws Exception {
    var store=com.perso.T4C.server.persistence.OnlineStore.fromEnvironment();store.migrate();
    var account=store.provisionAccount();
    var map=new PreviewMap(new boolean[20][20],0,0,4,5);
    var world=new PreviewWorld(map);
    var sessions=new PersistentSessions(store,map,world);
    try {
      long now=System.currentTimeMillis();
      sessions.wallClock=()->now;
      var sneaky=withSkill(progress(Map.of(),Map.of()),"sneak",25);
      var observerBase=progress(Map.of(),Map.of());
      var observer=ProgressEdits.withCooldowns(observerBase,Map.of("detect_hidden_until",now+60_000));
      var createdSneak=store.create(account.accountId(),"Sneak test",0,4.5,5.5,sneaky,0);
      var createdObserver=store.create(account.accountId(),"Sneak witness",0,5.5,5.5,observer,0);
      var sneakJoined=sessions.join(store.acquire(account.secret(),createdSneak.id()),System.nanoTime());
      var sneakyToken=sneakJoined.token();
      var observerToken=sessions.join(store.acquire(account.secret(),createdObserver.id()),System.nanoTime()).token();

      var refused=assertThrows(PreviewWorld.Rejected.class,() -> sessions.sneak(observerToken,0));
      assertEquals("not_learned",refused.getMessage());
      assertEquals(createdObserver,store.load(account.accountId(),createdObserver.id()),"a refusal must not write");

      var pending=sessions.sneak(sneakyToken,0).toCompletableFuture();
      long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
      while(!pending.isDone() && System.nanoTime()<deadline) { sessions.step();Thread.sleep(5); }
      var committed=pending.get().character();
      assertEquals(Long.MAX_VALUE,committed.progress().hiddenUntil());
      assertEquals(now+OnlineSneak.COOLDOWN_MILLIS,committed.progress().cooldowns().get("sneak"));
      assertTrue(world.snapshot(observerToken,now).players().stream()
          .anyMatch(actor -> actor.id()==sneakJoined.id()),
          "Detect Hidden keeps the concealed actor in the observer view");
    } finally { sessions.shutdown(); }
  }

  private static void assertRefused(CharacterProgress p,String reason) {
    var original=p;
    assertEquals(reason,assertThrows(IllegalArgumentException.class,() -> OnlineSneak.activate(original,NOW)).getMessage());
    assertEquals(original,p,"a refused attempt must not draft a write");
  }

  private static CharacterProgress withVitals(CharacterProgress p,CharacterProgress.Vitals v) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),v,p.advancement(),p.inventory(),
        p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
  }

  private static CharacterProgress progress(Map<String,Integer> skills,Map<String,Long> cooldowns) {
    var base=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(4)).progress();
    return new CharacterProgress(base.version(),base.characterClass(),base.gender(),base.attributes(),base.vitals(),base.advancement(),
        List.of(),Map.of(),skills,Set.of(),Map.of(),List.of(),0,base.respawn(),List.of(),cooldowns,0,base.companion());
  }

  private static CharacterProgress withSkill(CharacterProgress p,String skill,int level) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),p.inventory(),
        p.equipment(),Map.of(skill,level),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
  }
}
