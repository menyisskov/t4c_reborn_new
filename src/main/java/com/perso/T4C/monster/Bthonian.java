package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bthonian", x = 2792, y = 2452, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2796, y = 2456, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2800, y = 2444, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2800, y = 2460, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2804, y = 2440, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2808, y = 2436, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2808, y = 2452, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2812, y = 2448, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Bthonian", x = 2816, y = 2444, z = 2, stationary = false, aggressive = true)
public final class Bthonian extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Bthonian";

  public Bthonian(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bthonian",
        "${monster.bthonian}",
        4373,
        0,
        9,
        21714,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        215,
        660,
        java.util.List.of(new MonsterDef.LootDrop("Shiny silver key", 0.15f)),
        false,
        0.0f,
        135,
        122,
        122,
        159,
        0,
        122,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        120,
        490,
        0,
        1078853632,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 50, 10095, 2, 20),
            new MonsterDef.Attack("1d207+131", 1450, 100, 0, 0, 1),
            new MonsterDef.Attack("", 0, 40, 10088, 2, 20),
            new MonsterDef.Attack("", 0, 10, 10382, 2, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
