package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "VENADAR", x = 2345, y = 545, z = 2, stationary = false, aggressive = false)
public final class VENADAR extends DataMonster {
  public static final String SOUND_ATTACK = "Taunting Attack.wav";
  public static final String SOUND_DEATH = "Taunting Dying.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public VENADAR(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "VENADAR",
        "${monster.venadar}",
        1879,
        0,
        0,
        0,
        85,
        194,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        77,
        77,
        29,
        new int[] {40, 81, 40, 81, 61, 5000, 100, 100, 100, 100, 100, 100},
        100,
        290,
        0,
        1108082688,
        20038,
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
        java.util.List.of(new MonsterDef.Attack("1d110+84", 850, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
