package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Lighthaven Ranger",
    x = 2777,
    y = 1201,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Lighthaven Ranger",
    x = 2780,
    y = 1209,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Lighthaven Ranger",
    x = 2783,
    y = 1203,
    z = 0,
    stationary = false,
    aggressive = false)
public final class LighthavenRanger extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Lighthaven Ranger";

  public LighthavenRanger(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Lighthaven Ranger",
        "${monster.lighthaven_ranger}",
        847,
        0,
        0,
        0,
        42,
        94,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {75, 75, 75, 75, 50, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
        10011,
        40023,
        40022,
        40020,
        0,
        40021,
        40217,
        40175,
        40215,
        -100,
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
