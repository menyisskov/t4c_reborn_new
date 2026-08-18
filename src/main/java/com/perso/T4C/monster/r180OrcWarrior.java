package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r180OrcWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r180OrcWarrior(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Warrior",
        "${monster.orc_warrior}",
        199,
        0,
        2,
        235,
        10,
        23,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        21,
        66,
        java.util.List.of(
            new MonsterDef.LootDrop("Feather", 0.01f),
            new MonsterDef.LootDrop("Leather armor", 0.003f),
            new MonsterDef.LootDrop("Studded leather helmet", 0.009f),
            new MonsterDef.LootDrop("Dagger of Piercing", 8.0E-4f),
            new MonsterDef.LootDrop("Iron key", 0.01f),
            new MonsterDef.LootDrop("Healing potion", 0.01f)),
        false,
        0.0f,
        27,
        23,
        26,
        29,
        0,
        25,
        0,
        new int[] {117, 58, 88, 88, 88, 5000, 100, 100, 100, 100, 100, 100},
        12,
        58,
        0,
        1075314688,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
