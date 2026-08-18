package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r262UndeadBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r262UndeadBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Undead Bat",
        "${monster.undead_bat}",
        55,
        0,
        1,
        46,
        4,
        8,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        "Bat Attack.wav",
        "Bat Dying.wav",
        "Bat Hit.wav",
        5,
        16,
        java.util.List.of(new MonsterDef.LootDrop("Decaying Bat Wings", 0.05f)),
        false,
        0.0f,
        18,
        17,
        17,
        18,
        0,
        17,
        0,
        new int[] {61, 123, 123, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        3,
        27,
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
        90,
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+3", 46, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
