package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Rhunor, the Hollow Dawn",
    x = 5650,
    y = 2290,
    z = 0,
    stationary = false,
    aggressive = true)
public final class RhunorTheHollowDawn extends DataMonster {
  public static final String CANONICAL_NAME = "Rhunor, the Hollow Dawn";

  public RhunorTheHollowDawn(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.withWitnessLoot(
        EndgameMonsterFactory.create(
            CANONICAL_NAME,
            "rhunor_the_hollow_dawn",
            TheVerdantWarden.definition(),
            400,
            480000,
            50000000,
            2100,
            true),
        true,
        "witness_dark_plate",
        "witness_dark_robe",
        "witness_dark_wings",
        "witness_dark_dagger",
        "witness_dark_rod");
  }
}
