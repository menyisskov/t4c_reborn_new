package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "The Dusk Regent", x = 5860, y = 2040, z = 0, stationary = false, aggressive = true)
public final class DuskRegent extends DataMonster {
  public static final String CANONICAL_NAME = "The Dusk Regent";

  public DuskRegent(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.create(
        CANONICAL_NAME,
        "dusk_regent",
        SirCaradocTheSunderedKnight.definition(),
        375,
        225000,
        26000000,
        1400,
        true)
        .withLoot(java.util.List.of(
            new MonsterDef.LootDrop("item.last_witness_seal", 1.0f),
            new MonsterDef.LootDrop("mana_elixir", .5f)));
  }
}
