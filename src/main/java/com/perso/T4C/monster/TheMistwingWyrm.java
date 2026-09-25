package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// One of the Elder Wyrms: proto-drakes that predate the named Drake line and each embody one
// *class* archetype instead of one element (see TheRootcrownWyrm.java, the wisdom-mage pilot, and
// DESIGN_GUIDELINES.md's "The Elder Wyrms"). The Mistwing Wyrm is the archer exemplar
// (water-themed: its gear doubles water resistance), quicker and harder to hit but with less
// health.
// Same tier as the Rootcrown Wyrm (level 700, 950-requirement legendary gear) and the same
// Agmorkian/Kraanian drake puppet+sound family; sealed in Drake's Lair away from Arch Drake, the
// four Kraanian Dragonguards and the other Elder Wyrms. Loot follows the boss-loot rules: its
// three own legendary pieces, the matching archer Ancient Celestial/Empyrean sets, and a
// potion pair so a kill is never a total whiff.
@Spawn(
    type = "The Mistwing Wyrm",
    x = 3000,
    y = 2760,
    z = 0,
    stationary = false,
    aggressive = true)
public final class TheMistwingWyrm extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "The Mistwing Wyrm";

  public TheMistwingWyrm(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Mistwing Wyrm",
        "${monster.the_mistwing_wyrm}",
        90000,
        0,
        12,
        125000000,
        800,
        1700,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1300,
        3200,
        java.util.List.of(
            new MonsterDef.LootDrop("mistwing_wyrms_farsight_longbow", 0.01f),
            new MonsterDef.LootDrop("item.mistwing_wyrm_scale", 0.12f),
            new MonsterDef.LootDrop("mistwing_wyrms_rainveil_mantle", 0.015f),
            new MonsterDef.LootDrop("mistwing_wyrms_fogstride_boots", 0.015f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            new MonsterDef.LootDrop("ancient_celestial_archer_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_archer_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_archer_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_archer_protector", 0.012f)),
        false,
        0.0f,
        500,
        750,
        950,
        200,
        0,
        250,
        0,
        new int[] {120, 60, 170, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        700,
        2400,
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
        java.util.List.of(new MonsterDef.Attack("1d900+800", 1600, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("The Mistwing Wyrm", "MistwingWyrm"),
        java.util.Map.of());
  }
}
