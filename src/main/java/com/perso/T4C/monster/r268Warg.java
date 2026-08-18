package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Warg", x = 562, y = 1081, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 572, y = 1087, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 685, y = 1114, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 806, y = 533, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 814, y = 868, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 818, y = 1097, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 819, y = 541, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Warg", x = 844, y = 857, z = 0, stationary = false, aggressive = true)
public final class r268Warg extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public r268Warg(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Warg",
        "${monster.warg}",
        1356,
        0,
        6,
        4212,
        64,
        146,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        100,
        308,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough agate", 0.02f)),
        false,
        0.0f,
        71,
        65,
        65,
        82,
        0,
        65,
        0,
        new int[] {67, 67, 67, 67, 45, 5000, 100, 100, 100, 100, 100, 100},
        56,
        234,
        0,
        1077673984,
        20045,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        33,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d83+63", 682, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10265, 12, 18),
            new MonsterDef.Attack("", 0, 3, 10352, 5, 11)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
