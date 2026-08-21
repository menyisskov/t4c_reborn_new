package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Arachnofiend", x = 1786, y = 199, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1797, y = 183, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1816, y = 228, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1821, y = 169, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1821, y = 209, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1838, y = 152, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1851, y = 135, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1868, y = 121, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1872, y = 83, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1882, y = 128, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1882, y = 93, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1898, y = 107, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1905, y = 85, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1915, y = 91, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1929, y = 82, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1944, y = 135, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1947, y = 154, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1949, y = 103, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1959, y = 117, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1959, y = 172, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1960, y = 153, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1972, y = 102, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1982, y = 149, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1984, y = 88, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1986, y = 104, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1986, y = 163, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1988, y = 215, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1994, y = 196, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 1996, y = 205, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2002, y = 113, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2004, y = 101, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2004, y = 128, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2004, y = 236, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2005, y = 175, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2005, y = 225, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2041, y = 160, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2046, y = 202, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2049, y = 190, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2054, y = 177, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2066, y = 175, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 2093, y = 201, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 583, y = 704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 583, y = 729, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 599, y = 676, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 602, y = 703, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 604, y = 753, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 607, y = 786, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 614, y = 685, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 623, y = 720, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 628, y = 777, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 630, y = 799, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 638, y = 719, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 640, y = 807, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 644, y = 760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 645, y = 832, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 648, y = 727, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 649, y = 787, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 652, y = 799, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 654, y = 815, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 657, y = 834, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 658, y = 774, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 660, y = 751, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 670, y = 819, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 673, y = 764, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 674, y = 788, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 682, y = 748, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 682, y = 823, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 686, y = 773, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 687, y = 808, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 691, y = 835, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 697, y = 769, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 698, y = 741, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 708, y = 816, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 709, y = 707, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 709, y = 730, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 714, y = 795, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 717, y = 781, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 719, y = 836, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 727, y = 689, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 735, y = 727, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 737, y = 711, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 737, y = 805, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 738, y = 838, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 747, y = 674, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 749, y = 800, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 750, y = 819, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 759, y = 776, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 763, y = 793, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 765, y = 608, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 768, y = 678, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 768, y = 695, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 768, y = 749, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 772, y = 809, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 772, y = 825, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 774, y = 649, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 775, y = 705, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 777, y = 832, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 780, y = 793, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 784, y = 731, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 785, y = 661, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 785, y = 834, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 790, y = 774, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 798, y = 628, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 799, y = 753, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 804, y = 656, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 804, y = 666, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 804, y = 760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 805, y = 806, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 810, y = 612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 811, y = 743, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 813, y = 593, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 813, y = 636, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 819, y = 783, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 820, y = 809, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 821, y = 766, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 826, y = 612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 826, y = 688, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 836, y = 817, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 837, y = 803, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 842, y = 741, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 848, y = 774, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 857, y = 762, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 857, y = 790, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 864, y = 724, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 869, y = 766, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 874, y = 743, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 886, y = 735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 889, y = 718, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 890, y = 789, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 900, y = 762, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 905, y = 707, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 908, y = 804, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 909, y = 715, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 912, y = 783, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 923, y = 700, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 923, y = 708, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 925, y = 741, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 929, y = 761, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 933, y = 722, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 936, y = 807, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 938, y = 773, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 943, y = 688, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 943, y = 752, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 943, y = 786, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 944, y = 736, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 948, y = 704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 949, y = 721, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 954, y = 776, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 956, y = 764, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 964, y = 706, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 973, y = 728, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 991, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Arachnofiend", x = 995, y = 730, z = 0, stationary = false, aggressive = true)
public final class Arachnofiend extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public static final String CANONICAL_NAME = "Arachnofiend";

  public Arachnofiend(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Arachnofiend",
        "${monster.arachnofiend}",
        1497,
        0,
        6,
        4869,
        70,
        159,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        107,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Tarantula fang", 0.03f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Tarantula eyes", 0.03f)),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {87, 43, 65, 65, 65, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        20033,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        31,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d90+69", 730, 95, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10344, 1, 10),
            new MonsterDef.Attack("", 0, 55, 10091, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10314, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
