package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Emberglass Ashguard",
    x = 4340,
    y = 2600,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4352,
    y = 2600,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4364,
    y = 2600,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4335,
    y = 2620,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4350,
    y = 2620,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4365,
    y = 2620,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4380,
    y = 2620,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4340,
    y = 2640,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4355,
    y = 2640,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Emberglass Ashguard",
    x = 4370,
    y = 2640,
    z = 0,
    stationary = false,
    aggressive = true)
public final class EmberglassAshguard extends DataMonster {
  public static final String CANONICAL_NAME = "Emberglass Ashguard";

  public EmberglassAshguard(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.create(
        CANONICAL_NAME,
        "emberglass_ashguard",
        SunderedSentinel.definition(),
        270,
        61000,
        245000,
        550,
        false);
  }
}
