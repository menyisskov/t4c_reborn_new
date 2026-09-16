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
    assertEquals(114.5, def.getArmorClass(), 0.001);
    assertEquals(400L, def.getMinEnd());
    assertEquals(150L, def.getMinInt());
    assertEquals(150L, def.getMinWis());

    long firePowerBoost =
        def.getBoosts().stream()
            .filter(b -> b.getStatId() == 17)
            .mapToLong(b -> Long.parseLong(b.getExpression()))
            .sum();
    assertTrue(firePowerBoost > 0, "Body piece should carry a share of the themed fire power bonus");
  }

  @Test
  void loadsEmpyreanWarriorArmorWithStrengthRequirementAndAttackBonus() {
    ItemJsonLoader.loadAndRegister("assets/items");

    ItemDefinition def = ItemRegistry.findByKey("empyrean_warrior_armor");
    assertNotNull(def, "Empyrean Warrior Armor should be registered from JSON");
    assertEquals(171.75, def.getArmorClass(), 0.001);
    assertEquals(550L, def.getMinEnd());
    assertEquals(500L, def.getReqStr());

    boolean hasStrBoost = def.getBoosts().stream().anyMatch(b -> b.getStatId() == 3);
    boolean hasAttackBoost = def.getBoosts().stream().anyMatch(b -> b.getStatId() == 8);
    assertTrue(hasStrBoost, "Warrior armor should grant a strength boost");
    assertTrue(hasAttackBoost, "Warrior armor should grant an attack boost");
  }
}
