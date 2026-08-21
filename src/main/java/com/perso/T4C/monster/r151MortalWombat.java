package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Mortal Wombat", x = 1542, y = 372, z = 2, stationary = false, aggressive = true)
public final class r151MortalWombat extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public r151MortalWombat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mortal Wombat",
        "${monster.mortal_wombat}",
        2301,
        0,
        8,
        9409,
        108,
        245,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC!o",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        143,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Polished bone key", 0.05f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Finely cut carnelian", 0.01f),
            new MonsterDef.LootDrop("Finely cut ruby", 0.002f),
            new MonsterDef.LootDrop("Finely cut garnet", 5.0E-4f),
            new MonsterDef.LootDrop("Oak compound bow", 0.005f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        0,
        86,
        0,
        new int[] {90, 45, 67, 67, 67, 5000, 100, 100, 100, 100, 100, 100},
        90,
        330,
        0,
        1078198272,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d138+107", 970, 40, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10347, 11, 15),
            new MonsterDef.Attack("", 0, 25, 10119, 0, 10),
            new MonsterDef.Attack("", 0, 10, 10355, 0, 10),
            new MonsterDef.Attack("", 0, 25, 10096, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
