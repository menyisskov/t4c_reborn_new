package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Moonwake Revenant", x = 3920, y = 735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3932, y = 735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3944, y = 735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3918, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3930, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3942, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3954, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3915, y = 765, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3930, y = 765, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3945, y = 765, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3960, y = 765, z = 0, stationary = false, aggressive = true)
public final class MoonwakeRevenant extends DataMonster {
  public static final String CANONICAL_NAME = "Moonwake Revenant";

  public MoonwakeRevenant(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.create(
        CANONICAL_NAME,
        "moonwake_revenant",
        VeilboundWraith.definition(),
        230,
        43000,
        175000,
        420,
        false);
  }
}
