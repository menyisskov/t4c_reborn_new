package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Forest Guardian", x = 1263, y = 2811, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1289, y = 2903, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1313, y = 2882, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1335, y = 2856, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1356, y = 2886, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1357, y = 2718, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1357, y = 2859, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1378, y = 2860, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1399, y = 2663, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1400, y = 2693, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1428, y = 2900, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1432, y = 2693, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1446, y = 2695, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1462, y = 2818, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1470, y = 2734, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1484, y = 2753, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 1502, y = 2883, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 2722, y = 189, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 2727, y = 181, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 2736, y = 209, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 2745, y = 220, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 2800, y = 115, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Forest Guardian", x = 2839, y = 223, z = 0, stationary = false, aggressive = false)
public final class ForestGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "Forest Guardian";

  public ForestGuardian(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Forest Guardian",
        "${monster.forest_guardian}",
        3259,
        0,
        0,
        0,
        135,
        306,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(new MonsterDef.LootDrop("Light healing potion", 0.15f)),
        false,
        0.0f,
        85,
        74,
        74,
        285,
        0,
        74,
        0,
        new int[] {62, 62, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1078525952,
        20018,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        13,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10091, 6, 25),
            new MonsterDef.Attack("", 0, 50, 10120, 6, 25),
            new MonsterDef.Attack("", 0, 50, 10120, 1, 5)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
