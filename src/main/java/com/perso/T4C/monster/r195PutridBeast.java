package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Putrid Beast", x = 1016, y = 2633, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1039, y = 2605, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1049, y = 2648, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1053, y = 2624, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1063, y = 2652, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1096, y = 2761, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1102, y = 2646, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1102, y = 2724, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1112, y = 2767, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1114, y = 2751, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1121, y = 2620, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1121, y = 2655, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1122, y = 2646, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1134, y = 2611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1149, y = 2771, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1152, y = 2785, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1164, y = 2720, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1175, y = 2601, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1185, y = 2819, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1191, y = 2671, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1201, y = 2733, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1204, y = 2750, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1209, y = 2807, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1214, y = 2585, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1215, y = 2793, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1235, y = 2663, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1242, y = 2569, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1246, y = 2767, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1251, y = 2777, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1257, y = 2746, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1265, y = 2596, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1267, y = 2656, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1274, y = 2752, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1279, y = 2731, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1300, y = 2579, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1301, y = 2683, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1325, y = 2594, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Putrid Beast", x = 1391, y = 2661, z = 2, stationary = false, aggressive = true)
public final class r195PutridBeast extends DataMonster {
  public static final String SOUND_ATTACK = "Taunting Attack.wav";
  public static final String SOUND_DEATH = "Taunting Dying.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public r195PutridBeast(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Putrid Beast",
        "${monster.putrid_beast}",
        2435,
        0,
        8,
        10129,
        112,
        254,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC#m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        149,
        456,
        java.util.List.of(
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        98,
        89,
        89,
        114,
        0,
        89,
        0,
        new int[] {43, 87, 65, 65, 65, 5000, 100, 100, 100, 100, 100, 100},
        83,
        342,
        0,
        1078231040,
        20038,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        37,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d143+111", 1006, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10350, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10378, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10359, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10120, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
