package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r238TauntingHorror extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r238TauntingHorror(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Taunting Horror",
        "${monster.taunting_horror}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC#m",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Torch", 0.02f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Potion of fury", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.01f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {53, 106, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        20038,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
