package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r183Paladin extends DataMonster {
  public static final String SOUND_ATTACK = null;
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r183Paladin(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Paladin",
        "${monster.paladin}",
        353,
        0,
        3,
        543,
        17,
        39,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        35,
        110,
        java.util.List.of(
            new MonsterDef.LootDrop("Sword of Light", 0.01f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of partial protection from evil", 0.02f),
            new MonsterDef.LootDrop("Polished short sword", 0.01f)),
        false,
        0.0f,
        35,
        32,
        32,
        39,
        0,
        32,
        0,
        new int[] {84, 84, 84, 84, 112, 5000, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        10009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        46,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d23+16", 250, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
