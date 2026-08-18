package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Defiler", x = 1019, y = 1852, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1020, y = 1844, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1028, y = 1841, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1035, y = 1848, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1036, y = 1973, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1038, y = 1981, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1044, y = 1967, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1096, y = 2045, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1099, y = 2038, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1103, y = 2040, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1169, y = 1900, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 1172, y = 1896, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 819, y = 1937, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 855, y = 1947, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 865, y = 1984, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 869, y = 1964, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 883, y = 1911, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 884, y = 1935, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 905, y = 1909, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 912, y = 1948, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 915, y = 1993, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 942, y = 1983, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 951, y = 2003, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Defiler", x = 994, y = 1998, z = 1, stationary = false, aggressive = true)
public final class Defiler extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Defiler";

  public Defiler(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Defiler",
        "${monster.defiler}",
        334,
        0,
        2,
        496,
        16,
        37,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        34,
        104,
        java.util.List.of(),
        false,
        0.0f,
        34,
        32,
        32,
        37,
        0,
        32,
        0,
        new int[] {85, 85, 56, 113, 85, 5000, 100, 100, 100, 100, 100, 100},
        19,
        86,
        0,
        1075970048,
        20024,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d22+15", 238, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 25, 10747, 1, 10),
            new MonsterDef.Attack("", 0, 75, 10119, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
