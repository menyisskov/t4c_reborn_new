package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r143MOBNEOFLARE1 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r143MOBNEOFLARE1(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBNEOFLARE1",
        "${monster.mobneoflare1}",
        3797,
        0,
        47,
        90380,
        148,
        337,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        197,
        605,
        java.util.List.of(),
        false,
        0.0f,
        125,
        113,
        113,
        147,
        113,
        113,
        37,
        new int[] {56, 56, 56, 56, 56, 5000, 100, 100, 100, 100, 100, 100},
        110,
        450,
        0,
        1113325568,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d190+147", 1330, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
