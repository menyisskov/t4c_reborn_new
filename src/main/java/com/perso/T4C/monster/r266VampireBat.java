package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r266VampireBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r266VampireBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Vampire Bat",
        "${monster.vampire_bat}",
        792,
        0,
        4,
        1874,
        38,
        87,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        "Bat Attack.wav",
        "Bat Dying.wav",
        "Bat Hit.wav",
        68,
        209,
        java.util.List.of(new MonsterDef.LootDrop("Vampire bat wings", 0.04f)),
        false,
        0.0f,
        53,
        49,
        49,
        60,
        0,
        49,
        0,
        new int[] {50, 101, 101, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        38,
        257,
        0,
        0,
        20002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d50+37", 466, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 75, 10119, 3, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
