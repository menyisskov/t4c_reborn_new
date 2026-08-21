package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "CENTAURCHAMPION", x = 1020, y = 989, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 1064, y = 1090, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 1120, y = 1172, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 1136, y = 1269, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 851, y = 986, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 925, y = 1133, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 975, y = 1192, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURCHAMPION", x = 992, y = 1140, z = 0, stationary = false, aggressive = false)
public final class CentaurChampion extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public CentaurChampion(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURCHAMPION",
        "${monster.centaurchampion}",
        2086,
        0,
        37,
        39279,
        93,
        211,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        134,
        412,
        java.util.List.of(),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        82,
        82,
        30,
        new int[] {116, 116, 116, 116, 78, 5001, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1108606976,
        20051,
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
