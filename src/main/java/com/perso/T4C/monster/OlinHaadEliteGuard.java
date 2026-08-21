package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "OLINHAADELITEGUARD",
    x = 1807,
    y = 2540,
    z = 1,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "OLINHAADELITEGUARD",
    x = 1815,
    y = 2532,
    z = 1,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "OLINHAADELITEGUARD",
    x = 1835,
    y = 2568,
    z = 1,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "OLINHAADELITEGUARD",
    x = 1843,
    y = 2560,
    z = 1,
    stationary = false,
    aggressive = true)
public final class OlinHaadEliteGuard extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public OlinHaadEliteGuard(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "OLINHAADELITEGUARD",
        "${monster.olinhaadeliteguard}",
        500000,
        0,
        0,
        0,
        1,
        5,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        200,
        220,
        240,
        200,
        250,
        210,
        220,
        new int[] {5000, 5000, 5000, 5000, 5000, 5000, 100, 100, 100, 100, 100, 100},
        10,
        50,
        0,
        0,
        10011,
        269,
        265,
        259,
        270,
        268,
        7,
        272,
        287,
        100,
        0,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack(
                "if(target.hp>5?if(target.hp<20?target.hp-5:20-1d5):0)", 10000, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
