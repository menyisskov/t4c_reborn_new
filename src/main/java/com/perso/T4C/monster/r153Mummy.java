package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r153Mummy extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Mummy Dying.wav";
  public static final String SOUND_HIT = "Mummy Hit.wav";

  public r153Mummy(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mummy",
        "${monster.mummy}",
        69,
        0,
        1,
        59,
        4,
        10,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        7,
        22,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f)),
        false,
        0.0f,
        19,
        15,
        18,
        19,
        0,
        16,
        0,
        new int[] {92, 92, 122, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        4,
        26,
        0,
        1073741824,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d7+3", 58, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Crawling Mummy"),
        java.util.Map.of());
  }
}
