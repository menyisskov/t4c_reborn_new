package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class OnlineProgressionTest {
  private final XpCurve curve=XpCurve.loadDefault();

  @Test void sharedProgressionCrossesMultipleLevelsAndPreservesOwnedState() {
    var p=wounded(1);
    long oldXp=curve.getXpToNextLevel(1)-1;
    var before=state(p,oldXp);
    long award=curve.getTotalXp(3)+7-oldXp;
    var result=OnlineProgression.award(before,award,100,new Random(7));
    var after=result.progress();
    assertEquals(award,result.experience());
    assertEquals(3,after.advancement().level());
    assertEquals(p.advancement().statPoints()+10,after.advancement().statPoints());
    assertEquals(p.advancement().skillPoints()+30,after.advancement().skillPoints());
    assertEquals(after.vitals().maxHp(),after.vitals().hp());
    assertEquals(after.vitals().maxMana(),after.vitals().mana());
    assertTrue(after.vitals().maxHp()>p.vitals().maxHp());
    assertEquals(p.inventory(),after.inventory()); assertEquals(p.equipment(),after.equipment());
    assertEquals(p.attributes(),after.attributes()); assertEquals(p.questFlags(),after.questFlags());
    assertEquals(1,p.vitals().hp(),"Drafting cannot mutate committed state");
    var noLevel=OnlineProgression.award(state(p,0),1,100,new Random(7));
    assertEquals(p.vitals(),noLevel.progress().vitals(),"XP without leveling cannot heal");
  }

  @Test void hugeAwardStopsAtExistingCapAndFurtherXpCannotHealOrOverflow() {
    int cap=GameConstants.MAX_PLAYER_LEVEL;
    var p=wounded(cap-1);
    long total=curve.getTotalXp(cap)-1;
    var result=OnlineProgression.award(state(p,total),Long.MAX_VALUE,100,new Random(1));
    assertEquals(1,result.experience());
    assertEquals(cap,result.progress().advancement().level());
    var atCap=wounded(cap);
    var unchanged=OnlineProgression.award(state(atCap,curve.getTotalXp(cap)),Long.MAX_VALUE,100,new Random(1));
    assertEquals(0,unchanged.experience()); assertEquals(atCap,unchanged.progress());
    assertThrows(PreviewWorld.Rejected.class,() -> OnlineProgression.award(state(p,0),1,100,new Random(1)));
    assertThrows(PreviewWorld.Rejected.class,() -> OnlineProgression.award(state(wounded(1),curve.getTotalXp(2)),1,100,new Random(1)),
        "Legacy ledger awards cannot silently roll retroactive level gains without migration");
  }

  @Test void statAllocationValidatesAndDraftsAllFiveAttributesWithoutMutatingSource() {
    var original = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3)).progress();
    var progress = withStatPoints(original, 3);
    var state = state(progress, 0);

    var allocation = OnlineProgression.allocateStats(state, java.util.Map.of("strength", 2, "wisdom", 1));
    assertEquals(3, allocation.pointsSpent());
    assertEquals(progress.attributes().strength() + 2, allocation.progress().attributes().strength());
    assertEquals(progress.attributes().wisdom() + 1, allocation.progress().attributes().wisdom());
    assertEquals(0, allocation.progress().advancement().statPoints());
    assertEquals(3, progress.advancement().statPoints(), "Drafting cannot mutate committed state");

    assertAllocationRefused(state, java.util.Map.of(), "invalid_allocation");
    assertAllocationRefused(state, java.util.Map.of("unknown", 1), "invalid_allocation");
    assertAllocationRefused(state, java.util.Map.of("strength", 0), "invalid_allocation");
    assertAllocationRefused(state, java.util.Map.of("strength", Integer.MAX_VALUE, "wisdom", 1), "invalid_allocation");
    assertAllocationRefused(state, java.util.Map.of("strength", 4), "insufficient_stat_points");
  }

  private static void assertAllocationRefused(OnlineStore.CharacterState state, java.util.Map<String, Integer> allocation,
      String reason) {
    var rejected = assertThrows(PreviewWorld.Rejected.class,
        () -> OnlineProgression.allocateStats(state, allocation));
    assertEquals(reason, rejected.getMessage());
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void levelAndRefillPersistWithDamageAndRetryDoesNotRerollOrRegrant() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate(); var key=store.provisionAccount();
    var p=wounded(1);
    var created=store.create(key.accountId(),"Progression recovery",0,3,3,p,0);
    var session=store.acquire(key.secret(),created.id());
    var life=store.loadOrCreateSpawn("progression:"+UUID.randomUUID());
    var result=OnlineProgression.award(created,curve.getXpToNextLevel(1),100,new Random(7));
    var strike=new OnlineStore.CreatureStrike(life.key(),life.eventId(),10,10,0);
    var committed=store.commitCreatureStrike(session,0,life.eventId(),0,result.experience(),result.progress(),strike);
    assertEquals(2,committed.character().progress().advancement().level());
    store.release(session);
    var restarted=OnlineStore.fromEnvironment();
    var rejoined=restarted.acquire(key.secret(),created.id());
    assertEquals(committed.character(),rejoined.character());
    assertFalse(restarted.commitCreatureStrike(rejoined,0,life.eventId(),0,result.experience(),result.progress(),strike).applied());
    assertEquals(committed.character(),restarted.load(key.accountId(),created.id()));
  }

  private static OnlineStore.CharacterState state(CharacterProgress p,long experience) {
    return new OnlineStore.CharacterState(UUID.randomUUID(),UUID.randomUUID(),"Progression",0,3,3,0,experience,0,p);
  }

  private static CharacterProgress wounded(int level) {
    var p=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3)).progress();
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),
        new CharacterProgress.Vitals(1,p.vitals().maxHp(),0,p.vitals().maxMana()),
        new CharacterProgress.Advancement(level,p.advancement().statPoints(),p.advancement().skillPoints(),0,0),
        p.inventory(),p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),
        p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
  }

  private static CharacterProgress withStatPoints(CharacterProgress p, int statPoints) {
    return new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(), p.vitals(),
        new CharacterProgress.Advancement(p.advancement().level(), statPoints, p.advancement().skillPoints(),
            p.advancement().rebirths(), p.advancement().karma()), p.inventory(), p.equipment(), p.skills(), p.spells(),
        p.questFlags(), p.storage(), p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(), p.hiddenUntil(),
        p.companion());
  }
}
