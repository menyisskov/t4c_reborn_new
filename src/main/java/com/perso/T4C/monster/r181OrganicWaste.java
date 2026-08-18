package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r181OrganicWaste extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r181OrganicWaste(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Organic Waste",
        "${monster.organic_waste}",
        132,
        0,
        2,
        135,
        7,
        17,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        14,
        44,
        java.util.List.of(),
        false,
        0.0f,
        23,
        22,
        22,
        24,
        0,
        22,
        0,
        new int[] {60, 120, 120, 60, 90, 5000, 100, 100, 100, 100, 100, 100},
        8,
        22,
        0,
        1075838976,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        40,
        10,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d11+6", 106, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
