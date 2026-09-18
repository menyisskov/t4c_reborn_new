package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Cinder Whelp", x = 1870, y = 1570, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Cinder Whelp", x = 1920, y = 1630, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Cinder Whelp", x = 1950, y = 1580, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Cinder Whelp", x = 1860, y = 1640, z = 0, stationary = false, aggressive = true)
public final class CinderWhelp extends DataMonster {
  // Reuses the Wolf animation/sound family (same precedent as FenrisWolf/GreatWolf) —
  // fire theme carried by name/stats/resists, not by a dedicated sprite.
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Cinder Whelp";

  public CinderWhelp(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Cinder Whelp",
        "${monster.cinder_whelp}",
        1650,
        0,
        6,
        6700,
        60,
        105,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        100,
        320,
        java.util.List.of(new MonsterDef.LootDrop("torch", 0.02f)),
        false,
        0.0f,
        69,
        69,
        69,
        84,
        0,
        69,
        0,
        new int[] {100, 90, 60, 180, 100, 100, 100, 100, 100, 100, 100, 100},
        58,
        240,
        0,
        60,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d100+77", 700, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("CinderWhelp"),
        java.util.Map.of());
  }
}
