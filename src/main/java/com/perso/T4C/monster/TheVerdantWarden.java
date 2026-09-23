package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Boss lair at (1460,1600), worldZ 0 — an ancient treant guardian, the last loyal defender of the
// Wilds physically opposing the corruption; not allied with Caradoc or Ysolde. Reuses the TreeEnt
// animation/sound family (Forest Guardian precedent), scaled up to the strongest, tankiest of the
// three lair bosses. Guarded by three Moonlit Stalker adds — loyal Wilds beasts rallied to his
// last stand (see MoonlitStalker.java).
@Spawn(
    type = "The Verdant Warden",
    x = 1460,
    y = 1600,
    z = 0,
    stationary = false,
    aggressive = true)
public final class TheVerdantWarden extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "The Verdant Warden";

  public TheVerdantWarden(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "The Verdant Warden",
        "${monster.the_verdant_warden}",
        92000,
        0,
        11,
        118000000,
        860,
        1850,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1300,
        3250,
        java.util.List.of(
            new MonsterDef.LootDrop("verdant_wardens_bulwark", 0.015f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("healing_potion", 0.3f),
            // T4C-0021: earth-flavor source for the Ancient Celestial/Empyrean armor sets
            // (ArmorSetGenerator) - previously generated with zero acquisition path.
            new MonsterDef.LootDrop("ancient_celestial_earth_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_earth_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_earth_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_earth_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_earth_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_earth_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_earth_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_earth_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_earth_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_earth_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_earth_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_earth_protector", 0.012f)),
        false,
        0.0f,
        780,
        845,
        260,
        390,
        0,
        650,
        0,
        new int[] {120, 180, 150, 60, 130, 160, 100, 100, 100, 100, 100, 100},
        650,
        2000,
        0,
        650,
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
        java.util.List.of(new MonsterDef.Attack("1d975+845", 7800, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("The Verdant Warden", "Verdant Warden"),
        java.util.Map.of());
  }
}
