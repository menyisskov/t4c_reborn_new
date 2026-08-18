package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CryptCustodian extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Crypt Custodian";

  public CryptCustodian(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Crypt Custodian",
        "${monster.crypt_custodian}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "64kCentaurSkeleton#i",
        "64kCentaurSkeletonA#i",
        "64kCentaurSkeletonC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        116,
        357,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Critical healing potion", 0.03f),
            new MonsterDef.LootDrop("Mithril long sword", 0.0025f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f),
            new MonsterDef.LootDrop("Runed halberd", 0.005f)),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {68, 68, 68, 68, 5025, 100, 100, 100, 0, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
        20063,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        42,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d99+77", 790, 20, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10361, 0, 12),
            new MonsterDef.Attack("", 0, 5, 10360, 0, 12),
            new MonsterDef.Attack("", 0, 5, 10363, 0, 12),
            new MonsterDef.Attack("", 0, 10, 10362, 0, 12),
            new MonsterDef.Attack("", 0, 20, 10094, 0, 12),
            new MonsterDef.Attack("", 0, 20, 10364, 0, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
