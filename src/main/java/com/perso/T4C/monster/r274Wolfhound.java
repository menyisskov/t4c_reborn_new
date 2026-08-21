package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Wolfhound", x = 123, y = 596, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 133, y = 546, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 217, y = 1024, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 317, y = 485, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 329, y = 487, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 347, y = 384, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 361, y = 383, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 483, y = 680, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 484, y = 577, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 497, y = 469, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 498, y = 460, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 539, y = 1082, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 546, y = 874, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 548, y = 1064, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 548, y = 859, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 552, y = 1098, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 562, y = 875, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 563, y = 1052, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 571, y = 1060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 580, y = 1064, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 584, y = 1097, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 585, y = 1077, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 683, y = 845, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 690, y = 1068, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 714, y = 1110, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 733, y = 998, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 760, y = 1085, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 782, y = 858, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 785, y = 882, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 788, y = 868, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 789, y = 902, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 793, y = 1125, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 799, y = 863, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 800, y = 887, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 809, y = 896, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 81, y = 559, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 819, y = 1117, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 825, y = 884, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 827, y = 1088, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 827, y = 851, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 827, y = 892, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 829, y = 899, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 832, y = 878, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 837, y = 879, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 844, y = 861, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 862, y = 895, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 864, y = 851, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 882, y = 874, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Wolfhound", x = 93, y = 526, z = 0, stationary = false, aggressive = true)
public final class r274Wolfhound extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public r274Wolfhound(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wolfhound",
        "${monster.wolfhound}",
        1090,
        0,
        5,
        3148,
        56,
        126,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        86,
        264,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        63,
        58,
        58,
        72,
        0,
        58,
        0,
        new int[] {71, 71, 71, 71, 47, 5000, 100, 100, 100, 100, 100, 100},
        48,
        202,
        0,
        1077411840,
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
        java.util.List.of(new MonsterDef.Attack("1d71+55", 586, 95, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
