package com.perso.T4C.divine;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.DamageCallback;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DivineAfflictionsTest {
  @AfterEach
  void resetCollision() {
    CollisionManager.getInstance().clear();
  }

  @Test
  void elderGodCastsStatusInsteadOfDamageAndSharesDebuffCooldown() throws Exception {
    CollisionManager.getInstance().clear();
    Player player = recipient();
    AtomicInteger casts = new AtomicInteger();
    TestAvatar avatar = new TestAvatar();
    avatar.onlySpell(DivineAfflictions.CURSE);
    avatar.setDamageCallback(
        new DamageCallback() {
          @Override
          public void applyDamage(BaseMonster source, int damage) {
            fail("A ranged debuff is not a physical attack");
          }

          @Override
          public void applySpell(BaseMonster source, int id, int damage) {
            assertEquals(0, damage);
            assertTrue(DivineAfflictions.apply(SpellRegistry.findById(id), player));
            casts.incrementAndGet();
          }
        });
    avatar.castAt(new Vector2(GRID_W * 3, 0));
    assertEquals(1, casts.get());
    assertFalse(avatar.canCastMonsterSpell(DivineAfflictions.ARMOR_MELT));
    assertTrue(avatar.canCastMonsterSpell(DivinePantheon.find("anubis").judgmentSpellId()));
    avatar.castAt(new Vector2(GRID_W * 3, 0));
    assertEquals(1, casts.get(), "Another debuff cannot bypass the shared cooldown");
    assertEquals(1000, player.getCurrentHp(), "A status cast must not damage the player");
    player.applyHeal(100, 100);
    assertEquals(1060, player.getCurrentHp());
  }

  @Test
  void afflictionsRefreshPersistAndExpireWithoutCompounding() throws Exception {
    Player player = recipient();
    for (int repeat = 0; repeat < 2; repeat++) {
      for (SpellData spell : DivineAfflictions.definitions()) {
        SpellData registered = SpellRegistry.findById(spell.getSpellId());
        assertNotNull(registered);
        assertFalse(SpellRegistry.playerCastableSpells().contains(registered));
        assertTrue(DivineAfflictions.apply(registered, player));
      }
      assertWeakening(player);
    }
    assertEquals(5, player.getActiveBuffs().size());
    Player loaded = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(player), loaded);
    // The fixture's plain armor buff is not a registry spell, so restore just its AC baseline.
    armorBaseline(loaded);
    assertWeakening(loaded);
    loaded.applyHeal(100, 100);
    assertEquals(1060, loaded.getCurrentHp(), "Reloading preserves the healing curse");
    var expiry = Player.ActiveBuff.class.getDeclaredField("expiresAtMillis");
    expiry.setAccessible(true);
    for (var buff : loaded.getActiveBuffs()) {
      if (!buff.getSpellName().equals("test armor")) expiry.setLong(buff, 0L);
    }
    loaded.update(0f);
    assertEquals(1000d, ArmorClassRules.effectiveArmorClass(loaded), .001);
    assertEquals(750, loaded.getEffectiveSkillLevel("archery"));
    assertEquals(200, loaded.getElementResistance("fire"));
    loaded.applyHeal(100, 100);
    assertEquals(1160, loaded.getCurrentHp(), "Expired curses restore normal healing");
  }

  private static void assertWeakening(Player player) {
    assertEquals(750d, ArmorClassRules.effectiveArmorClass(player), .001);
    assertEquals(400, player.getEffectiveSkillLevel("attack"));
    assertEquals(600, player.getEffectiveSkillLevel("archery"));
    assertEquals(160, player.getEffectiveSkillLevel("dodge"));
    assertEquals(160, player.getElementResistance("fire"));
    assertEquals(4000, player.getElementResistance("light"));
  }

  @Test
  void curseAlsoReducesRegenerationAndDispelRestoresHealing() {
    Player player = recipient();
    player.setEndurance(0);
    DivineAfflictions.apply(SpellRegistry.findById(DivineAfflictions.CURSE), player);
    player.applyBuff(
        "test regeneration",
        "",
        "",
        60,
        false,
        List.of(new SpellData.SpellEffect("REGEN", "hp", "100", "")));
    player.update(1f);
    assertEquals(1060, player.getCurrentHp());
    player.update(1f);
    // Natural regeneration is at most one HP here, rounded down while cursed.
    assertEquals(1120, player.getCurrentHp());
    assertTrue(player.dispelBuff(SpellRegistry.findById(DivineAfflictions.CURSE).getName()));
    player.update(1f);
    assertEquals(1220, player.getCurrentHp());
  }

  private static Player recipient() {
    Player player = new Player();
    player.setMaxHp(10000);
    player.setCurrentHp(1000);
    player.setSkills(new HashMap<>(Map.of("attack", 500, "archery", 750, "dodge", 200)));
    player.setQuestFlag("legacy:resist:fire", 100);
    armorBaseline(player);
    return player;
  }

  private static void armorBaseline(Player player) {
    player.applyBuff(
        "test armor",
        "",
        "",
        60,
        false,
        List.of(new SpellData.SpellEffect("ATTRIBUTE", "armorClass", "1000", "")));
  }

  private static class TestAvatar extends DivineAvatar {
    TestAvatar() throws Exception {
      super(DivinePantheon.definition("anubis"), 0, 0);
    }

    void onlySpell(int id) {
      attacks = getDefinition().getAttacks().stream().filter(a -> a.getSpellId() == id).toList();
      assertEquals(1, attacks.size());
    }

    void castAt(Vector2 position) {
      performAttack(position);
    }
  }
}
