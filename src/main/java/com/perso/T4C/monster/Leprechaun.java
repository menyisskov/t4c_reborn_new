package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Leprechaun extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Leprechaun";

  public Leprechaun(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Leprechaun",
        "${monster.leprechaun}",
        1321,
        0,
        6,
        4062,
        0,
        0,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        98,
        302,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana prism", 0.01f),
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f),
            new MonsterDef.LootDrop("Hickory compound bow", 0.005f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f)),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {108, 54, 81, 81, 81, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20041,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        41,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 60, 10119, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10365, 1, 12),
            new MonsterDef.Attack("", 0, 35, 10364, 1, 12),
            new MonsterDef.Attack("1d81+62", 670, 100, 10347, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
