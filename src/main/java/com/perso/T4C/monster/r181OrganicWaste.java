package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Organic Waste", x = 173, y = 509, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Organic Waste", x = 195, y = 527, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Organic Waste", x = 202, y = 696, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Organic Waste", x = 215, y = 482, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Organic Waste", x = 419, y = 451, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Organic Waste", x = 419, y = 462, z = 2, stationary = false, aggressive = true)
public final class r181OrganicWaste extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

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
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
