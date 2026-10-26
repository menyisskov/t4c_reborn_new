package com.perso.T4C.monster.core;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.monster.GreaterDrake;
import com.perso.T4C.monster.TheVerdantWarden;
import com.perso.T4C.monster.Training1Skeleton;
import com.perso.T4C.monster.VeilboundWraith;
import com.perso.T4C.monster.WarbandRaider;
import com.perso.T4C.monster.json.MonsterJsonDef;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class HuntingDamageCurveTest {
  @Test
  void fourteenWraithsCannotKillTheReportedArcherInTwoSecondsWithoutAuraHealing() {
    var authored = VeilboundWraith.definition();
    var monster = MonsterRegistry.findByName(authored.getName());
    var formula = DiceFormula.of(monster.getAttacks().getFirst().getName());
    int worstHit = formula.max();
    assertTrue(worstHit < DiceFormula.of(authored.getAttacks().getFirst().getName()).max());
    assertEquals(worstHit, monster.getHitDamageMax());
    assertEquals(formula.min(), monster.getHitDamageMin());
    assertEquals(authored.getHealth(), monster.getHealth());
    assertSame(monster, HuntingDamageCurve.apply(monster));
    var attacker =
        new CombatProfile(
            monster.getLevel(), 290, 340, 435, 5820, 1, 1, 0, false, false, false, Map.of());
    var archer = new CombatProfile(243, 270, 550, 1455, 1, 1, 1, 432, false, false, true, Map.of());
    var random = new Random(1);
    int hp = 9000;
    // At the unchanged 1.5-second cooldown, an initially ready pack can land two
    // complete volleys in two seconds. Assume every hit lands at maximum damage.
    int maximumMitigatedHit = Math.max(0, worstHit - 432);
    assertTrue(hp > 2 * 14 * maximumMitigatedHit);
    for (int hit = 0; hit < 2 * 14; hit++) {
      hp -=
          CombatResolver.resolve(
                  new PhysicalAttackRequest(attacker, archer, worstHit, 0, false), random)
              .damage();
    }
    assertTrue(hp > 0, "No rebirth or aura healing is used in this two-volley check");
  }

  @Test
  void bossesAndTrainingChampionsRetainTheirAuthoredAttacks() {
    var boss = TheVerdantWarden.definition();
    var champion = Training1Skeleton.definition();
    assertSame(boss, HuntingDamageCurve.apply(boss));
    assertSame(champion, HuntingDamageCurve.apply(champion));
    assertFalse(HuntingDamageCurve.appliesTo(boss));
    assertFalse(HuntingDamageCurve.appliesTo(champion));
  }

  @Test
  void newRegularMonstersAreCoveredOutsideFieldsAndOutsideTheOldLevelBand() {
    // Different former exclusions: a below-150 camp enemy, and a mixed-spell
    // field enemy above the player cap. Both retain their XP and attack identity.
    for (var authored : List.of(WarbandRaider.definition(), GreaterDrake.definition())) {
      var balanced = MonsterRegistry.findByName(authored.getName());
      assertTrue(HuntingDamageCurve.appliesTo(balanced));
      assertTrue(
          DiceFormula.of(balanced.getAttacks().getFirst().getName()).max()
              <= HuntingDamageCurve.maximumRawDamage(balanced.getLevel()));
      assertEquals(
          DiceFormula.of(balanced.getAttacks().getFirst().getName()).max(),
          balanced.getHitDamageMax(),
          "Mixed spell users must also publish their reduced melee range");
      assertEquals(
          authored.getAttacks().getFirst().getValue1(),
          balanced.getAttacks().getFirst().getValue1());
      assertEquals(authored.getHealth(), balanced.getHealth());
    }
  }

  @Test
  void additionalDefinitionsCapFallbackMeleeAndSpellsWithoutCappingSelfHealing() throws Exception {
    var json = new MonsterJsonDef();
    json.name = "New damage budget fixture";
    json.level = 10;
    json.hitDamageMin = 1000;
    json.hitDamageMax = 1000;
    var authored = json.toMonsterDef();
    try {
      MonsterRegistry.registerAdditionalDefinitions(List.of(authored));
      var balanced = MonsterRegistry.findByName(json.name);
      int cap = HuntingDamageCurve.maximumRawDamage(json.level);
      assertEquals(cap, balanced.getHitDamageMax(), "No attack list may bypass the registry rule");
      var monster = new BudgetMonster(authored);
      assertEquals(cap, monster.roll(), "Fallback damage is capped even for an authored instance");
      monster.use(new MonsterDef.Attack("if(1=1?1d1000:1d1000)", 100, 100, 0, 0, 1));
      assertEquals(cap, monster.roll(), "Special melee formulas cannot bypass the runtime cap");
      monster.use(new MonsterDef.Attack("", 100, 100, 10095, 0, 20));
      assertEquals(
          MonsterSpellRules.maximumElementalDamage(json.level),
          monster.roll(),
          "Elemental spell damage uses its global budget before resistance");
      monster.use(new MonsterDef.Attack("", 100, 100, 10616, 0, 20));
      assertEquals(1000, monster.roll(), "Caster healing is not outgoing damage");
      assertEquals(authored.getXpOnDeath(), balanced.getXpOnDeath());
    } finally {
      MonsterRegistry.resetToGeneratedDefinitions();
    }
    assertFalse(
        HuntingDamageCurve.appliesTo(authored), "Reset also clears additional-content provenance");
  }

  private static final class BudgetMonster extends BaseMonster {
    BudgetMonster(MonsterDef definition) throws GameException {
      super(
          definition.getName(),
          0,
          0,
          1000,
          0,
          0,
          0,
          1000,
          1000,
          30000,
          null,
          null,
          null,
          null,
          null);
      applyCombatDefinition(definition);
      random =
          new Random() {
            @Override
            public int nextInt(int bound) {
              return bound - 1;
            }
          };
    }

    void use(MonsterDef.Attack attack) {
      attacks = List.of(attack);
    }

    int roll() {
      return rollDamage();
    }

    @Override
    protected int rollMonsterSpellDamage(int spellId) {
      return 1000;
    }
  }
}
