package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.MonsterCombatRules;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterSpellDamageRules;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** The directed authored pair must commit through the same bounded creature worker as movement. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineClanCombatTest {
  @Test void goblinBombermanSpellCanKillPassiveHorseWithoutPlayerRewardAndRestartKeepsItsLife() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var canonical=MonsterCatalog.loadBundled();
    var classified=MonsterCatalog.loadClassifiedBundled(canonical);
    var bomberJson=new Gson().toJsonTree(canonical.monsters().get("Goblin Bomberman")).getAsJsonObject();
    var authoredSpell=canonical.monsters().get("Goblin Bomberman").getAttacks().stream()
        .filter(MonsterDef.Attack::isSpell).findFirst().orElseThrow();
    assertEquals(10219,authoredSpell.getSpellId());
    bomberJson.add("attacks",new Gson().toJsonTree(List.of(authoredSpell)));
    MonsterDef bomber=new Gson().fromJson(bomberJson,MonsterDef.class);
    var horseJson=new Gson().toJsonTree(canonical.monsters().get("Wild Horse")).getAsJsonObject();
    horseJson.addProperty("health",1);horseJson.addProperty("acMin",0);horseJson.addProperty("acMax",0);
    horseJson.addProperty("agi",1);
    horseJson.addProperty("respawnTime",300_000);
    MonsterDef horse=new Gson().fromJson(horseJson,MonsterDef.class);
    assertTrue(OnlineLootDrafts.supports(horse,ItemCatalog.loadBundled()));
    var expectedRolls=rolls();
    var sourceStats=MonsterCombatRules.resolve(bomber);
    var targetStats=MonsterCombatRules.resolve(horse);
    int rolled=OnlineMonsterSpellAttacks.rawDamage(bomber,authoredSpell.getSpellId(),expectedRolls);
    int raw=MonsterSpellDamageRules.balanceElementalDamage(authoredSpell.getSpellId(),
        Math.max(1,bomber.getLevel()),rolled,bomber.getName());
    int authoredSkill=Math.max(1,authoredSpell.getValue1());
    var attacker=new CombatProfile(sourceStats.level(),sourceStats.strength(),sourceStats.endurance(),
        sourceStats.agility(),authoredSkill,authoredSkill,sourceStats.dodge(),
        MonsterCombatRules.rollArmor(sourceStats.armorMin(),sourceStats.armorMax(),expectedRolls),
        false,false,true,Map.of());
    int expectedDamage=CombatResolver.resolve(new PhysicalAttackRequest(attacker,
        targetStats.profile(false,expectedRolls),raw,0,false),expectedRolls).damage();
    assertTrue(expectedDamage>0,"The authored spell damage must land this fixed combat roll");
    var hunter=new MonsterContent.Spawn("Goblin Bomberman","Goblin Bomberman",3,3,0,false,true);
    // Adjacent engagement keeps the established clan attack cadence while choosing its spell.
    var prey=new MonsterContent.Spawn("Horse","Wild Horse",4,3,0,false,false);
    var content=new MonsterContent(MonsterContent.VERSION,
        Map.of(bomber.getName(),bomber,horse.getName(),horse),List.of(hunter,prey));
    var map=new PreviewMap(new boolean[10][10],0,0,3,3);
    UUID hunterLife,preyLife;
    var world=new PreviewWorld(map,content);
    try(var creatures=new PersistentCreatures(store,world,map,"start",null,ItemCatalog.loadBundled(),classified)) {
      hunterLife=creatures.life(1).eventId();preyLife=creatures.life(2).eventId();
      var combat=new OnlineClanCombat(map,world,creatures,classified,rolls(),System::currentTimeMillis);
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      OnlineStore.SpawnState saved=store.loadOrCreateSpawn(prey.persistenceKey());
      while(saved.diedAt()==null && System.nanoTime()<deadline) {
        creatures.step(List.of(),null);combat.step();
        Thread.sleep(5);
        saved=store.loadOrCreateSpawn(prey.persistenceKey());
      }
      assertNotNull(saved.diedAt(),"A real directed authored spell must kill the one-HP passive defender");
      assertEquals("CLAN",saved.deathKind());assertNull(saved.recipient());assertNull(saved.claimedAt());
      assertEquals(Math.max(0,horse.getHealth()-expectedDamage),saved.health());
      assertEquals(preyLife,saved.eventId());
      assertEquals(hunterLife,store.loadOrCreateSpawn(hunter.persistenceKey()).eventId());
      creatures.step(List.of(),null);
      assertEquals(1,world.snapshot(world.join().token()).creatures().size(),
          "Only the committed living goblin remains visible");
    }
    var restarted=new PreviewWorld(map,content);
    try(var recovered=new PersistentCreatures(store,restarted,map,"start",null,ItemCatalog.loadBundled(),classified)) {
      var dead=recovered.life(2);
      assertEquals(preyLife,dead.eventId());assertEquals(0,dead.health());
      assertEquals("CLAN",dead.deathKind());assertNotNull(dead.diedAt());
      assertEquals(hunterLife,recovered.life(1).eventId());
      assertEquals(1,restarted.snapshot(restarted.join().token()).creatures().size());
    }
  }

  private static Random rolls() {
    // Force a low dodge roll so this zero-skill spell exercises damage instead of a miss.
    return new Random(1) { @Override public int nextInt(int bound) {
      if (bound==90) return 0;
      return bound>=80 ? Math.min(30,bound-1) : bound-1;
    } };
  }
}
