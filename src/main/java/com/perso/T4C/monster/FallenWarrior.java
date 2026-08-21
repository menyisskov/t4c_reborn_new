package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Fallen Warrior", x = 810, y = 1495, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 810, y = 1500, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 824, y = 1541, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 834, y = 1550, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 837, y = 1558, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 841, y = 1544, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 851, y = 1517, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 853, y = 1546, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 857, y = 1529, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 860, y = 1545, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 862, y = 1523, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Fallen Warrior", x = 871, y = 1525, z = 1, stationary = false, aggressive = true)
public final class FallenWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Fallen Warrior";

  public FallenWarrior(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Fallen Warrior",
        "${monster.fallen_warrior}",
        374,
        0,
        3,
        590,
        18,
        41,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        37,
        115,
        java.util.List.of(new MonsterDef.LootDrop("Corrupt heart", 0.1f)),
        false,
        0.0f,
        36,
        33,
        33,
        40,
        0,
        33,
        0,
        new int[] {84, 84, 111, 55, 5025, 55, 100, 100, 100, 100, 100, 100},
        21,
        94,
        0,
        1076101120,
        20009,
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
        java.util.List.of(new MonsterDef.Attack("1d24+17", 262, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
