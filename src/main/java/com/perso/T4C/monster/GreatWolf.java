package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Great Wolf", x = 354, y = 379, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 439, y = 935, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 554, y = 1076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 555, y = 860, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 557, y = 1091, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 570, y = 1092, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 574, y = 1068, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 577, y = 1076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 581, y = 1084, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 697, y = 1089, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 724, y = 1029, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 772, y = 1074, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 792, y = 541, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 797, y = 546, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 809, y = 519, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 809, y = 873, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 811, y = 548, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 818, y = 862, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 824, y = 538, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 826, y = 532, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 833, y = 855, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 87, y = 606, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Great Wolf", x = 95, y = 559, z = 0, stationary = false, aggressive = true)
public final class GreatWolf extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Great Wolf";

  public GreatWolf(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Great Wolf",
        "${monster.great_wolf}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        116,
        357,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Finely cut limestone", 5.0E-4f),
            new MonsterDef.LootDrop("Finely cut emerald", 0.002f),
            new MonsterDef.LootDrop("Finely cut malachite", 0.01f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {63, 63, 63, 63, 42, 5000, 100, 100, 100, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
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
            new MonsterDef.Attack("1d99+77", 790, 87, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10352, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10376, 0, 0),
            new MonsterDef.Attack("1d138+107", 970, 7, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
