package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "CENTAURAVENGER", x = 1064, y = 1265, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 1067, y = 1257, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 1087, y = 1111, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 1100, y = 1114, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 1189, y = 1077, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 1196, y = 1087, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 833, y = 985, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 882, y = 928, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 894, y = 1117, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 915, y = 822, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 929, y = 841, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 934, y = 1071, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 947, y = 828, z = 0, stationary = false, aggressive = true)
@Spawn(type = "CENTAURAVENGER", x = 956, y = 855, z = 0, stationary = false, aggressive = true)
public final class CentaurAvenger extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public CentaurAvenger(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURAVENGER",
        "${monster.centauravenger}",
        1497,
        0,
        32,
        24348,
        70,
        159,
        30000L,
        "64kCentaurKing#i",
        "64kCentaurKingA#i",
        "64kCentaurKingC#n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
        new int[] {130, 130, 130, 130, 86, 5001, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1106247680,
        20054,
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
        java.util.List.of(new MonsterDef.Attack("1d90+69", 730, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
