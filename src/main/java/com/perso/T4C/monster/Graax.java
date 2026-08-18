package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Graax", x = 315, y = 464, z = 2, stationary = false, aggressive = true)
public final class Graax extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Graax";

  public Graax(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Graax",
        "${monster.graax}",
        398,
        0,
        2,
        470,
        10,
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
            new MonsterDef.LootDrop("Rusted long sword", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.06f),
            new MonsterDef.LootDrop("Mana elixir", 0.05f),
            new MonsterDef.LootDrop("Ring of light", 0.04f),
            new MonsterDef.LootDrop("Leather belt", 0.04f)),
        false,
        0.0f,
        27,
        25,
        25,
        29,
        0,
        25,
        0,
        new int[] {88, 88, 58, 117, 88, 5000, 100, 100, 100, 100, 100, 100},
        12,
        58,
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
        java.util.List.of(new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
