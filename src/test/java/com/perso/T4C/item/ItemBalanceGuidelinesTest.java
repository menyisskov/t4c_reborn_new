package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemBalance.Archetype;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.player.BodyPart;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * T4C-0027: every JSON-authored item follows the item rules in DESIGN_GUIDELINES.md (encoded in
 * {@link ItemBalance}). Armor-set pieces are checked for class, requirements and AC; hand-made
 * items are also checked against the single-item bonus budget.
 */
class ItemBalanceGuidelinesTest {
  private static final int INT = 1, STR = 3, WIS = 4, AGI = 6, ATK = 8, ARCHERY = 10035;
  private static final int LIGHT_RESIST = ItemBalance.LIGHT_RESIST_STAT_ID;
  private static final Set<Integer> RESISTS = ItemBalance.RESISTIBLE_ELEMENTS;
  private static final Map<Integer, Integer> POWER_TO_RESIST =
      Map.of(16, 12, 17, 13, 18, 14, 19, 15, 23, 21, 24, 22);
  private static final Set<Integer> INT_POWERS = Set.of(17, 18, 24); // fire, water, dark
  private static final Set<Integer> WIS_POWERS = Set.of(19, 23); // earth, light
  private static final int AIR_POWER = 16;

  private final List<ItemDefinition> items = new ArrayList<>();

  @BeforeEach
  void load() {
    ItemJsonLoader.loadAndRegister("assets/items");
    File[] files = new File("assets/items").listFiles((d, n) -> n.endsWith(".json"));
    assertNotNull(files);
    for (File f : files) {
      String key = f.getName().substring(0, f.getName().length() - ".json".length());
      ItemDefinition def = ItemRegistry.findByKey(key);
      assertNotNull(def, key + " should load");
      items.add(def);
    }
  }

  @AfterEach
  void reset() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void requirementsStayReachable() {
    for (ItemDefinition d : items) {
      assertTrue(
          d.getMinEnd() <= ItemBalance.MAX_ENDURANCE_REQUIREMENT,
          d.getKey() + " asks for " + d.getMinEnd() + " endurance");
      for (long req : new long[] {d.getReqStr(), d.getReqAgi(), d.getMinInt(), d.getMinWis()})
        assertTrue(
            req <= ItemBalance.MAX_SINGLE_REQUIREMENT, d.getKey() + " asks for " + req);
      assertNotNull(ItemBalance.archetype(d), d.getKey() + " has no class requirement");
    }
  }

  /** EquipmentBonusRules keys active boosts by boostId, so a shared id silently drops one
   * item's bonus. Generated armor-set pieces own 20000-29999; hand-made items use 30000+. */
  @Test
  void boostIdsAreUniqueAndInTheirReservedRange() {
    Map<Integer, String> owner = new HashMap<>();
    for (ItemDefinition d : items) {
      String bare = d.getKey().startsWith("item.") ? d.getKey().substring(5) : d.getKey();
      boolean generated = bare.startsWith("ancient_celestial_") || bare.startsWith("empyrean_");
      for (ItemDefinition.ItemBoost boost : d.getBoosts()) {
        String previous = owner.putIfAbsent(boost.getBoostId(), d.getKey());
        assertTrue(
            previous == null,
            "boostId " + boost.getBoostId() + " is used by both " + previous + " and " + d.getKey());
        int id = boost.getBoostId();
        assertTrue(
            generated ? id >= 20000 && id < 30000 : id >= 30000,
            d.getKey() + " boostId " + id + " is outside its reserved range");
      }
    }
  }

  @Test
  void armorClassFollowsTheEnduranceRequirement() {
    for (ItemDefinition d : items) {
      if (d.getBodyPart() == BodyPart.WEAPON) continue;
      double expected =
          ItemBalance.expectedArmorClass(d.getBodyPart(), d.getMinEnd(), ItemBalance.archetype(d));
      assertEquals(expected, d.getArmorClass(), 0.15, d.getKey() + " armor class");
    }
  }

  @Test
  void bonusesMatchTheItemsClass() {
    for (ItemDefinition d : items) {
      Archetype a = ItemBalance.archetype(d);
      Map<Integer, Double> b = boosts(d);
      boolean weapon = d.getBodyPart() == BodyPart.WEAPON;
      String k = d.getKey();
      if (a.isMage()) {
        for (int forbidden : new int[] {STR, AGI, ATK, ARCHERY})
          assertTrue(!b.containsKey(forbidden), k + " is mage gear with physical stat " + forbidden);
        Set<Integer> allowedPowers =
            a == Archetype.INT_MAGE
                ? INT_POWERS
                : a == Archetype.WIS_MAGE ? WIS_POWERS : Set.of(AIR_POWER);
        int powers = 0;
        for (int stat : b.keySet()) {
          if (!POWER_TO_RESIST.containsKey(stat)) continue;
          assertTrue(allowedPowers.contains(stat), k + " (" + a + ") has off-school power " + stat);
          powers++;
        }
        assertTrue(powers > 0, k + " is mage gear without elemental power");
        if (a != Archetype.WIS_MAGE) assertTrue(b.getOrDefault(INT, 0d) > 0, k + " needs intelligence");
        if (a != Archetype.INT_MAGE) assertTrue(b.getOrDefault(WIS, 0d) > 0, k + " needs wisdom");
        // Resistance is never confined to the item's own school (and never includes light,
        // checked separately) - every mage item resists all five non-light schools alike.
        if (!weapon)
          for (int r : RESISTS)
            assertTrue(b.getOrDefault(r, 0d) > 0, k + " needs resistance to all five non-light elements");
      } else {
        boolean warrior = a == Archetype.WARRIOR;
        for (int stat : b.keySet())
          assertTrue(
              !POWER_TO_RESIST.containsKey(stat) && stat != INT && stat != WIS,
              k + " is " + a + " gear with mage stat " + stat);
        assertTrue(b.containsKey(warrior ? STR : AGI), k + " needs " + (warrior ? "strength" : "agility"));
        assertTrue(b.containsKey(warrior ? ATK : ARCHERY), k + " needs " + (warrior ? "attack" : "archery"));
        if (!weapon)
          for (int r : RESISTS)
            assertTrue(b.getOrDefault(r, 0d) > 0, k + " needs resistance to all five non-light elements");
      }
    }
  }

  @Test
  void singleItemsFollowTheBonusBudget() {
    for (ItemDefinition d : items) {
      String k = d.getKey();
      String bare = k.startsWith("item.") ? k.substring("item.".length()) : k;
      if (bare.startsWith("ancient_celestial_") || bare.startsWith("empyrean_")) continue;
      Archetype a = ItemBalance.archetype(d);
      double p =
          ItemBalance.primaryRequirement(a, d.getReqStr(), d.getReqAgi(), d.getMinInt(), d.getMinWis());
      Map<Integer, Double> b = boosts(d);
      int main = ItemBalance.mainStatBonus(a, p);
      switch (a) {
        case WARRIOR -> assertEquals(main, b.get(STR), 1, k + " strength");
        case ARCHER -> assertEquals(main, b.get(AGI), 1, k + " agility");
        case INT_MAGE -> assertEquals(main, b.get(INT), 1, k + " intelligence");
        case WIS_MAGE -> assertEquals(main, b.get(WIS), 1, k + " wisdom");
        case HYBRID_MAGE -> {
          assertEquals(main, b.get(INT), 1, k + " intelligence");
          assertEquals(main, b.get(WIS), 1, k + " wisdom");
        }
      }
      if (d.getBodyPart() == BodyPart.WEAPON) continue;
      if (a.isMage()) {
        for (int stat : POWER_TO_RESIST.keySet()) {
          if (!b.containsKey(stat)) continue;
          assertEquals(ItemBalance.magicPowerBonus(p), b.get(stat), 1, k + " power");
        }
        // Resistance is the same across all five non-light schools, not tied to which power
        // the item happens to have (a light-power item still resists air/fire/water/earth/dark,
        // just never light itself).
        for (int r : RESISTS)
          assertEquals(ItemBalance.magicResistBonus(p), b.getOrDefault(r, 0d), 1, k + " resistance " + r);
      } else {
        int skill = a == Archetype.WARRIOR ? ATK : ARCHERY;
        assertEquals(ItemBalance.combatSkillBonus(p), b.get(skill), 1, k + " attack/archery");
        for (int r : RESISTS)
          assertTrue(
              b.get(r) >= ItemBalance.physicalResistBonus(p) - 1, k + " resistance " + r);
      }
    }
  }

  /** No item, of any class, ever grants light resistance - positive or negative. Scans the
   * *whole* registry (legacy Java catalog included via {@link ItemRegistry#load()}), not just
   * the JSON-authored {@link #items}, so a pre-existing legacy item can't quietly keep it. */
  @Test
  void neverGrantsLightResist() {
    for (ItemDefinition d : ItemRegistry.load()) {
      boolean hasLightResist =
          d.getBoosts().stream().anyMatch(boost -> boost.getStatId() == LIGHT_RESIST);
      assertTrue(!hasLightResist, d.getKey() + " grants light resist, which is never allowed");
    }
  }

  /** Numeric boosts summed by stat; formula boosts (legacy enchant lines) count as present. */
  private static Map<Integer, Double> boosts(ItemDefinition d) {
    Map<Integer, Double> out = new HashMap<>();
    for (ItemDefinition.ItemBoost boost : d.getBoosts()) {
      double v;
      try {
        v = Double.parseDouble(boost.getExpression());
      } catch (NumberFormatException e) {
        v = 1;
      }
      out.merge(boost.getStatId(), v, Double::sum);
    }
    return out;
  }
}
