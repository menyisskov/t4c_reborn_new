package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Blaarg Toemangler", x = 931, y = 1042, z = 2, stationary = false, aggressive = true)
public final class BlaargToemangler extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String CANONICAL_NAME = "Blaarg Toemangler";

  public BlaargToemangler(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Blaarg Toemangler",
        "${monster.blaarg_toemangler}",
        1584,
        0,
        4,
        3729,
        38,
        87,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        136,
        418,
        java.util.List.of(
            new MonsterDef.LootDrop("Rusted hand axe", 0.01f),
            new MonsterDef.LootDrop("Human foot", 0.05f),
            new MonsterDef.LootDrop("Orcish shield", 0.05f)),
        false,
        0.0f,
        53,
        49,
        49,
        60,
        0,
        49,
        0,
        new int[] {101, 50, 76, 76, 76, 5000, 100, 100, 100, 100, 100, 100},
        38,
        130,
        0,
        1077084160,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        15,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d50+37", 498, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10120, 3, 13),
            new MonsterDef.Attack("", 0, 40, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 15, 10122, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
