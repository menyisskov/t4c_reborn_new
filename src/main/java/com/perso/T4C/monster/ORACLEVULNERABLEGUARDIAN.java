package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "ORACLEVULNERABLEGUARDIAN",
    x = 2722,
    y = 2270,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "ORACLEVULNERABLEGUARDIAN",
    x = 2802,
    y = 2190,
    z = 2,
    stationary = false,
    aggressive = true)
public final class ORACLEVULNERABLEGUARDIAN extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public ORACLEVULNERABLEGUARDIAN(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ORACLEVULNERABLEGUARDIAN",
        "${monster.oraclevulnerableguardian}",
        4988,
        0,
        51,
        128457,
        125,
        348,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        233,
        715,
        java.util.List.of(),
        false,
        0.0f,
        145,
        131,
        131,
        171,
        131,
        131,
        41,
        new int[] {31, 63, 31, 63, 47, 5000, 100, 100, 100, 100, 100, 100},
        130,
        780,
        0,
        1115815936,
        20036,
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
        java.util.List.of(new MonsterDef.Attack("1d224+124", 1820, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
