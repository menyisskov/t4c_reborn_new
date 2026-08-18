package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

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
