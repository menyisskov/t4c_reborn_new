package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Liedric Throatcutter",
    x = 495,
    y = 1624,
    z = 0,
    stationary = false,
    aggressive = true)
public final class LiedricThroatcutter extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Liedric Throatcutter";

  public LiedricThroatcutter(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Liedric Throatcutter",
        "${monster.liedric_throatcutter}",
        1534,
        0,
        4,
        3518,
        35,
        84,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        132,
        406,
        java.util.List.of(
            new MonsterDef.LootDrop("Golden chalice", 0.1f),
            new MonsterDef.LootDrop("Critical healing potion", 0.05f),
            new MonsterDef.LootDrop("Pouch of Blue Cohosh", 0.03f),
            new MonsterDef.LootDrop("Polished hand axe", 0.02f)),
        false,
        0.0f,
        52,
        48,
        48,
        59,
        0,
        48,
        0,
        new int[] {77, 77, 77, 77, 103, 5000, 100, 100, 100, 100, 100, 100},
        37,
        200,
        0,
        1076363264,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d50+34", 450, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10120, 3, 13),
            new MonsterDef.Attack("", 0, 40, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 15, 10122, 3, 13),
            new MonsterDef.Attack("", 0, 40, 10094, 14, 25)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
