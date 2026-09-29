package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "The Pale Cantor", x = 4050, y = 675, z = 0, stationary = false, aggressive = true)
public final class PaleCantor extends DataMonster {
  public static final String CANONICAL_NAME = "The Pale Cantor";

  public PaleCantor(MonsterDef definition, float x, float y) throws GameException {
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
                "pale_cantor",
                YsoldeTheVeiledMatriarch.definition(),
                245,
                85000,
                5500000,
                700,
                true)
            .withLoot(
                java.util.List.of(
                    new MonsterDef.LootDrop("item.moonwake_bell_shard", 1.0f),
                    new MonsterDef.LootDrop("serious_healing_potion", .5f))),
        true,
        "witness_fire_plate",
        "witness_fire_robe",
        "witness_fire_wings",
        "witness_fire_brand",
        "witness_fire_mace",
        "witness_water_plate",
        "witness_water_robe",
        "witness_water_wings",
        "witness_water_sceptre",
        "witness_water_staff");
  }
}
