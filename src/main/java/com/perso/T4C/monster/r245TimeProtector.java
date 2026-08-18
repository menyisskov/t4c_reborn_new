package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r245TimeProtector extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r245TimeProtector(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Time Protector",
        "${monster.time_protector}",
        6334,
        0,
        11,
        34954,
        0,
        0,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        269,
        825,
        java.util.List.of(new MonsterDef.LootDrop("Bracer of leaves", 0.05f)),
        false,
        0.0f,
        165,
        149,
        149,
        195,
        0,
        149,
        0,
        new int[] {63, 63, 63, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        150,
        610,
        0,
        1079164928,
        20018,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 100, 10120, 2, 15),
            new MonsterDef.Attack("1d258+152", 1810, 100, 10120, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
