package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r271WidowHatchling extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public r271WidowHatchling(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Widow Hatchling",
        "${monster.widow_hatchling}",
        1879,
        0,
        7,
        6776,
        0,
        0,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        125,
        385,
        java.util.List.of(
            new MonsterDef.LootDrop("Spider venom", 0.01f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Flask of liquid spider silk", 0.005f)),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        0,
        77,
        0,
        new int[] {97, 48, 72, 72, 72, 5000, 100, 100, 100, 100, 100, 100},
        70,
        290,
        0,
        1078034432,
        20033,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        40,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 10, 10344, 0, 10),
            new MonsterDef.Attack("1d110+84", 850, 100, 10321, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
