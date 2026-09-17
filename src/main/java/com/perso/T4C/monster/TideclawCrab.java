package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Tideclaw Crab", x = 1780, y = 2350, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideclaw Crab", x = 1820, y = 2310, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideclaw Crab", x = 1730, y = 2360, z = 0, stationary = false, aggressive = true)
public final class TideclawCrab extends DataMonster {
  // Reuses the Scorpion animation/sound family as the closest existing chitinous,
  // multi-legged creature — no dedicated crab sprite exists in this checkout.
  public static final String SOUND_ATTACK = "Elemear Attack.wav";
  public static final String SOUND_DEATH = "Scorpion Dying.wav";
  public static final String SOUND_HIT = "Scorpion Hit.wav";

  public static final String CANONICAL_NAME = "Tideclaw Crab";

  public TideclawCrab(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Tideclaw Crab",
        "${monster.tideclaw_crab}",
        1180,
        0,
        6,
        4700,
        40,
        75,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        75,
        230,
        java.util.List.of(new MonsterDef.LootDrop("tideclaw_band", 0.02f)),
        false,
        0.0f,
        54,
        54,
        54,
        64,
        0,
        54,
        0,
        new int[] {70, 130, 150, 80, 100, 100, 100, 100, 100, 100, 100, 100},
        42,
        175,
        0,
        30,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        65,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d60+44", 400, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("TideclawCrab"),
        java.util.Map.of());
  }
}
