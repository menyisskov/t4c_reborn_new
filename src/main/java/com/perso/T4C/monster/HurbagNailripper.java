package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class HurbagNailripper extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Hurbag Nailripper";

  public HurbagNailripper(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Hurbag Nailripper",
        "${monster.hurbag_nailripper}",
        1808,
        0,
        5,
        4659,
        45,
        102,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        150,
        462,
        java.util.List.of(
            new MonsterDef.LootDrop("Orcish shield", 0.05f),
            new MonsterDef.LootDrop("Rusted hand axe", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.04f),
            new MonsterDef.LootDrop("Serious healing potion", 0.05f)),
        false,
        0.0f,
        50,
        50,
        50,
        83,
        0,
        50,
        0,
        new int[] {98, 49, 74, 74, 74, 5000, 100, 100, 100, 100, 100, 100},
        42,
        178,
        0,
        1077215232,
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
            new MonsterDef.Attack("1d58+44", 514, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 20, 10096, 3, 13),
            new MonsterDef.Attack("", 0, 30, 10120, 3, 13),
            new MonsterDef.Attack("", 0, 30, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 20, 10122, 3, 13),
            new MonsterDef.Attack("", 0, 20, 10088, 15, 25)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
