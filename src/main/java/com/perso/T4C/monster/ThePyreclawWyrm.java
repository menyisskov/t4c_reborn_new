package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// One of the Elder Wyrms: proto-drakes that predate the named Drake line and each embody one
// *class* archetype instead of one element (see TheRootcrownWyrm.java, the wisdom-mage pilot, and
// DESIGN_GUIDELINES.md's "The Elder Wyrms"). The Pyreclaw Wyrm is the warrior exemplar
// (fire-themed: its gear doubles fire resistance), the heaviest-hitting and toughest of the five.
// Same tier as the Rootcrown Wyrm (level 700, 950-requirement legendary gear) and the same
// Agmorkian/Kraanian drake puppet+sound family; sealed in Drake's Lair away from Arch Drake, the
// four Kraanian Dragonguards and the other Elder Wyrms. Loot follows the boss-loot rules: its
// three own legendary pieces, the matching warrior Ancient Celestial/Empyrean sets, and a
// potion pair so a kill is never a total whiff.
@Spawn(
    type = "The Pyreclaw Wyrm",
    x = 2950,
    y = 2890,
    z = 0,
    stationary = false,
    aggressive = true)
public final class ThePyreclawWyrm extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "The Pyreclaw Wyrm";

  public ThePyreclawWyrm(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Pyreclaw Wyrm",
        "${monster.the_pyreclaw_wyrm}",
        105000,
        0,
        12,
        125000000,
        1000,
        2100,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1500,
        3750,
        java.util.List.of(
            new MonsterDef.LootDrop("pyreclaw_wyrms_searing_greatsword", 0.01f),
            new MonsterDef.LootDrop("item.pyreclaw_wyrm_scale", 0.12f),
            new MonsterDef.LootDrop("pyreclaw_wyrms_molten_warhelm", 0.015f),
            new MonsterDef.LootDrop("pyreclaw_wyrms_forgeplate_gauntlets", 0.015f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_warrior_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_protector", 0.012f)),
        false,
        0.0f,
        950,
        900,
        300,
        150,
        0,
        200,
        0,
        new int[] {100, 100, 60, 170, 110, 100, 100, 100, 100, 100, 100, 100},
        700,
        2000,
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
        java.util.List.of(new MonsterDef.Attack("1d1100+1000", 2000, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("The Pyreclaw Wyrm", "PyreclawWyrm"),
        java.util.Map.of());
  }
}
