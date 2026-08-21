package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBHUNTER2", x = 780, y = 439, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBHUNTER2", x = 792, y = 801, z = 0, stationary = false, aggressive = false)
public final class Hunter2 extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Hunter2(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBHUNTER2",
        "${monster.mobhunter2}",
        2086,
        0,
        37,
        39279,
        93,
        211,
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
        20,
        22,
        24,
        20,
        25,
        21,
        22,
        new int[] {58, 58, 58, 58, 39, 5000, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1108606976,
        10011,
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
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
