package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Carrion Crawler", x = 386, y = 524, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 398, y = 545, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 408, y = 498, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 409, y = 519, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 434, y = 472, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 437, y = 581, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 441, y = 567, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 448, y = 477, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 466, y = 434, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 470, y = 454, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 488, y = 615, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 500, y = 642, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 502, y = 405, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 504, y = 415, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 516, y = 440, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 518, y = 605, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 532, y = 613, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 533, y = 438, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 546, y = 468, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 555, y = 595, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 558, y = 567, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 582, y = 560, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 583, y = 511, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 585, y = 487, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 621, y = 526, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Carrion Crawler", x = 648, y = 376, z = 1, stationary = false, aggressive = true)
public final class CarrionCrawler extends DataMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public static final String CANONICAL_NAME = "Carrion Crawler";

  public CarrionCrawler(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Carrion Crawler",
        "${monster.carrion_crawler}",
        1801,
        0,
        7,
        6369,
        82,
        187,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        122,
        374,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana prism", 0.01f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f)),
        false,
        0.0f,
        83,
        76,
        76,
        96,
        0,
        76,
        0,
        new int[] {99, 99, 99, 99, 99, 5000, 100, 100, 100, 100, 100, 100},
        68,
        282,
        0,
        1078001664,
        20016,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        36,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d106+81", 826, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 19, 10357, 1, 10),
            new MonsterDef.Attack("", 0, 40, 10120, 1, 10),
            new MonsterDef.Attack("", 0, 1, 10314, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
