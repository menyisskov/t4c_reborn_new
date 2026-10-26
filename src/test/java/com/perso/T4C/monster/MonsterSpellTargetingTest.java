package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterSpellRules;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.NpcCastVfxHook;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.spell.SpellVisualResolver;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MonsterSpellTargetingTest {
  @Test
  void legacyDrakeFireballsUseFireResistance() {
    assertEquals("fire", com.perso.T4C.monster.core.MonsterSpellRules.fallbackElement(10095));
    assertEquals(
        SpellVisualResolver.resolve(SpellRegistry.findById(10018)),
        SpellVisualResolver.resolve(MonsterSpellRules.visualSpell(10095)),
        "The legacy fireball reuses the asset-audited Fireball visuals");
    org.junit.jupiter.api.Assertions.assertFalse(
        com.perso.T4C.monster.core.MonsterSpellRules.usesArmor(10095));
    assertTrue(
        com.perso.T4C.monster.core.MonsterSpellRules.usesArmor(
            com.perso.T4C.divine.DivinePantheon.find("artemis").judgmentSpellId()));
  }

  @Test
  void archDrakeFireballHasBossPressureWithoutIncreasingOrdinaryDrakeBursts() {
    assertEquals(1800, MonsterSpellRules.elementalDamageLimit(10095, 1000, "Arch Drake"));
    assertEquals(1800, MonsterSpellRules.balanceRawDamage(10095, 1000, 2000, "Arch Drake"));
    assertEquals(500, MonsterSpellRules.balanceRawDamage(10095, 1000, 2000, "Greater Drake"));
  }

  @Test
  void elementalImpactRunsHealingAuraOnceBeforeApplyingResistedDamage() throws Exception {
    Player player = new Player();
    player.setMaxHp(10_000);
    player.setCurrentHp(1000);
    player.setRebirthCount(50);
    var impacts = new AtomicInteger();
    var healed = new AtomicInteger();
    int applied =
        MonsterSpellRules.applyElementalHit(
            player,
            500,
            250,
            () -> {
              impacts.incrementAndGet();
              var proc =
                  SeraphAuraService.onHit(
                      player,
                      0,
                      new java.util.Random() {
                        @Override
                        public int nextInt(int bound) {
                          return 0;
                        }
                      });
              assertTrue(proc.healingTriggered());
              healed.set(proc.totalHealing());
              player.applyHeal(proc.totalHealing(), proc.totalHealing());
            });
    assertEquals(1, impacts.get());
    assertEquals(MonsterSpellRules.damageAfterResistance(500, 250), applied);
    assertEquals(1000 + healed.get() - applied, player.getCurrentHp());
  }

  @Test
  void aFourteenDrakeEightSecondFireballBurstIsSurvivableAtReportedResistanceWithoutAura()
      throws Exception {
    var drake = new SpellMonster(10095, 250);
    var damage = new AtomicInteger();
    drake.setDamageCallback(
        (attacker, raw) ->
            damage.addAndGet(
                com.perso.T4C.monster.core.MonsterSpellRules.damageAfterResistance(raw, 652)));
    // Six initially-ready volleys fit in eight seconds at the unchanged 1.5-second
    // cooldown. Every fireball rolls its maximum, without aura, misses or kills.
    for (int hit = 0; hit < 14 * 6; hit++) drake.cast();
    assertTrue(
        damage.get() < 9000,
        "The reported 9,000-HP baseline must survive this maximum-roll spell burst");
    int worstRaw = com.perso.T4C.monster.core.MonsterSpellRules.maximumElementalDamage(250);
    assertEquals(
        14 * 6 * com.perso.T4C.monster.core.MonsterSpellRules.damageAfterResistance(worstRaw, 652),
        damage.get());
  }

  @Test
  void drakeHealingRestoresTheCasterWithoutDamagingItsTarget() throws Exception {
    var monster = new SpellMonster(10616);
    monster.takeDamage(100);
    var receivedDamage = new AtomicInteger();
    monster.setDamageCallback((attacker, amount) -> receivedDamage.addAndGet(amount));

    var healingVisuals = new AtomicInteger();
    NpcCastVfxHook.setShared(
        null,
        (spell, position) -> {
          assertEquals(MonsterSpellRules.visualSpell(10616), spell);
          assertEquals(monster.getPosition(), position);
          healingVisuals.incrementAndGet();
        });
    try {
      monster.cast();
    } finally {
      NpcCastVfxHook.setShared(null);
    }

    assertEquals(monster.getMaxHealth(), monster.getHealth(), "Healing stays within maximum HP");
    assertEquals(0, receivedDamage.get(), "The healing spell cannot hit the player");
    assertEquals(1, healingVisuals.get(), "The caster's healing is visible");
  }

  @Test
  void anOffensiveDrakeSpellStillReachesTheDamageHandler() throws Exception {
    var monster = new SpellMonster(10095);
    monster.takeDamage(100);
    var receivedDamage = new AtomicInteger();
    monster.setDamageCallback((attacker, amount) -> receivedDamage.addAndGet(amount));

    monster.cast();

    assertTrue(receivedDamage.get() > 0);
    assertTrue(monster.getAnimations().isAttacking(), "Spell casts play a visible gesture");
    assertEquals(900, monster.getHealth(), "An offensive spell does not heal its caster");
  }

  private static final class SpellMonster extends BaseMonster {
    SpellMonster(int spellId) throws GameException {
      this(spellId, 500);
    }

    SpellMonster(int spellId, int level) throws GameException {
      super("Drake spell fixture", 0, 0, 1000, 0, 0, 0, 1, 1, 30000, null, null, null, null, null);
      combatLevel = level;
      random =
          new java.util.Random() {
            @Override
            public int nextInt(int bound) {
              return bound - 1;
            }
          };
      attacks = List.of(new MonsterDef.Attack("", 0, 100, spellId, 0, 20));
    }

    void cast() {
      performAttack(new Vector2(96, 0));
    }
  }
}
