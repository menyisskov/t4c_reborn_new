package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r275Woodstalker extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r275Woodstalker(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Woodstalker",
        "${monster.woodstalker}",
        2086,
        0,
        7,
        7855,
        93,
        211,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        134,
        412,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachenplate", 0.003f),
            new MonsterDef.LootDrop("Scroll of orientation center", 0.05f),
            new MonsterDef.LootDrop("Hickory compound bow", 0.01f),
            new MonsterDef.LootDrop("Bone tipped arrow", 0.02f)),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        0,
        82,
        0,
        new int[] {58, 58, 58, 58, 39, 5000, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1078099968,
        21042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
