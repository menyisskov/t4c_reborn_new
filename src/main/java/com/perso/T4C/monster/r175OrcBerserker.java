package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r175OrcBerserker extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r175OrcBerserker(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Berserker",
        "${monster.orc_berserker}",
        631,
        0,
        4,
        1311,
        30,
        69,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        57,
        176,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Orcish shield", 0.005f),
            new MonsterDef.LootDrop("Flask of crystal water", 1.0E-4f),
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Ringmail helmet", 0.003f),
            new MonsterDef.LootDrop("Ringmail armor", 0.002f),
            new MonsterDef.LootDrop("Ringmail boots", 0.0034999999f),
            new MonsterDef.LootDrop("Ringmail leggings", 0.003f),
            new MonsterDef.LootDrop("Potion of Heroism", 0.01f)),
        false,
        0.0f,
        47,
        43,
        43,
        53,
        0,
        43,
        0,
        new int[] {105, 52, 78, 78, 78, 5000, 100, 100, 100, 100, 100, 100},
        32,
        138,
        0,
        1076887552,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d40+29", 394, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
