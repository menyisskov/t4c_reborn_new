package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "GHUNDARGRUMBLEFOOT",
    x = 1135,
    y = 1798,
    z = 0,
    stationary = false,
    aggressive = false)
public final class GHUNDARGRUMBLEFOOT extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public GHUNDARGRUMBLEFOOT(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "GHUNDARGRUMBLEFOOT",
        "${monster.ghundargrumblefoot}",
        3142,
        0,
        33,
        52248,
        73,
        166,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        222,
        682,
        java.util.List.of(),
        false,
        0.0f,
        77,
        70,
        70,
        89,
        70,
        70,
        27,
        new int[] {86, 43, 64, 64, 64, 5000, 100, 100, 100, 130, 100, 100},
        62,
        258,
        0,
        1106771968,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d94+72", 754, 25, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
