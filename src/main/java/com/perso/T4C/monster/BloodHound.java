package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Blood Hound", x = 1000, y = 1083, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1000, y = 1091, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1003, y = 1060, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1019, y = 1345, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1021, y = 1142, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1024, y = 1077, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1031, y = 1060, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1045, y = 1145, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1047, y = 1272, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1050, y = 1131, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1055, y = 1279, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1056, y = 1340, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1057, y = 1072, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1057, y = 1314, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1059, y = 1103, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 1059, y = 1270, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 583, y = 1123, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 587, y = 1144, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 603, y = 1103, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 620, y = 1122, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 632, y = 1076, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 642, y = 1103, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 737, y = 1278, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 747, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 747, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 747, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 747, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 747, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 748, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 749, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 952, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 750, y = 962, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 952, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 751, y = 962, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 952, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 752, y = 962, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 952, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 753, y = 962, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 1200, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 952, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 754, y = 962, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 953, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 755, y = 961, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 1117, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 954, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 756, y = 960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 1069, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 1292, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 1346, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 955, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 957, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 757, y = 959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 765, y = 1286, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 767, y = 1135, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 767, y = 1272, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 769, y = 1146, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 770, y = 1216, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 771, y = 1040, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 772, y = 1104, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 772, y = 1245, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 774, y = 1117, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 775, y = 1349, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 778, y = 1017, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 788, y = 1290, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 791, y = 1125, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 795, y = 1333, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 801, y = 1145, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 807, y = 1346, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 833, y = 1338, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 840, y = 1350, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 923, y = 1209, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 932, y = 1215, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 973, y = 1052, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 974, y = 1000, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 975, y = 1346, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 979, y = 1209, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 981, y = 1333, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 995, y = 1066, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 997, y = 1234, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Blood Hound", x = 999, y = 1027, z = 1, stationary = false, aggressive = true)
public final class BloodHound extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Blood Hound";

  public BloodHound(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Blood Hound",
        "${monster.blood_hound}",
        1090,
        0,
        5,
        3148,
        56,
        126,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        86,
        264,
        java.util.List.of(
            new MonsterDef.LootDrop("Blackened iron key", 0.05f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f)),
        false,
        0.0f,
        63,
        58,
        58,
        72,
        0,
        58,
        0,
        new int[] {67, 67, 67, 67, 67, 5000, 100, 100, 100, 100, 100, 100},
        48,
        202,
        0,
        1077411840,
        21045,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        25,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d71+55", 586, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10352, 5, 11),
            new MonsterDef.Attack("", 0, 3, 10265, 5, 11),
            new MonsterDef.Attack("", 0, 25, 10119, 5, 11)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
