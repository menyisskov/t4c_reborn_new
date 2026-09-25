package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Plague", x = 1111, y = 1730, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2052, y = 2225, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2067, y = 2223, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2073, y = 2268, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2080, y = 2240, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2084, y = 2217, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2086, y = 2273, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2095, y = 2227, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2099, y = 2256, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2101, y = 2235, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2115, y = 2205, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2116, y = 2226, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Plague", x = 2124, y = 2198, z = 1, stationary = false, aggressive = true)
public final class KraanianPlague extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Plague";

  public KraanianPlague(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Plague",
        "${monster.kraanian_plague}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        116,
        357,
        java.util.List.of(new MonsterDef.LootDrop("item.plague_eaten_key", 0.05f)),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {42, 84, 63, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
        20034,
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
        java.util.List.of(new MonsterDef.Attack("1d99+77", 790, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
