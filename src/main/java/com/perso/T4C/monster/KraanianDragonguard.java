package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Dragonguard", x = 2820, y = 2750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Dragonguard", x = 2880, y = 2810, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Dragonguard", x = 2810, y = 2820, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Dragonguard", x = 2890, y = 2740, z = 0, stationary = false, aggressive = true)
public final class KraanianDragonguard extends DataMonster {
  // Reuses the "KraanianFlying" sprite/sound family already used by Kraanian Wyrmling — an
  // elite evolution of the same Kraanian stock, now guarding the true Drakes' lair.
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Dragonguard";

  public KraanianDragonguard(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Dragonguard",
        "${monster.kraanian_dragonguard}",
        130000,
        0,
        0,
        1190000000,
        1100,
        2600,
        30000L,
        "KraanianFlying#h",
        "KraanianFlyingA#h",
        "KraanianFlyingC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        900,
        2700,
        java.util.List.of(new MonsterDef.LootDrop("dragonguards_scale_bracer", 0.03f)),
        false,
        0.0f,
        900,
        800,
        800,
        1000,
        0,
        800,
        0,
        new int[] {63, 5000, 63, -63, 63, 5000, 100, 100, 100, 100, 100, 100},
        750,
        3000,
        0,
        900,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        90,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d1450+1150", 9500, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("KraanianDragonguard"),
        java.util.Map.of());
  }
}
