package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Boss of The Fading Veil's lair at (1400,1580), worldZ 0 — the corruption's source. A robed,
// caster-leaning matriarch: reuses the plain Skeleton animation family (fits her gaunt, veiled
// silhouette) with two ranged spell-attacks alongside a weaker melee strike, mirroring Greater
// Drake's melee+spell attack shape. Guarded by three Veilbound Wraith adds (see
// VeilboundWraith.java).
@Spawn(
    type = "Ysolde, the Veiled Matriarch",
    x = 1400,
    y = 1580,
    z = 0,
    stationary = false,
    aggressive = true)
public final class YsoldeTheVeiledMatriarch extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Ysolde, the Veiled Matriarch";

  public YsoldeTheVeiledMatriarch(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ysolde, the Veiled Matriarch",
        "${monster.ysolde_the_veiled_matriarch}",
        64000,
        15000,
        10,
        88000000,
        600,
        1300,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1200,
        3000,
        java.util.List.of(
            new MonsterDef.LootDrop("ysoldes_veiled_circlet", 0.02f),
            new MonsterDef.LootDrop("mana_elixir", 0.3f),
            new MonsterDef.LootDrop("potion_of_mana", 0.3f)),
        false,
        0.0f,
        300,
        480,
        420,
        840,
        0,
        840,
        0,
        new int[] {100, 90, 90, 120, 250, 10, 100, 100, 100, 100, 100, 100},
        600,
        1800,
        0,
        180,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        4,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d420+400", 5000, 40, 0, 0, 1),
            new MonsterDef.Attack("", 0, 35, 10094, 2, 15),
            new MonsterDef.Attack("", 0, 25, 10352, 2, 15)),
        false,
        0,
        java.util.List.of("Ysolde, the Veiled Matriarch", "Ysolde"),
        java.util.Map.of());
  }
}
