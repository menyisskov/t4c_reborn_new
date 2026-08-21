package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bat", x = 166, y = 208, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 187, y = 131, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 194, y = 223, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 212, y = 72, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 219, y = 191, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 230, y = 108, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 241, y = 93, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 244, y = 145, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 284, y = 127, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 286, y = 113, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 294, y = 304, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 313, y = 282, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 323, y = 415, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 326, y = 408, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 332, y = 350, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 334, y = 405, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 339, y = 371, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 353, y = 366, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 358, y = 363, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bat", x = 371, y = 340, z = 1, stationary = false, aggressive = true)
public final class Bat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public static final String CANONICAL_NAME = "Bat";

  public Bat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bat",
        "${monster.bat}",
        27,
        0,
        1,
        21,
        2,
        5,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1,
        5,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f)),
        false,
        0.0f,
        12,
        15,
        18,
        16,
        0,
        14,
        0,
        new int[] {62, 124, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        1,
        14,
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
        50,
        16,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d4+1", 22, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
