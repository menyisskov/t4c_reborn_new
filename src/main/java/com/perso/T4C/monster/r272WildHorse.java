package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r272WildHorse extends DataMonster {
  public static final String SOUND_ATTACK = "Pegase Attack.wav";
  public static final String SOUND_DEATH = "Pegase Dying.wav";
  public static final String SOUND_HIT = "Pegase Hit.wav";

  public r272WildHorse(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wild Horse",
        "${monster.wild_horse}",
        353,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "Horse#h",
        "HorseA",
        "HorseC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        new int[] {56, 56, 56, 56, 56, 5000, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        20022,
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
        false,
        java.util.List.of(),
        true,
        30,
        java.util.List.of("Horse"),
        java.util.Map.of());
  }
}
