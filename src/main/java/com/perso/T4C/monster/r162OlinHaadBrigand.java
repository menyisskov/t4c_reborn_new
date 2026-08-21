package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Olin Haad Brigand", x = 2603, y = 1200, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2604, y = 1202, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2605, y = 1198, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2606, y = 1202, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2607, y = 1200, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2613, y = 1209, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2621, y = 1215, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Brigand", x = 2633, y = 1219, z = 0, stationary = false, aggressive = true)
public final class r162OlinHaadBrigand extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r162OlinHaadBrigand(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Brigand",
        "${monster.olin_haad_brigand}",
        353,
        0,
        3,
        543,
        17,
        39,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        35,
        110,
        java.util.List.of(),
        false,
        0.0f,
        35,
        32,
        32,
        39,
        0,
        32,
        0,
        new int[] {84, 84, 84, 84, 56, 5000, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        10011,
        40023,
        40022,
        0,
        0,
        40021,
        40062,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d23+16", 250, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
