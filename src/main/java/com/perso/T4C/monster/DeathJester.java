package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Death Jester", x = 105, y = 2310, z = 1, stationary = false, aggressive = true)
public final class DeathJester extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Death Jester";

  public DeathJester(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Death Jester",
        "${monster.death_jester}",
        1642,
        0,
        4,
        3960,
        40,
        91,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        140,
        430,
        java.util.List.of(
            new MonsterDef.LootDrop("Healing potion", 0.05f),
            new MonsterDef.LootDrop("Polished broadsword", 0.01f),
            new MonsterDef.LootDrop("Round shield", 0.03f),
            new MonsterDef.LootDrop("Death Jester Skull", 1.0f),
            new MonsterDef.LootDrop("Crown of the Jester", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.01f),
            new MonsterDef.LootDrop("Pouch of Witch Hazel", 0.01f),
            new MonsterDef.LootDrop("Magical lute", 0.5f)),
        false,
        0.0f,
        54,
        50,
        50,
        61,
        0,
        50,
        0,
        new int[] {75, 75, 100, 50, 5025, 50, 100, 100, 100, 125, 100, 100},
        39,
        166,
        0,
        1077084160,
        20012,
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
        java.util.List.of(
            new MonsterDef.Attack("1d52+39", 478, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 35, 10119, 3, 14),
            new MonsterDef.Attack("", 0, 50, 10086, 3, 14)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
