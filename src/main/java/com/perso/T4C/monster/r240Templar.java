package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r240Templar extends DataMonster {
  public static final String SOUND_ATTACK = null;
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r240Templar(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Templar",
        "${monster.templar}",
        313,
        0,
        2,
        453,
        15,
        35,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        32,
        99,
        java.util.List.of(
            new MonsterDef.LootDrop("Templar ring", 0.01f),
            new MonsterDef.LootDrop("Polished short sword", 0.01f),
            new MonsterDef.LootDrop("Iron ring", 0.02f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {85, 85, 85, 85, 113, 5000, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        10009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        46,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
