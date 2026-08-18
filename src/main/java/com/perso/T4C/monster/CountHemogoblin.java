package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CountHemogoblin extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Count Hemogoblin";

  public CountHemogoblin(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Count Hemogoblin",
        "${monster.count_hemogoblin}",
        1162,
        0,
        3,
        2308,
        28,
        63,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC#l",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        106,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Red cape", 0.08f),
            new MonsterDef.LootDrop("Potion of mana", 0.03f),
            new MonsterDef.LootDrop("Manastone", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.04f),
            new MonsterDef.LootDrop("Flask of Goblin Blood", 0.05f),
            new MonsterDef.LootDrop("Goblin Blade", 0.03f),
            new MonsterDef.LootDrop("Skeleton bone", 0.0075f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {106, 53, 106, 53, 5025, 53, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        20041,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10086, 3, 11),
            new MonsterDef.Attack("", 0, 30, 10119, 3, 11),
            new MonsterDef.Attack("", 0, 30, 10090, 3, 11)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
