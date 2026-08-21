package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r174OrcBattlemage extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r174OrcBattlemage(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Battlemage",
        "${monster.orc_battlemage}",
        684,
        0,
        4,
        1480,
        33,
        74,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        61,
        187,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.04f),
            new MonsterDef.LootDrop("Polished long sword", 0.02f),
            new MonsterDef.LootDrop("Pouch of Woody Nightshade", 0.01f),
            new MonsterDef.LootDrop("Pouch of Witch Hazel", 0.008f),
            new MonsterDef.LootDrop("Potion of Heroism", 0.01f)),
        false,
        0.0f,
        49,
        45,
        45,
        55,
        0,
        45,
        0,
        new int[] {103, 51, 78, 78, 78, 5000, 100, 100, 100, 100, 100, 100},
        34,
        146,
        0,
        1076953088,
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
        java.util.List.of(
            new MonsterDef.Attack("1d42+32", 418, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10120, 3, 13),
            new MonsterDef.Attack("", 0, 40, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 15, 10122, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
