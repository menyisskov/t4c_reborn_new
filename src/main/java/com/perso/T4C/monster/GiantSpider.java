package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Giant Spider", x = 281, y = 72, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Giant Spider", x = 309, y = 66, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Giant Spider", x = 325, y = 79, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Giant Spider", x = 330, y = 60, z = 1, stationary = false, aggressive = true)
public final class GiantSpider extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public static final String CANONICAL_NAME = "Giant Spider";

  public GiantSpider(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Giant Spider",
        "${monster.giant_spider}",
        69,
        0,
        1,
        60,
        4,
        10,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        7,
        22,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f)),
        false,
        0.0f,
        19,
        18,
        18,
        19,
        0,
        18,
        0,
        new int[] {122, 61, 92, 92, 92, 5000, 100, 100, 100, 100, 100, 100},
        4,
        26,
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
        10,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d7+3", 68, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
