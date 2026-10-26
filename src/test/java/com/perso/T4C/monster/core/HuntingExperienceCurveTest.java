package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.monster.FeyWarden;
import com.perso.T4C.monster.json.MonsterJsonDef;
import com.perso.T4C.spawn.HuntingGrounds;
import org.junit.jupiter.api.Test;

class HuntingExperienceCurveTest {
  @Test
  void liveFeyWardenPaceProjectsIntoTheRequestedFiveToTenHourRange() {
    var monster = MonsterRegistry.findByName("Fey Warden");
    long reward =
        MonsterExperience.reward(monster.getXpOnDeath())
            + MonsterExperience.damageReward(monster.getXpPerHit(), monster.getHealth());
    // The reported run gained forty levels in ten minutes before calibration. This is
    // that build's recorded full-kill reward, not a new balance constant.
    long previousReward = 392310600L;
    double hoursPerHundred = 10d / 60 * 100 / 40 * previousReward / reward;
    assertTrue(
        hoursPerHundred >= 5 && hoursPerHundred <= 10,
        "The same observed kill rate projects to " + hoursPerHundred + " hours per hundred levels");
  }

  @Test
  void registryAppliesTheHuntingFloorWithoutChangingCombatOrDamageExperience() {
    var authored = FeyWarden.definition();
    var registered = MonsterRegistry.findByName(authored.getName());
    assertTrue(registered.getXpOnDeath() > authored.getXpOnDeath());
    assertEquals(authored.getHealth(), registered.getHealth());
    assertEquals(authored.getXpPerHit(), registered.getXpPerHit());
    assertSame(registered, HuntingExperienceCurve.apply(registered));
  }

  @Test
  void lateHuntingRewardsGrowWithHealthAndContinuePastTheReferenceLevelCap() {
    long previous = 0;
    for (int level = 150; level <= 400; level++) {
      long reward = HuntingExperienceCurve.minimumBaseKillExperience(level, 50000);
      assertTrue(reward >= previous, "XP per HP regressed at level " + level);
      previous = reward;
    }
    assertEquals(
        HuntingExperienceCurve.minimumBaseKillExperience(350, 50000) * 2,
        HuntingExperienceCurve.minimumBaseKillExperience(350, 100000));
    assertTrue(
        HuntingExperienceCurve.minimumBaseKillExperience(400, 100000)
            > HuntingExperienceCurve.minimumBaseKillExperience(320, 100000));
    for (var ground : HuntingGrounds.all()) {
      var monster = MonsterRegistry.findByName(ground.monster());
      if (monster.getLevel() < 150 || monster.getLevel() > 400) continue;
      assertTrue(
          monster.getXpOnDeath()
              >= HuntingExperienceCurve.minimumBaseKillExperience(
                  monster.getLevel(), monster.getHealth()),
          ground.monster() + " bypasses the hunting progression");
    }
  }

  @Test
  void preservesUnrewardingEncountersOtherLevelBandsAndStrongerAuthoredAwards() {
    for (var definition :
        java.util.List.of(
            definition(149, 10000, 100),
            definition(401, 10000, 100),
            definition(200, 10000, 0),
            definition(200, 10000, 2000000000))) {
      assertSame(definition, HuntingExperienceCurve.apply(definition));
    }
    var peaceful = new MonsterJsonDef();
    peaceful.name = "Peaceful witness";
    peaceful.level = 200;
    peaceful.health = 10000;
    peaceful.xpOnDeath = 100;
    peaceful.canAttack = false;
    var definition = peaceful.toMonsterDef();
    assertSame(definition, HuntingExperienceCurve.apply(definition));
  }

  @Test
  void largeBossAwardsSaturateTheAuthoredIntBeforeTheLongCombatMultiplier() {
    var boss = HuntingExperienceCurve.apply(definition(400, Integer.MAX_VALUE, 1));
    assertEquals(Integer.MAX_VALUE, boss.getXpOnDeath());
    assertEquals(4294967294L, MonsterExperience.reward(boss.getXpOnDeath()));
  }

  private static MonsterDef definition(int level, int health, int experience) {
    var definition = new MonsterJsonDef();
    definition.name = "Hunting experience fixture";
    definition.level = level;
    definition.health = health;
    definition.xpOnDeath = experience;
    return definition.toMonsterDef();
  }
}
