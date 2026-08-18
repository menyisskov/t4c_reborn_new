package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r192PsykowaspTrooper extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r192PsykowaspTrooper(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psykowasp Trooper",
        "${monster.psykowasp_trooper}",
        313,
        0,
        2,
        453,
        15,
        35,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Wasp Attack.wav",
        "Wasp Dying.wav",
        "Wasp Hit.wav",
        32,
        99,
        java.util.List.of(new MonsterDef.LootDrop("Psykowasp nectar", 0.005f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {56, 113, 85, 85, 85, 5000, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        45,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
