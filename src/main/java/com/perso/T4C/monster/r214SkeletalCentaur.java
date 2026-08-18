package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r214SkeletalCentaur extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public r214SkeletalCentaur(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeletal Centaur",
        "${monster.skeletal_centaur}",
        1879,
        0,
        7,
        6776,
        85,
        194,
        30000L,
        "64kCentaurSkeleton#i",
        "64kCentaurSkeletonA#i",
        "64kCentaurSkeletonC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        125,
        385,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        0,
        77,
        0,
        new int[] {61, 61, 61, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        70,
        290,
        0,
        1078034432,
        20063,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        36,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d110+84", 850, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10359, 1, 10),
            new MonsterDef.Attack("", 0, 10, 10357, 1, 10),
            new MonsterDef.Attack("", 0, 20, 10096, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
