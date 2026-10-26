package com.perso.T4C.item.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ItemJsonLoaderTest {

  @AfterEach
  void resetRegistry() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void loadsAncientCelestialFireArmorWithScaledStatsAndRequirements() {
    ItemJsonLoader.loadAndRegister("assets/items");

    ItemDefinition def = ItemRegistry.findByKey("ancient_celestial_fire_armor");
    assertNotNull(def, "Ancient Celestial Fire Armor should be registered from JSON");
    // Fire is an intelligence school (T4C-0027): intelligence-led requirements, and AC from the
    // body slot, the 400 endurance requirement and the intelligence-mage multiplier.
    assertEquals(99.2, def.getArmorClass(), 0.001);
    assertEquals(400L, def.getMinEnd());
    assertEquals(300L, def.getMinInt());
    assertEquals(75L, def.getMinWis());

    long firePowerBoost =
        def.getBoosts().stream()
            .filter(b -> b.getStatId() == 17)
            .mapToLong(b -> Long.parseLong(b.getExpression()))
            .sum();
    assertTrue(
        firePowerBoost > 0, "Body piece should carry a share of the themed fire power bonus");
  }

  @Test
  void loadsEmpyreanWarriorArmorWithStrengthRequirementAndAttackBonus() {
    ItemJsonLoader.loadAndRegister("assets/items");

    ItemDefinition def = ItemRegistry.findByKey("empyrean_warrior_armor");
    assertNotNull(def, "Empyrean Warrior Armor should be registered from JSON");
    // Warrior armor is the tank gear (T4C-0027): the most AC per point of endurance
    // requirement of any class, and no intelligence/wisdom gate at all.
    assertEquals(187.6, def.getArmorClass(), 0.001);
    assertEquals(550L, def.getMinEnd());
    assertEquals(500L, def.getReqStr());
    assertEquals(0L, def.getMinInt());
    assertEquals(0L, def.getMinWis());

    boolean hasStrBoost = def.getBoosts().stream().anyMatch(b -> b.getStatId() == 3);
    boolean hasAttackBoost = def.getBoosts().stream().anyMatch(b -> b.getStatId() == 8);
    boolean hasEnduranceBoost = def.getBoosts().stream().anyMatch(b -> b.getStatId() == 2);
    assertTrue(hasStrBoost, "Warrior armor should grant a strength boost");
    assertTrue(hasAttackBoost, "Warrior armor should grant an attack boost");
    assertTrue(hasEnduranceBoost, "Warrior armor should grant an endurance boost");
  }

  @Test
  void everyPlusThreeBowHasBothHigherVariantsAndNewUpgradesKeepTheirRequirements() {
    ItemJsonLoader.loadAndRegister("assets/items");
    for (ItemDefinition parent : ItemRegistry.load()) {
      if (!parent.isBow()
          || parent.getBodyPart() != com.perso.T4C.player.BodyPart.WEAPON
          || !parent.getKey().endsWith("_3")) continue;
      String root = parent.getKey().substring(0, parent.getKey().length() - 2);
      assertNotNull(ItemRegistry.findByKey(root + "_4"), root);
      assertNotNull(ItemRegistry.findByKey(root + "_5"), root);
    }
    var parent = ItemRegistry.findByKey("elm_recurve_bow_3");
    var plus4 = ItemRegistry.findByKey("elm_recurve_bow_4");
    var plus5 = ItemRegistry.findByKey("elm_recurve_bow_5");
    assertEquals(parent.getReqStr(), plus4.getReqStr());
    assertEquals(parent.getReqAgi(), plus5.getReqAgi());
    assertEquals(parent.getAtkDelay(), plus5.getAtkDelay());
    assertEquals(parent.getAppearanceEquippedPrimary(), plus4.getAppearanceEquippedPrimary());
    assertTrue(!parent.getDmgFormula().equals(plus4.getDmgFormula()));
    assertTrue(!plus4.getDmgFormula().equals(plus5.getDmgFormula()));
    var stock =
        ((com.perso.T4C.npc.behavior.ShopBehavior)
                com.perso.T4C.npc.catalog.ShopCatalog.get("QuartermasterElenna"))
            .items();
    assertTrue(stock.contains("elm_recurve_bow_4"));
    assertTrue(stock.contains("elm_recurve_bow_5"));
  }
}
