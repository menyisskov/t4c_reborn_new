package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Sandlord", x = 266, y = 2400, z = 0, stationary = false, aggressive = true)
public final class r208Sandlord extends DataMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public r208Sandlord(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Sandlord",
        "${monster.sandlord}",
        1694,
        0,
        4,
        4181,
        42,
        94,
        30000L,
        "BigWorm#h",
        "BigWormA#k",
        "BigWormC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        142,
        440,
        java.util.List.of(),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {75, 75, 50, 100, 75, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
        20017,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
