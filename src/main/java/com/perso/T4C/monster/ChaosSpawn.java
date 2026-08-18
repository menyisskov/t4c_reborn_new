package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ChaosSpawn extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Chaos Spawn";

  public ChaosSpawn(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Chaos Spawn",
        "${monster.chaos_spawn}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "AtrocityBoss#h",
        "AtrocityBossA#h",
        "AtrocityBossC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        98,
        302,
        java.util.List.of(
            new MonsterDef.LootDrop("Chaos key", 0.04f),
            new MonsterDef.LootDrop("Serious healing potion", 0.03f),
            new MonsterDef.LootDrop("Scale mail", 0.005f),
            new MonsterDef.LootDrop("High metal flail", 0.01f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Cracked blue bracer", 0.005f)),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {81, 81, 54, 108, 81, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20040,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10375, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10381, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10118, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10096, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
