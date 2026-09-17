package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Wyrmling", x = 1870, y = 1920, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Wyrmling", x = 1930, y = 1980, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Wyrmling", x = 1860, y = 1990, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Wyrmling", x = 1940, y = 1910, z = 0, stationary = false, aggressive = true)
public final class KraanianWyrmling extends DataMonster {
  // Reuses the "KraanianFlying" sprite/sound family already used by the low-level Kraanian
  // Flyer, but authored as a distinct, much higher-level monster guarding the Lesser Drake's
  // aerie.
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Wyrmling";

  public KraanianWyrmling(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Wyrmling",
        "${monster.kraanian_wyrmling}",
        20300,
        0,
        10,
        128000,
        330,
        560,
        30000L,
        "KraanianFlying#h",
        "KraanianFlyingA#h",
        "KraanianFlyingC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        330,
        1000,
        java.util.List.of(new MonsterDef.LootDrop("wyrmling_scale_bracer", 0.03f)),
        false,
        0.0f,
        190,
        180,
        160,
        115,
        0,
        140,
        0,
        new int[] {130, 80, 90, 130, 100, 100, 100, 100, 100, 100, 100, 100},
        220,
        550,
        0,
        150,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        80,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d330+274", 2065, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("KraanianWyrmling"),
        java.util.Map.of());
  }
}
