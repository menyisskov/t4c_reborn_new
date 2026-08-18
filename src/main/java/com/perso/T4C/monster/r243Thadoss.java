package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r243Thadoss extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r243Thadoss(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Thadoss",
        "${monster.thadoss}",
        1642,
        0,
        4,
        3960,
        40,
        91,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        140,
        428,
        java.util.List.of(new MonsterDef.LootDrop("Manastone", 0.04f)),
        false,
        0.0f,
        54,
        50,
        50,
        61,
        0,
        50,
        0,
        new int[] {75, 75, 100, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        39,
        166,
        0,
        1077084160,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d52+39", 478, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10119, 7, 12),
            new MonsterDef.Attack("", 0, 50, 10090, 7, 12),
            new MonsterDef.Attack("", 0, 100, 10120, 3, 6)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
