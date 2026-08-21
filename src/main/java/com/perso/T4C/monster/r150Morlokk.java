package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r150Morlokk extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 3.wav";
  public static final String SOUND_DEATH = "Kobold Dying.wav";
  public static final String SOUND_HIT = "Kobold Hit.wav";

  public r150Morlokk(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Morlokk",
        "${monster.morlokk}",
        1462,
        0,
        6,
        4708,
        69,
        156,
        30000L,
        "Kobold#l",
        "KoboldA#g",
        "KoboldC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        106,
        324,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Mithril blade", 0.01f),
            new MonsterDef.LootDrop("Potion of clear thought", 0.02f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f)),
        false,
        0.0f,
        74,
        68,
        68,
        85,
        0,
        68,
        0,
        new int[] {88, 66, 66, 66, 66, 5025, 100, 100, 100, 100, 100, 100},
        59,
        246,
        0,
        1077739520,
        20004,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        41,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d88+68", 718, 40, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10227, 0, 10),
            new MonsterDef.Attack("", 0, 25, 10094, 0, 10),
            new MonsterDef.Attack("", 0, 3, 10347, 0, 10),
            new MonsterDef.Attack("", 0, 2, 10388, 0, 10),
            new MonsterDef.Attack("", 0, 10, 10348, 0, 10),
            new MonsterDef.Attack("", 0, 5, 10382, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
