package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r234Stinkbreath extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r234Stinkbreath(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Stinkbreath",
        "${monster.stinkbreath}",
        1812,
        0,
        5,
        4661,
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
            new MonsterDef.LootDrop("Healing potion", 0.05f),
            new MonsterDef.LootDrop("Healing potion", 0.05f),
            new MonsterDef.LootDrop("Potion of fury", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Orcish shield", 0.0025f),
            new MonsterDef.LootDrop("Polished long sword", 0.001f)),
        false,
        0.0f,
        57,
        52,
        52,
        65,
        0,
        52,
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
            new MonsterDef.Attack("", 0, 20, 10091, 3, 12),
            new MonsterDef.Attack("", 0, 70, 10096, 4, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
