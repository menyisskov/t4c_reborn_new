package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemBalance;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The five Elder Wyrms (DESIGN_GUIDELINES.md "The Elder Wyrms"): one boss per class archetype,
 * each placed in Drake's Lair and dropping its own legendary weapon plus two legendary armor
 * pieces of that class, alongside a multi-item boss loot table with a medium-rarity tier.
 */
class ElderWyrmsContentTest {
  private record Wyrm(
      String name, ItemBalance.Archetype archetype, String weapon, String armor1, String armor2) {}

  private static final List<Wyrm> WYRMS =
      List.of(
          new Wyrm(
              "The Rootcrown Wyrm",
              ItemBalance.Archetype.WIS_MAGE,
              "rootcrown_wyrms_verdant_sceptre",
              "rootcrown_wyrms_ageless_mantle",
              "rootcrown_wyrms_timeless_circlet"),
          new Wyrm(
              "The Pyreclaw Wyrm",
              ItemBalance.Archetype.WARRIOR,
              "pyreclaw_wyrms_searing_greatsword",
              "pyreclaw_wyrms_molten_warhelm",
              "pyreclaw_wyrms_forgeplate_gauntlets"),
          new Wyrm(
              "The Mistwing Wyrm",
              ItemBalance.Archetype.ARCHER,
              "mistwing_wyrms_farsight_longbow",
              "mistwing_wyrms_rainveil_mantle",
              "mistwing_wyrms_fogstride_boots"),
          new Wyrm(
              "The Duskmaw Wyrm",
              ItemBalance.Archetype.INT_MAGE,
              "duskmaw_wyrms_umbral_rod",
              "duskmaw_wyrms_nightshroud_mantle",
              "duskmaw_wyrms_eclipsed_crown"),
          new Wyrm(
              "The Galecrest Wyrm",
              ItemBalance.Archetype.HYBRID_MAGE,
              "galecrest_wyrms_tempest_wand",
              "galecrest_wyrms_windswept_mantle",
              "galecrest_wyrms_thunderhead_circlet"));

  /** Drake's Lair on the world map (compendium/data/zones.json: center 2850,2780, radius 180). */
  private static final int LAIR_X = 2850, LAIR_Y = 2780, LAIR_RADIUS = 180;

  @BeforeEach
  void load() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void reset() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void everyClassArchetypeHasExactlyOneElderWyrm() {
    Set<ItemBalance.Archetype> covered = EnumSet.noneOf(ItemBalance.Archetype.class);
    for (Wyrm wyrm : WYRMS) assertTrue(covered.add(wyrm.archetype()), wyrm.name() + " repeats a class");
    assertEquals(EnumSet.allOf(ItemBalance.Archetype.class), covered);
  }

  @Test
  void eachWyrmIsALevel700BossPlacedInDrakesLair() {
    for (Wyrm wyrm : WYRMS) {
      MonsterDef def = MonsterRegistry.findByName(wyrm.name());
      assertNotNull(def, wyrm.name() + " should be registered");
      assertEquals(700, def.getLevel(), wyrm.name() + " level");
      List<SpawnDefinition> spawns =
          SpawnRegistry.monsters().stream().filter(s -> wyrm.name().equals(s.type())).toList();
      assertEquals(1, spawns.size(), wyrm.name() + " should have exactly one spawn point");
      SpawnDefinition spawn = spawns.get(0);
      assertEquals(0, spawn.z(), wyrm.name() + " spawns on the world map");
      assertTrue(
          Math.hypot(spawn.x() - LAIR_X, spawn.y() - LAIR_Y) <= LAIR_RADIUS,
          wyrm.name() + " should spawn inside Drake's Lair");
    }
  }

  @Test
  void eachWyrmDropsItsOwnLegendaryWeaponAndArmorOfItsClass() {
    Set<String> seen = new HashSet<>();
    for (Wyrm wyrm : WYRMS) {
      Set<String> loot = lootKeys(MonsterRegistry.findByName(wyrm.name()));
      for (String key : List.of(wyrm.weapon(), wyrm.armor1(), wyrm.armor2())) {
        assertTrue(seen.add(key), key + " is dropped by more than one Elder Wyrm");
        assertTrue(loot.contains(key), wyrm.name() + " should drop " + key);
        ItemDefinition item = ItemRegistry.findByKey(key);
        assertNotNull(item, key + " should load");
        assertTrue(item.isUnique(), key + " should be legendary (unique)");
        assertEquals(wyrm.archetype(), ItemBalance.archetype(item), key + " class");
        assertEquals(
            key.equals(wyrm.weapon()),
            item.getBodyPart() == BodyPart.WEAPON,
            key + " weapon/armor slot");
      }
      ItemDefinition weapon = ItemRegistry.findByKey(wyrm.weapon());
      assertNotNull(weapon.getDmgFormula(), wyrm.weapon() + " needs a real damage formula");
      assertEquals(
          wyrm.archetype() == ItemBalance.Archetype.ARCHER,
          weapon.isBow(),
          wyrm.weapon() + " is a bow only for the archer wyrm");
    }
  }

  /** Boss-loot rules (T4C-0028/T4C-0034): several different items, every key real, and at least
   * one medium-rarity (0.2+) drop so a kill is never a total whiff. */
  @Test
  void newWyrmLootTablesFollowTheBossLootRules() {
    for (Wyrm wyrm : WYRMS) {
      MonsterDef def = MonsterRegistry.findByName(wyrm.name());
      for (MonsterDef.LootDrop drop : def.getLoot()) {
        assertNotNull(
            ItemRegistry.findByKey(drop.getItem()),
            wyrm.name() + " drops unknown item " + drop.getItem());
      }
      assertTrue(
          def.getLoot().stream().anyMatch(d -> d.getChance() >= 0.2f),
          wyrm.name() + " needs a medium-rarity drop");
      assertTrue(def.getGoldMax() > def.getGoldMin() && def.getGoldMin() > 0, wyrm.name() + " gold");
      if (wyrm.name().equals("The Rootcrown Wyrm")) continue; // the T4C-0029 pilot, unchanged
      assertTrue(lootKeys(def).size() >= 13, wyrm.name() + " should drop a full boss loot table");
    }
  }

  private static Set<String> lootKeys(MonsterDef def) {
    Set<String> keys = new HashSet<>();
    for (MonsterDef.LootDrop drop : def.getLoot()) keys.add(drop.getItem());
    return keys;
  }
}
