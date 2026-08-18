package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "BALORK", x = 235, y = 452, z = 1, stationary = false, aggressive = false)
public final class BALORK extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public BALORK(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "BALORK",
        "${monster.balork}",
        508,
        0,
        13,
        3372,
        13,
        29,
        30000L,
        "Demon#i",
        "DemonA#i",
        "DemonC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        52,
        164,
        java.util.List.of(),
        false,
        0.0f,
        51,
        53,
        35,
        36,
        56,
        38,
        50,
        new int[] {115, 115, 115, 115, 115, 5000, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1088421888,
        20013,
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
        java.util.List.of(new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
