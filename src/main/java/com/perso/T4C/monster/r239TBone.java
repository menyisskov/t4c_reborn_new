package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "T-Bone", x = 189, y = 2975, z = 2, stationary = false, aggressive = true)
public final class r239TBone extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r239TBone(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "T-Bone",
        "${monster.t_bone}",
        1162,
        0,
        3,
        2308,
        28,
        63,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        106,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Ringmail helmet", 0.002f),
            new MonsterDef.LootDrop("Ringmail armor", 0.001f),
            new MonsterDef.LootDrop("Ringmail boots", 0.002f),
            new MonsterDef.LootDrop("Ringmail leggings", 0.002f),
            new MonsterDef.LootDrop("Polished broadsword", 0.001f),
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.01f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 106, 53, 5025, 53, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 60, 10086, 3, 12),
            new MonsterDef.Attack("", 0, 40, 10119, 3, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
