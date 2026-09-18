package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ashfang Stalker", x = 1930, y = 1560, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashfang Stalker", x = 1970, y = 1610, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashfang Stalker", x = 1880, y = 1660, z = 0, stationary = false, aggressive = true)
public final class AshfangStalker extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Ashfang Stalker";

  public AshfangStalker(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ashfang Stalker",
        "${monster.ashfang_stalker}",
        1800,
        0,
        7,
        7300,
        65,
        115,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        110,
        345,
        java.util.List.of(new MonsterDef.LootDrop("cinderwrought_sash", 0.02f)),
        false,
        0.0f,
        74,
        74,
        74,
        90,
        0,
        74,
        0,
        new int[] {100, 85, 55, 190, 110, 95, 100, 100, 100, 100, 100, 100},
        63,
        260,
        0,
        68,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d109+84", 770, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("AshfangStalker"),
        java.util.Map.of());
  }
}
