package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Deathstalker extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Deathstalker";

  public Deathstalker(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Deathstalker",
        "${monster.deathstalker}",
        1879,
        0,
        7,
        6776,
        85,
        194,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        125,
        385,
        java.util.List.of(
            new MonsterDef.LootDrop("Polished bone key", 0.05f),
            new MonsterDef.LootDrop("Adamantite blade", 0.0025f),
            new MonsterDef.LootDrop("Collector book", 0.005f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Cord of treachery", 0.01f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.005f)),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        0,
        77,
        0,
        new int[] {72, 72, 72, 72, 97, 5000, 100, 100, 100, 100, 100, 100},
        70,
        290,
        0,
        1078034432,
        21042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d110+84", 850, 75, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10354, 9, 12),
            new MonsterDef.Attack("", 0, 10, 10355, 0, 8),
            new MonsterDef.Attack("", 0, 10, 10088, 9, 12),
            new MonsterDef.Attack("", 0, 5, 10274, 0, 8),
            new MonsterDef.Attack("", 0, 3, 10375, 0, 8)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
