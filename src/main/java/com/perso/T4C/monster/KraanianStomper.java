package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Stomper", x = 2047, y = 2235, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Stomper", x = 2056, y = 2254, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Stomper", x = 2099, y = 2221, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Stomper", x = 2111, y = 2211, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Stomper", x = 2122, y = 2191, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Stomper", x = 2132, y = 2201, z = 1, stationary = false, aggressive = true)
public final class KraanianStomper extends DataMonster {
  public static final String SOUND_ATTACK = "Atrocity Attack.wav";
  public static final String SOUND_DEATH = "Atrocity Dying.wav";
  public static final String SOUND_HIT = "Atrocity Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Stomper";

  public KraanianStomper(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Stomper",
        "${monster.kraanian_stomper}",
        1762,
        0,
        7,
        6168,
        81,
        183,
        30000L,
        "Tank#h",
        "TankA#h",
        "TankC#n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        120,
        368,
        java.util.List.of(new MonsterDef.LootDrop("Yellow crystal shard", 0.1f)),
        false,
        0.0f,
        82,
        75,
        75,
        95,
        0,
        75,
        0,
        new int[] {62, 62, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        67,
        278,
        0,
        1077968896,
        20037,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d103+80", 814, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
