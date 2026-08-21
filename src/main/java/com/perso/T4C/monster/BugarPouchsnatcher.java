package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Bugar Pouchsnatcher",
    x = 1232,
    y = 2256,
    z = 0,
    stationary = false,
    aggressive = true)
public final class BugarPouchsnatcher extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Bugar Pouchsnatcher";

  public BugarPouchsnatcher(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bugar Pouchsnatcher",
        "${monster.bugar_pouchsnatcher}",
        1066,
        0,
        3,
        2014,
        25,
        58,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        100,
        304,
        java.util.List.of(
            new MonsterDef.LootDrop("Red cape", 0.03f),
            new MonsterDef.LootDrop("Bracelet of power", 0.02f),
            new MonsterDef.LootDrop("Fine steel scimitar", 0.001f)),
        false,
        0.0f,
        43,
        40,
        40,
        48,
        0,
        40,
        0,
        new int[] {80, 80, 80, 80, 53, 5000, 100, 100, 100, 100, 100, 100},
        28,
        122,
        0,
        1076625408,
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
            new MonsterDef.Attack("1d34+24", 346, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 20, 10122, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
