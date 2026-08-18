package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Shadow Demon", x = 1438, y = 2730, z = 2, stationary = false, aggressive = true)
public final class r211ShadowDemon extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public r211ShadowDemon(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Shadow Demon",
        "${monster.shadow_demon}",
        3259,
        0,
        9,
        14831,
        135,
        306,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        179,
        550,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana prism", 0.01f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f)),
        false,
        0.0f,
        115,
        104,
        104,
        135,
        0,
        104,
        0,
        new int[] {75, 75, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1078525952,
        21013,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        37,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10381, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10375, 1, 10),
            new MonsterDef.Attack("", 0, 50, 10374, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10367, 1, 10),
            new MonsterDef.Attack("", 0, 10, 10389, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
