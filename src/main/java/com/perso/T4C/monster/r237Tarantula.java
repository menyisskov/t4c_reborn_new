package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Tarantula", x = 1000, y = 2333, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 1001, y = 2289, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 1016, y = 2297, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 637, y = 1585, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 644, y = 1597, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 650, y = 1578, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 677, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 720, y = 2030, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 728, y = 2056, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 730, y = 2005, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 738, y = 2070, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 739, y = 2049, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 741, y = 1995, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 742, y = 2042, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 744, y = 2050, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 746, y = 2058, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 749, y = 2053, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 750, y = 1867, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 753, y = 1848, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 754, y = 2069, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 757, y = 1855, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 758, y = 2012, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 759, y = 1875, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 762, y = 1835, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 766, y = 1882, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 767, y = 1868, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 769, y = 1843, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 770, y = 2073, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 772, y = 1888, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 773, y = 1831, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 774, y = 1997, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 775, y = 2007, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 778, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 779, y = 1839, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 783, y = 2072, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 784, y = 1869, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 784, y = 2001, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 786, y = 2056, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 787, y = 2030, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 789, y = 1846, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 789, y = 1856, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 789, y = 2013, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 789, y = 2068, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 794, y = 2044, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 811, y = 2615, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 814, y = 2085, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 837, y = 2627, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 845, y = 2555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 851, y = 2558, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 852, y = 2587, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 854, y = 2638, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 857, y = 2593, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 860, y = 2605, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 865, y = 2535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 870, y = 2597, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 878, y = 2619, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 880, y = 2517, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 882, y = 2583, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 884, y = 2535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 894, y = 2590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 895, y = 2602, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 904, y = 2239, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 904, y = 2559, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 905, y = 2610, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 909, y = 2595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 913, y = 1878, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 924, y = 2287, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 926, y = 2595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 928, y = 2581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 930, y = 2259, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 937, y = 1879, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 938, y = 2621, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 939, y = 1887, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 952, y = 2304, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 974, y = 2258, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 974, y = 2275, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 985, y = 2245, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 987, y = 2288, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tarantula", x = 997, y = 2262, z = 0, stationary = false, aggressive = true)
public final class r237Tarantula extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public r237Tarantula(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Tarantula",
        "${monster.tarantula}",
        606,
        0,
        4,
        1235,
        29,
        66,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        55,
        170,
        java.util.List.of(new MonsterDef.LootDrop("Tarantula eyes", 0.03f)),
        false,
        0.0f,
        46,
        42,
        42,
        52,
        0,
        42,
        0,
        new int[] {105, 52, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        31,
        134,
        0,
        1075052544,
        20033,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d38+28", 432, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Dark Tarantula"),
        java.util.Map.of());
  }
}
