package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Jade Kingsnake", x = 2713, y = 2023, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2718, y = 1980, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2742, y = 2004, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2754, y = 1940, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2758, y = 2004, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2759, y = 1975, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2761, y = 2034, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2763, y = 2060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2770, y = 1989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2770, y = 2022, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2788, y = 1989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2788, y = 2021, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2796, y = 2006, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2797, y = 2035, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2798, y = 1973, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2807, y = 2059, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2814, y = 2005, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2816, y = 1949, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2837, y = 2025, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Jade Kingsnake", x = 2843, y = 1994, z = 0, stationary = false, aggressive = true)
public final class JadeKingsnake extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public static final String CANONICAL_NAME = "Jade Kingsnake";

  public JadeKingsnake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Jade Kingsnake",
        "${monster.jade_kingsnake}",
        581,
        0,
        3,
        1158,
        41,
        50,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC#m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachenhelm", 0.005f),
            new MonsterDef.LootDrop("Grim sword of war", 0.02f),
            new MonsterDef.LootDrop("Scroll of orientation center", 0.01f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 53, 106, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        100,
        0,
        1075052544,
        20019,
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
            new MonsterDef.Attack("1d10+40", 450, 50, 0, 0, 1),
            new MonsterDef.Attack("", 0, 50, 10649, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
