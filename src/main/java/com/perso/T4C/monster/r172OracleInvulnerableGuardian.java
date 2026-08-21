package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2716,
    y = 2264,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2724,
    y = 2256,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2728,
    y = 2276,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2730,
    y = 2262,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2732,
    y = 2248,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2736,
    y = 2268,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2738,
    y = 2254,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2744,
    y = 2260,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2780,
    y = 2200,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2786,
    y = 2206,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2788,
    y = 2192,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2792,
    y = 2212,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2794,
    y = 2198,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2796,
    y = 2184,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2800,
    y = 2204,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Invulnerable Guardian",
    x = 2808,
    y = 2196,
    z = 2,
    stationary = false,
    aggressive = true)
public final class r172OracleInvulnerableGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public r172OracleInvulnerableGuardian(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Oracle Invulnerable Guardian",
        "${monster.oracle_invulnerable_guardian}",
        498800,
        0,
        0,
        0,
        125,
        348,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        145,
        131,
        131,
        171,
        0,
        131,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        130,
        780,
        0,
        1090021872,
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
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d224+124", 1820, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
