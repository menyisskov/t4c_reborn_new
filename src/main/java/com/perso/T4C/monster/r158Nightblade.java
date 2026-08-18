package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r158Nightblade extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r158Nightblade(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Nightblade",
        "${monster.nightblade}",
        710,
        0,
        4,
        1575,
        34,
        78,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        62,
        192,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Polished short sword", 0.01f),
            new MonsterDef.LootDrop("Pouch of Blue Cohosh", 0.01f),
            new MonsterDef.LootDrop("Golden chalice", 0.008f)),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {77, 77, 77, 77, 51, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1076953088,
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
        23,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 42, 10096, 3, 13),
            new MonsterDef.Attack("", 0, 42, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 8, 10122, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
