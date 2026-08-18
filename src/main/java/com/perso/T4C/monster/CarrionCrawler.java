package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

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
        "SmallWormC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
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
