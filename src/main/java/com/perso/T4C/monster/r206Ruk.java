package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ruk", x = 193, y = 2639, z = 2, stationary = false, aggressive = true)
public final class r206Ruk extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public r206Ruk(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ruk",
        "${monster.ruk}",
        508,
        0,
        2,
        674,
        13,
        29,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        52,
        164,
        java.util.List.of(
            new MonsterDef.LootDrop("Golden ring", 0.05f),
            new MonsterDef.LootDrop("Goblin leather armor", 0.05f),
            new MonsterDef.LootDrop("Goblin Blade", 0.05f)),
        false,
        0.0f,
        30,
        28,
        28,
        33,
        0,
        28,
        0,
        new int[] {115, 57, 86, 86, 86, 5025, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
        20041,
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
            new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 60, 10086, 8, 13),
            new MonsterDef.Attack("", 0, 40, 10094, 8, 13),
            new MonsterDef.Attack("", 0, 90, 10120, 3, 7),
            new MonsterDef.Attack("", 0, 10, 10122, 3, 7)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
