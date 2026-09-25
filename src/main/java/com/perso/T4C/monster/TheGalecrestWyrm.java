package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// One of the Elder Wyrms: proto-drakes that predate the named Drake line and each embody one
// *class* archetype instead of one element (see TheRootcrownWyrm.java, the wisdom-mage pilot, and
// DESIGN_GUIDELINES.md's "The Elder Wyrms"). The Galecrest Wyrm is the hybrid (air) mage
// exemplar.
// Same tier as the Rootcrown Wyrm (level 700, 950-requirement legendary gear) and the same
// Agmorkian/Kraanian drake puppet+sound family; sealed in Drake's Lair away from Arch Drake, the
// four Kraanian Dragonguards and the other Elder Wyrms. Loot follows the boss-loot rules: its
// three own legendary pieces, the matching air Ancient Celestial/Empyrean sets, and a
// potion pair so a kill is never a total whiff.
@Spawn(
    type = "The Galecrest Wyrm",
    x = 2740,
    y = 2690,
    z = 0,
    stationary = false,
    aggressive = true)
public final class TheGalecrestWyrm extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "The Galecrest Wyrm";

  public TheGalecrestWyrm(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Galecrest Wyrm",
        "${monster.the_galecrest_wyrm}",
        92000,
        0,
        12,
        125000000,
        850,
        1800,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1300,
        3300,
        java.util.List.of(
            new MonsterDef.LootDrop("galecrest_wyrms_tempest_wand", 0.01f),
            new MonsterDef.LootDrop("item.galecrest_wyrm_scale", 0.12f),
            new MonsterDef.LootDrop("galecrest_wyrms_windswept_mantle", 0.015f),
            new MonsterDef.LootDrop("galecrest_wyrms_thunderhead_circlet", 0.015f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            new MonsterDef.LootDrop("ancient_celestial_air_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_air_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_air_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_air_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_air_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_air_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_air_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_air_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_air_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_air_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_air_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_air_protector", 0.012f)),
        false,
        0.0f,
        600,
        750,
        300,
        700,
        0,
        700,
        0,
        new int[] {170, 60, 100, 100, 100, 110, 100, 100, 100, 100, 100, 100},
        700,
        2200,
        0,
        700,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d950+850", 1700, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("The Galecrest Wyrm", "GalecrestWyrm"),
        java.util.Map.of());
  }
}
