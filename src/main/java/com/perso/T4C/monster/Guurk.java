package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Guurk", x = 210, y = 621, z = 2, stationary = false, aggressive = true)
public final class Guurk extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Guurk";

  public Guurk(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Guurk",
        "${monster.guurk}",
        398,
        0,
        2,
        460,
        8,
        23,
        30000L,
        "AtrocityBoss#h",
        "AtrocityBossA#h",
        "AtrocityBossC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        42,
        132,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of mana", 0.05f),
            new MonsterDef.LootDrop("Mana elixir", 0.05f),
            new MonsterDef.LootDrop("Leather belt", 0.04f)),
        false,
        0.0f,
        20,
        25,
        25,
        36,
        0,
        25,
        0,
        new int[] {88, 88, 58, 117, 88, 5000, 100, 100, 100, 100, 100, 100},
        12,
        68,
        0,
        1075314688,
        20040,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        10,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d16+7", 154, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 60, 10086, 4, 12),
            new MonsterDef.Attack("", 0, 40, 10120, 4, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
