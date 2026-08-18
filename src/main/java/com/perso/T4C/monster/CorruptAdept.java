package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Corrupt Adept", x = 825, y = 1559, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 841, y = 1529, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 842, y = 1744, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 846, y = 1534, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 862, y = 1586, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 866, y = 1586, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 867, y = 1529, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 874, y = 1610, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 874, y = 1733, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 876, y = 1761, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 879, y = 1787, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 880, y = 1765, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 881, y = 1621, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 891, y = 1641, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 897, y = 1723, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 903, y = 1762, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 907, y = 1625, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 909, y = 1735, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 922, y = 1621, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 923, y = 1562, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 930, y = 1599, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 936, y = 1656, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 948, y = 1571, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 949, y = 1637, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 949, y = 1669, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Adept", x = 967, y = 1647, z = 1, stationary = false, aggressive = true)
public final class CorruptAdept extends DataMonster {
  public static final String SOUND_ATTACK = "Atrocity Attack.wav";
  public static final String SOUND_DEATH = "Atrocity Dying.wav";
  public static final String SOUND_HIT = "Atrocity Hit.wav";

  public static final String CANONICAL_NAME = "Corrupt Adept";

  public CorruptAdept(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Corrupt Adept",
        "${monster.corrupt_adept}",
        418,
        0,
        3,
        699,
        20,
        46,
        30000L,
        "Kraanian#h",
        "KraanianA#h",
        "KraanianC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        41,
        126,
        java.util.List.of(),
        false,
        0.0f,
        38,
        35,
        35,
        42,
        0,
        35,
        0,
        new int[] {110, 55, 83, 83, 83, 5000, 100, 100, 100, 100, 100, 100},
        23,
        102,
        0,
        1076232192,
        20025,
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
        java.util.List.of(new MonsterDef.Attack("1d27+19", 286, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
