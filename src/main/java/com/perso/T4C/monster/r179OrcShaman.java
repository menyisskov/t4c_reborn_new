package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r179OrcShaman extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r179OrcShaman(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Shaman",
        "${monster.orc_shaman}",
        254,
        0,
        2,
        337,
        13,
        29,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        26,
        82,
        java.util.List.of(
            new MonsterDef.LootDrop("Feather", 0.01f),
            new MonsterDef.LootDrop("Iron ring", 0.005f),
            new MonsterDef.LootDrop("Light healing potion", 0.01f),
            new MonsterDef.LootDrop("Potion of mana", 0.01f),
            new MonsterDef.LootDrop("Woodland robe", 0.001f)),
        false,
        0.0f,
        25,
        30,
        28,
        35,
        0,
        26,
        0,
        new int[] {115, 57, 86, 86, 86, 5000, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
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
            new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 25, 10094, 4, 15),
            new MonsterDef.Attack("", 0, 75, 10120, 4, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
