package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Skrull", x = 949, y = 1704, z = 0, stationary = false, aggressive = true)
public final class r231Skrull extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r231Skrull(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skrull",
        "${monster.skrull}",
        765,
        0,
        9,
        3535,
        37,
        84,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        132,
        406,
        java.util.List.of(
            new MonsterDef.LootDrop("Ringmail armor", 0.002f),
            new MonsterDef.LootDrop("Ringmail boots", 0.0034999999f),
            new MonsterDef.LootDrop("Ringmail leggings", 0.003f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Skeleton bone", 0.01f)),
        false,
        0.0f,
        52,
        48,
        48,
        59,
        0,
        48,
        0,
        new int[] {76, 76, 101, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        37,
        158,
        0,
        1077018624,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d48+36", 454, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 55, 10096, 4, 10),
            new MonsterDef.Attack("", 0, 35, 10086, 3, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
