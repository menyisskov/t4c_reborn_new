package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Dark Spider", x = 611, y = 155, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 639, y = 188, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 653, y = 138, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 675, y = 183, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 675, y = 92, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 689, y = 107, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 700, y = 205, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 706, y = 142, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 712, y = 126, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 713, y = 101, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 720, y = 76, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 721, y = 206, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 727, y = 151, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 732, y = 166, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 739, y = 129, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 744, y = 139, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 744, y = 98, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 748, y = 250, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 752, y = 235, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 755, y = 101, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 767, y = 221, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 781, y = 155, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 783, y = 77, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 792, y = 103, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 800, y = 143, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 800, y = 145, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 808, y = 194, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 819, y = 136, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 820, y = 153, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Spider", x = 828, y = 183, z = 1, stationary = false, aggressive = true)
public final class DarkSpider extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public static final String CANONICAL_NAME = "Dark Spider";

  public DarkSpider(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dark Spider",
        "${monster.dark_spider}",
        181,
        0,
        2,
        209,
        9,
        22,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC#m",
        "Spider Attack.wav",
        "Spider Dying.wav",
        "Spider Hit.wav",
        19,
        60,
        java.util.List.of(new MonsterDef.LootDrop("Vial of Spider Venom", 0.04f)),
        false,
        0.0f,
        26,
        24,
        24,
        28,
        0,
        24,
        0,
        new int[] {118, 59, 88, 88, 88, 5000, 100, 100, 100, 100, 100, 100},
        11,
        54,
        0,
        0,
        20007,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        21,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d14+8", 167, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
