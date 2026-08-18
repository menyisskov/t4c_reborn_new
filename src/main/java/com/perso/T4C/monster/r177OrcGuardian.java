package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r177OrcGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r177OrcGuardian(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Guardian",
        "${monster.orc_guardian}",
        847,
        0,
        4,
        2090,
        42,
        94,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        71,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Orcish shield", 0.0069999998f),
            new MonsterDef.LootDrop("Potion of fury", 0.02f),
            new MonsterDef.LootDrop("Stone key", 0.01f),
            new MonsterDef.LootDrop("Fine steel scimitar", 5.0E-4f),
            new MonsterDef.LootDrop("Pouch of Woody Nightshade", 0.01f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {100, 50, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
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
        java.util.List.of(new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
