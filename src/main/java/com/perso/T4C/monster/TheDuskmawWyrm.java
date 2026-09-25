package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// One of the Elder Wyrms: proto-drakes that predate the named Drake line and each embody one
// *class* archetype instead of one element (see TheRootcrownWyrm.java, the wisdom-mage pilot, and
// DESIGN_GUIDELINES.md's "The Elder Wyrms"). The Duskmaw Wyrm is the intelligence-mage
// exemplar (dark power).
// Same tier as the Rootcrown Wyrm (level 700, 950-requirement legendary gear) and the same
// Agmorkian/Kraanian drake puppet+sound family; sealed in Drake's Lair away from Arch Drake, the
// four Kraanian Dragonguards and the other Elder Wyrms. Loot follows the boss-loot rules: its
// three own legendary pieces, the matching dark Ancient Celestial/Empyrean sets, and a
// potion pair so a kill is never a total whiff.
@Spawn(
    type = "The Duskmaw Wyrm",
    x = 2770,
    y = 2930,
    z = 0,
    stationary = false,
    aggressive = true)
public final class TheDuskmawWyrm extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "The Duskmaw Wyrm";

  public TheDuskmawWyrm(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Duskmaw Wyrm",
        "${monster.the_duskmaw_wyrm}",
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
            new MonsterDef.LootDrop("duskmaw_wyrms_umbral_rod", 0.01f),
            new MonsterDef.LootDrop("duskmaw_wyrms_nightshroud_mantle", 0.015f),
            new MonsterDef.LootDrop("duskmaw_wyrms_eclipsed_crown", 0.015f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            new MonsterDef.LootDrop("ancient_celestial_dark_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_dark_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_dark_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_dark_protector", 0.012f)),
        false,
        0.0f,
        600,
        750,
        250,
        950,
        0,
        300,
        0,
        new int[] {100, 100, 100, 100, 170, 60, 100, 100, 100, 100, 100, 100},
        700,
        2100,
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
        java.util.List.of(new MonsterDef.Attack("1d950+850", 1800, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("The Duskmaw Wyrm", "DuskmawWyrm"),
        java.util.Map.of());
  }
}
