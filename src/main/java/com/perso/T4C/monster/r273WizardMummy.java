package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Wizard Mummy", x = 1211, y = 2371, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 1232, y = 2337, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 1245, y = 2273, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 1274, y = 2211, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 1275, y = 2172, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 1301, y = 2236, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 1312, y = 2292, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 394, y = 208, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 400, y = 197, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 428, y = 183, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 446, y = 250, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 447, y = 166, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 448, y = 226, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 456, y = 180, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 464, y = 266, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 475, y = 283, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 485, y = 191, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 493, y = 314, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 518, y = 299, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 704, y = 2584, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 704, y = 2615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 712, y = 2596, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 797, y = 2617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 867, y = 2570, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 874, y = 2543, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Wizard Mummy", x = 892, y = 2566, z = 2, stationary = false, aggressive = true)
public final class r273WizardMummy extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Mummy Dying.wav";
  public static final String SOUND_HIT = "Mummy Hit.wav";

  public r273WizardMummy(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wizard Mummy",
        "${monster.wizard_mummy}",
        217,
        0,
        2,
        267,
        11,
        25,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        23,
        71,
        java.util.List.of(new MonsterDef.LootDrop("Torch", 0.05f)),
        false,
        0.0f,
        28,
        26,
        26,
        30,
        0,
        26,
        0,
        new int[] {87, 87, 116, 58, 5025, 58, 100, 100, 100, 100, 100, 100},
        13,
        62,
        0,
        1075314688,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        21,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d15+10", 166, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10120, 5, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
