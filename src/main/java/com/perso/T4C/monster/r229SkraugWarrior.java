package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r229SkraugWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public r229SkraugWarrior(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skraug Warrior",
        "${monster.skraug_warrior}",
        1000,
        0,
        0,
        0,
        1,
        100,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        100,
        100,
        100,
        100,
        0,
        100,
        0,
        new int[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        100,
        100,
        0,
        1079574528,
        20050,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        42,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d100", 100, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
