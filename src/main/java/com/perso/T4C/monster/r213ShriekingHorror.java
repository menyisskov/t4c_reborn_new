package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r213ShriekingHorror extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r213ShriekingHorror(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Shrieking Horror",
        "${monster.shrieking_horror}",
        967,
        0,
        5,
        2574,
        48,
        109,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        79,
        242,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Mummy bandages", 0.02f)),
        false,
        0.0f,
        59,
        54,
        54,
        67,
        0,
        54,
        0,
        new int[] {73, 73, 97, 48, 5025, 48, 100, 100, 100, 100, 100, 100},
        44,
        186,
        0,
        1077280768,
        20011,
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
            new MonsterDef.Attack("1d62+47", 538, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 60, 10120, 10, 14),
            new MonsterDef.Attack("", 0, 10, 10119, 10, 14),
            new MonsterDef.Attack("", 0, 18, 10086, 10, 14),
            new MonsterDef.Attack("", 0, 18, 10096, 2, 9)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
