package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r147MOBYOGGOTHWORM1 extends DataMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public r147MOBYOGGOTHWORM1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBYOGGOTHWORM1",
        "${monster.mobyoggothworm1}",
        1497,
        0,
        32,
        24348,
        70,
        159,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC#k",
        "Worm Attack.wav",
        "Worm Dying.wav",
        "Worm Hit.wav",
        107,
        330,
        java.util.List.of(),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        68,
        68,
        27,
        new int[] {43, 87, 43, 87, 65, 5000, 100, 100, 100, 100, 100, 100},
        150,
        250,
        0,
        1106247680,
        20016,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d90+69", 730, 3, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
