package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class BloodHound extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Blood Hound";

  public BloodHound(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Blood Hound",
        "${monster.blood_hound}",
        1090,
        0,
        5,
        3148,
        56,
        126,
        30000L,
        "",
        null,
        null,
        "Wolf Attack.wav",
        "Wolf Dying.wav",
        "Wolf Hit.wav",
        86,
        264,
        java.util.List.of(
            new MonsterDef.LootDrop("Blackened iron key", 0.05f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f)),
        false,
        0.0f,
        63,
        58,
        58,
        72,
        0,
        58,
        0,
        new int[] {67, 67, 67, 67, 67, 5000, 100, 100, 100, 100, 100, 100},
        48,
        202,
        0,
        1077411840,
        21045,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        25,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d71+55", 586, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10352, 5, 11),
            new MonsterDef.Attack("", 0, 3, 10265, 5, 11),
            new MonsterDef.Attack("", 0, 25, 10119, 5, 11)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
