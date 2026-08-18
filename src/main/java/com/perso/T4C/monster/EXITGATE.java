package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class EXITGATE extends DataMonster {
  public static final String SOUND_ATTACK = null;
  public static final String SOUND_DEATH = null;
  public static final String SOUND_HIT = null;

  public EXITGATE(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "EXITGATE",
        "${monster.exitgate}",
        1000000,
        0,
        0,
        0,
        1,
        4,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        500,
        500,
        500,
        500,
        500,
        500,
        500,
        new int[] {5000, 5000, 5000, 5000, 5000, 5000, 100, 100, 100, 100, 100, 100},
        200,
        65535,
        0,
        1232348160,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d4", 50, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
