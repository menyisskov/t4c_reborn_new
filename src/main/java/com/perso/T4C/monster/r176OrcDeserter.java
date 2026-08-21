package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Orc Deserter", x = 1002, y = 2088, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1010, y = 2131, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1037, y = 1934, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1051, y = 2038, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1069, y = 1917, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1104, y = 2010, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1109, y = 1981, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1118, y = 1951, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1157, y = 2044, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1158, y = 2044, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Deserter", x = 1159, y = 2053, z = 0, stationary = false, aggressive = true)
public final class r176OrcDeserter extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r176OrcDeserter(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Deserter",
        "${monster.orc_deserter}",
        374,
        0,
        3,
        590,
        18,
        41,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        37,
        115,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Orcish shield", 0.005f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Flask of crystal water", 1.0E-4f)),
        false,
        0.0f,
        36,
        33,
        33,
        40,
        0,
        33,
        0,
        new int[] {111, 55, 84, 84, 84, 5000, 100, 100, 100, 100, 100, 100},
        21,
        94,
        0,
        1076101120,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        30,
        22,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d24+17", 261, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
