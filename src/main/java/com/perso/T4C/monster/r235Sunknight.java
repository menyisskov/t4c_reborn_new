package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r235Sunknight extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r235Sunknight(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Sun knight",
        "${monster.sun_knight}",
        533,
        0,
        3,
        1027,
        25,
        62,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        50,
        154,
        java.util.List.of(
            new MonsterDef.LootDrop("Sword of Light", 0.05f),
            new MonsterDef.LootDrop("Manastone", 0.02f),
            new MonsterDef.LootDrop("Polished short sword", 0.01f),
            new MonsterDef.LootDrop("Round shield", 0.01f)),
        false,
        0.0f,
        43,
        40,
        40,
        48,
        0,
        40,
        0,
        new int[] {80, 80, 80, 80, 53, 5000, 100, 100, 100, 100, 100, 100},
        28,
        122,
        0,
        1076101120,
        21043,
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
        java.util.List.of(new MonsterDef.Attack("1d38+24", 346, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
