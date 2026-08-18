package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Deep One", x = 1004, y = 434, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 1006, y = 362, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 1009, y = 204, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 1016, y = 428, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 1055, y = 400, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 1083, y = 404, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 278, y = 2293, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 280, y = 2283, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 286, y = 2260, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 288, y = 2245, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 303, y = 2298, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 323, y = 2198, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 327, y = 2288, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 334, y = 2267, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 337, y = 2192, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 349, y = 2201, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 349, y = 2226, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 350, y = 2270, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 351, y = 2208, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 352, y = 2237, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 355, y = 2187, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 360, y = 2223, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 360, y = 2272, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 363, y = 2285, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 666, y = 161, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 690, y = 181, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 723, y = 109, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 733, y = 123, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 746, y = 176, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 757, y = 235, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 766, y = 301, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 827, y = 357, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 853, y = 307, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 863, y = 347, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 868, y = 241, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 878, y = 355, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 927, y = 418, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 941, y = 365, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 946, y = 165, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 965, y = 398, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Deep One", x = 977, y = 213, z = 2, stationary = false, aggressive = true)
public final class DeepOne extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Deep One";

  public DeepOne(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Deep One",
        "${monster.deep_one}",
        631,
        0,
        4,
        1311,
        30,
        69,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        57,
        176,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f)),
        false,
        0.0f,
        47,
        43,
        43,
        53,
        0,
        43,
        0,
        new int[] {78, 78, 52, 105, 78, 5000, 100, 100, 100, 100, 100, 100},
        32,
        138,
        0,
        1076887552,
        20026,
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
        java.util.List.of(new MonsterDef.Attack("1d40+29", 394, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
