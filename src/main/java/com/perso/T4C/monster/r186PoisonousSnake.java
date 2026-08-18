package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r186PoisonousSnake extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public r186PoisonousSnake(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Poisonous Snake",
        "${monster.poisonous_snake}",
        164,
        0,
        2,
        184,
        9,
        20,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC#m",
        "Snake Attack.wav",
        "Snake Dying.wav",
        "Snake Hit.wav",
        17,
        55,
        java.util.List.of(),
        false,
        0.0f,
        25,
        24,
        24,
        25,
        0,
        23,
        0,
        new int[] {89, 89, 59, 118, 89, 5000, 100, 100, 100, 100, 100, 100},
        10,
        50,
        0,
        1075052544,
        20019,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        30,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d12+8", 130, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
