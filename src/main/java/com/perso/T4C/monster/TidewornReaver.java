package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Coastal raiders guarding the last stretch of mainland shore before the crossing to Avalon
// (center ~1550,1300, worldZ 0) — see quest/definition/PassageToAvalon.java. Reuses the
// legacy "Thief" human-raider animation/sound family (same one r197Raider uses) at fork-tier
// stats, since no dedicated smuggler sprite exists yet.
@Spawn(type = "Tideworn Reaver", x = 1500, y = 1260, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1540, y = 1240, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1580, y = 1255, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1610, y = 1280, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1590, y = 1320, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1550, y = 1340, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1510, y = 1330, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1480, y = 1300, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1620, y = 1330, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 1470, y = 1270, z = 0, stationary = false, aggressive = true)
public final class TidewornReaver extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Tideworn Reaver";

  public TidewornReaver(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Tideworn Reaver",
        "${monster.tideworn_reaver}",
        29000,
        0,
        5,
        240000,
        150,
        330,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        270,
        680,
        java.util.List.of(
            new MonsterDef.LootDrop("healing_potion", 0.1f),
            new MonsterDef.LootDrop("mana_potion", 0.06f)),
        false,
        0.0f,
        250,
        230,
        230,
        190,
        0,
        250,
        0,
        new int[] {110, 120, 95, 80, 70, 130, 100, 100, 100, 100, 100, 100},
        280,
        1080,
        0,
        165,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d410+355", 3300, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("TidewornReaver"),
        java.util.Map.of());
  }
}
