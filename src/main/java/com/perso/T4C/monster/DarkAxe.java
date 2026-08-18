package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Dark Axe", x = 1359, y = 273, z = 1, stationary = false, aggressive = true)
public final class DarkAxe extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Dark Axe";

  public DarkAxe(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dark Axe",
        "${monster.dark_axe}",
        2180,
        0,
        5,
        6297,
        56,
        126,
        30000L,
        "BlackWarrior#m",
        "BlackWarriorA#l",
        "BlackWarriorC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        172,
        528,
        java.util.List.of(
            new MonsterDef.LootDrop("Healing potion", 0.05f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Red cape", 0.05f),
            new MonsterDef.LootDrop("Ringmail boots", 0.005f),
            new MonsterDef.LootDrop("Ringmail gauntlets", 0.005f),
            new MonsterDef.LootDrop("Ringmail helmet", 0.005f),
            new MonsterDef.LootDrop("Ringmail leggings", 0.002f),
            new MonsterDef.LootDrop("Ringmail armor", 0.001f)),
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
        20043,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d71+55", 586, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
