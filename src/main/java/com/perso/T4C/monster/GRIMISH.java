package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "GRIMISH", x = 1400, y = 1827, z = 0, stationary = false, aggressive = false)
public final class GRIMISH extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public GRIMISH(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "GRIMISH",
        "${monster.grimish}",
        996,
        0,
        27,
        13542,
        50,
        113,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        80,
        247,
        java.util.List.of(),
        false,
        0.0f,
        60,
        55,
        55,
        69,
        55,
        55,
        24,
        new int[] {96, 48, 72, 72, 72, 5025, 100, 100, 100, 100, 100, 100},
        45,
        190,
        0,
        1102053376,
        20041,
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
        java.util.List.of(new MonsterDef.Attack("1d64+49", 550, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
