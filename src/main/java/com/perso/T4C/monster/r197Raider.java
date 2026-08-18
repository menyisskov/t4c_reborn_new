package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r197Raider extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r197Raider(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Raider",
        "${monster.raider}",
        274,
        0,
        2,
        376,
        14,
        31,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        28,
        88,
        java.util.List.of(
            new MonsterDef.LootDrop("Ringmail armor", 0.006f),
            new MonsterDef.LootDrop("Leather boots", 0.01f),
            new MonsterDef.LootDrop("Iron key", 0.08f),
            new MonsterDef.LootDrop("Healing potion", 0.02f)),
        false,
        0.0f,
        31,
        29,
        29,
        34,
        0,
        29,
        0,
        new int[] {86, 86, 86, 86, 57, 5000, 100, 100, 100, 100, 100, 100},
        16,
        74,
        0,
        1075314688,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        11,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d18+13", 212, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
