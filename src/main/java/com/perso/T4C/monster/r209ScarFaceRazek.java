package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Scar-Face Razek", x = 2242, y = 1498, z = 0, stationary = false, aggressive = true)
public final class r209ScarFaceRazek extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r209ScarFaceRazek(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Scar-Face Razek",
        "${monster.scar_face_razek}",
        434,
        0,
        2,
        534,
        11,
        25,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        46,
        142,
        java.util.List.of(
            new MonsterDef.LootDrop("Healing potion", 0.05f),
            new MonsterDef.LootDrop("Polished long sword", 0.04f),
            new MonsterDef.LootDrop("Studded leather armor", 0.04f),
            new MonsterDef.LootDrop("Studded leather boots", 0.03f),
            new MonsterDef.LootDrop("Studded leather gloves", 0.03f),
            new MonsterDef.LootDrop("Studded leather pants", 0.03f)),
        false,
        0.0f,
        28,
        26,
        26,
        30,
        0,
        26,
        0,
        new int[] {87, 87, 87, 87, 58, 5000, 100, 100, 100, 100, 100, 100},
        13,
        62,
        0,
        1075314688,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        11,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d15+10", 166, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
