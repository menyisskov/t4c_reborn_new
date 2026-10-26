package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "MOBQUENTINTARANTULA",
    x = 1731,
    y = 247,
    z = 2,
    stationary = false,
    aggressive = true)
public final class QuentinTarantula extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public QuentinTarantula(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBQUENTINTARANTULA",
        "${monster.mobquentintarantula}",
        3006,
        0,
        44,
        66721,
        131,
        290,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        170,
        522,
        java.util.List.of(),
        false,
        0.0f,
        110,
        100,
        100,
        129,
        100,
        100,
        34,
        new int[] {78, 39, 58, 58, 58, 5000, 100, 100, 100, 100, 100, 100},
        95,
        390,
        0,
        1111228416,
        20033,
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
        java.util.List.of(new MonsterDef.Attack("1d164+127", 1150, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
