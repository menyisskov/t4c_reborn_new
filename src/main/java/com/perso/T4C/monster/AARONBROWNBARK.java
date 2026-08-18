package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "AARONBROWNBARK", x = 812, y = 1568, z = 0, stationary = false, aggressive = false)
public final class AARONBROWNBARK extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public AARONBROWNBARK(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "AARONBROWNBARK",
        "${monster.aaronbrownbark}",
        3758,
        0,
        36,
        67760,
        85,
        194,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        250,
        770,
        java.util.List.of(),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        77,
        77,
        29,
        new int[] {81, 81, 81, 81, 81, 5000, 100, 200, 100, 200, 100, 100},
        70,
        290,
        0,
        1108082688,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d110+84", 850, 40, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
