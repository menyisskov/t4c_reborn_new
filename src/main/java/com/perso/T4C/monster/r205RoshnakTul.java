package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Roshnak Tul", x = 2652, y = 531, z = 0, stationary = false, aggressive = true)
public final class r205RoshnakTul extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r205RoshnakTul(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Roshnak Tul",
        "${monster.roshnak_tul}",
        842,
        0,
        3,
        1349,
        20,
        40,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        82,
        252,
        java.util.List.of(
            new MonsterDef.LootDrop("Pouch of yellow powder", 0.07f),
            new MonsterDef.LootDrop("Ringmail armor", 0.0069999998f),
            new MonsterDef.LootDrop("Ringmail gauntlets", 0.01f),
            new MonsterDef.LootDrop("Ringmail helmet", 0.01f),
            new MonsterDef.LootDrop("Ringmail leggings", 0.01f),
            new MonsterDef.LootDrop("Crude orcish necklace", 0.002f),
            new MonsterDef.LootDrop("Ringmail boots", 0.01f),
            new MonsterDef.LootDrop("Mana elixir", 0.03f),
            new MonsterDef.LootDrop("Healing potion", 0.02f)),
        false,
        0.0f,
        30,
        35,
        35,
        50,
        0,
        35,
        0,
        new int[] {110, 55, 83, 83, 83, 5000, 100, 100, 100, 100, 100, 100},
        23,
        175,
        0,
        1076232192,
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
            new MonsterDef.Attack("1d21+19", 240, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10096, 2, 11),
            new MonsterDef.Attack("", 0, 60, 10086, 12, 15),
            new MonsterDef.Attack("", 0, 100, 10088, 16, 25)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
