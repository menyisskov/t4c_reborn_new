package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r200RibbonFiend extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public r200RibbonFiend(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ribbon Fiend",
        "${monster.ribbon_fiend}",
        1153,
        0,
        6,
        3462,
        60,
        135,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Garb of the dead", 0.005f),
            new MonsterDef.LootDrop("Blackened iron key", 0.005f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {84, 84, 112, 56, 5025, 70, 100, 100, 100, 100, 100, 100},
        50,
        210,
        0,
        1077477376,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        34,
        0,
        false,
        java.util.List.of(
            new MonsterDef.Attack("1d76+59", 610, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10091, 3, 10),
            new MonsterDef.Attack("", 0, 3, 10359, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10378, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10119, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
