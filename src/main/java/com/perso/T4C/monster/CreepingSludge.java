package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class CreepingSludge extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Creeping Sludge";

  public CreepingSludge(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Creeping Sludge",
        "${monster.creeping_sludge}",
        1426,
        0,
        6,
        4540,
        67,
        153,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        104,
        319,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f)),
        false,
        0.0f,
        71,
        67,
        67,
        84,
        0,
        67,
        0,
        new int[] {53, 106, 53, 106, 79, 5000, 100, 100, 100, 100, 100, 100},
        58,
        242,
        0,
        1077739520,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d87+66", 706, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 20, 10348, 2, 10),
            new MonsterDef.Attack("", 0, 25, 10333, 2, 5),
            new MonsterDef.Attack("", 0, 5, 10350, 2, 10),
            new MonsterDef.Attack("", 0, 50, 10119, 2, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
