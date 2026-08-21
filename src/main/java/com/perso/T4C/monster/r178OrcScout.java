package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r178OrcScout extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r178OrcScout(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Scout",
        "${monster.orc_scout}",
        148,
        0,
        2,
        157,
        8,
        18,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        16,
        49,
        java.util.List.of(
            new MonsterDef.LootDrop("Studded leather armor", 0.004f),
            new MonsterDef.LootDrop("Studded leather pants", 0.003f),
            new MonsterDef.LootDrop("Studded leather helmet", 0.01f)),
        false,
        0.0f,
        24,
        23,
        23,
        25,
        0,
        23,
        0,
        new int[] {119, 59, 89, 89, 89, 5000, 100, 100, 100, 100, 100, 100},
        9,
        46,
        0,
        1074790400,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d11+7", 118, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
