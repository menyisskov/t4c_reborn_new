package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class BandagedHorror extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Bandaged Horror";

  public BandagedHorror(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bandaged Horror",
        "${monster.bandaged_horror}",
        966,
        0,
        5,
        2574,
        48,
        109,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        79,
        242,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Mummy bandages", 0.03f),
            new MonsterDef.LootDrop("Gleaming shard", 0.02f)),
        false,
        0.0f,
        59,
        54,
        54,
        67,
        0,
        54,
        0,
        new int[] {73, 73, 97, 48, 5025, 48, 100, 100, 100, 100, 100, 100},
        44,
        186,
        0,
        1077280768,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d62+47", 538, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
