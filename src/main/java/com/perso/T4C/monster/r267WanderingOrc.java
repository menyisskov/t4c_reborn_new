package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r267WanderingOrc extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r267WanderingOrc(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wandering Orc",
        "${monster.wandering_orc}",
        132,
        0,
        2,
        136,
        7,
        17,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        14,
        44,
        java.util.List.of(
            new MonsterDef.LootDrop("Studded leather armor", 0.01f),
            new MonsterDef.LootDrop("Studded leather pants", 0.01f),
            new MonsterDef.LootDrop("Studded leather helmet", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Flail of stability", 5.0E-4f)),
        false,
        0.0f,
        23,
        22,
        22,
        24,
        0,
        22,
        0,
        new int[] {120, 60, 90, 90, 90, 5000, 100, 100, 100, 100, 100, 100},
        8,
        42,
        0,
        1074790400,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        40,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d11+6", 106, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
