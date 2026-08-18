package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r146MOBSHRIEKERRODENT1 extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public r146MOBSHRIEKERRODENT1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBSHRIEKERRODENT1",
        "${monster.mobshriekerrodent1}",
        2667,
        0,
        42,
        57052,
        119,
        269,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        158,
        484,
        java.util.List.of(),
        false,
        0.0f,
        103,
        94,
        94,
        120,
        94,
        94,
        32,
        new int[] {42, 84, 42, 84, 62, 5000, 100, 100, 100, 100, 100, 100},
        88,
        362,
        0,
        1110441984,
        20003,
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
        java.util.List.of(new MonsterDef.Attack("1d151+118", 1066, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
