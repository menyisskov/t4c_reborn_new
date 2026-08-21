package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Undead Bat", x = 14, y = 306, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 199, y = 101, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 240, y = 71, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 273, y = 55, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 292, y = 31, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 300, y = 21, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 331, y = 52, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 35, y = 281, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Bat", x = 9, y = 335, z = 1, stationary = false, aggressive = true)
public final class r262UndeadBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r262UndeadBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Undead Bat",
        "${monster.undead_bat}",
        55,
        0,
        1,
        46,
        4,
        8,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        5,
        16,
        java.util.List.of(new MonsterDef.LootDrop("Decaying Bat Wings", 0.05f)),
        false,
        0.0f,
        18,
        17,
        17,
        18,
        0,
        17,
        0,
        new int[] {61, 123, 123, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        3,
        27,
        0,
        0,
        20002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        90,
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+3", 46, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
