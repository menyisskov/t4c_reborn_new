package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Jailkeeper", x = 204, y = 2371, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Jailkeeper", x = 236, y = 2408, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Jailkeeper", x = 255, y = 2358, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Jailkeeper", x = 778, y = 517, z = 1, stationary = false, aggressive = true)
public final class Jailkeeper extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Jailkeeper";

  public Jailkeeper(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Jailkeeper",
        "${monster.jailkeeper}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "BlackWarrior#m",
        "BlackWarriorA#l",
        "BlackWarriorC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Rusted long sword", 0.05f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 79, 79, 53, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
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
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
