package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r187Psimonk extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r187Psimonk(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psi monk",
        "${monster.psi_monk}",
        215,
        0,
        2,
        268,
        12,
        25,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        23,
        71,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Monk white sash", 0.02f),
            new MonsterDef.LootDrop("Rang kwan", 0.03f),
            new MonsterDef.LootDrop("Shimmering white robe", 0.01f)),
        false,
        0.0f,
        28,
        26,
        26,
        30,
        0,
        26,
        0,
        new int[] {87, 87, 87, 87, 116, 5000, 100, 100, 100, 100, 100, 100},
        13,
        55,
        0,
        1076101120,
        10011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        46,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d14+11", 150, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
