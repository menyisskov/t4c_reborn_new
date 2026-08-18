package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Crypt Custodian", x = 1549, y = 682, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1564, y = 675, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1566, y = 702, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1569, y = 699, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1574, y = 655, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1575, y = 696, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1581, y = 715, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1583, y = 663, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1584, y = 648, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1594, y = 675, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1595, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1600, y = 702, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1604, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1607, y = 623, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1615, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1625, y = 602, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Crypt Custodian", x = 1626, y = 610, z = 2, stationary = false, aggressive = true)
public final class CryptCustodian extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

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
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
