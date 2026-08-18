package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DungeonBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public static final String CANONICAL_NAME = "Dungeon Bat";

  public DungeonBat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dungeon Bat",
        "${monster.dungeon_bat}",
        41,
        0,
        1,
        34,
        3,
        7,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        "Bat Attack.wav",
        "Bat Dying.wav",
        "Bat Hit.wav",
        3,
        11,
        java.util.List.of(),
        false,
        0.0f,
        17,
        16,
        16,
        17,
        0,
        16,
        0,
        new int[] {61, 123, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        2,
        23,
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
        40,
        16,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+2", 34, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
