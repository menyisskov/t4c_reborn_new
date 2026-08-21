package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBDELWOBBLE", x = 1523, y = 63, z = 1, stationary = false, aggressive = true)
public final class Delwobble extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Delwobble(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBDELWOBBLE",
        "${monster.mobdelwobble}",
        1420,
        0,
        22,
        15750,
        34,
        78,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        124,
        384,
        java.util.List.of(),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        46,
        46,
        22,
        new int[] {77, 77, 77, 77, 103, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1099431936,
        10011,
        278,
        288,
        0,
        0,
        261,
        118,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d45+33", 430, 50, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
