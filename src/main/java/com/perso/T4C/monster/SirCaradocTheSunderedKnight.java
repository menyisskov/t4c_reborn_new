package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Boss of The Fading Veil's lair at (1440,1540), worldZ 0 — once a noble knight, now bound in
// service to Ysolde's corruption. Reuses the "MonsDraconianPlate" armored-knight animation family
// (Draconis Knight precedent), paired with undead sounds befitting his bound-in-undeath state.
// Guarded by three Sundered Sentinel adds from his own fallen retinue (see SunderedSentinel.java).
@Spawn(
    type = "Sir Caradoc, the Sundered Knight",
    x = 1440,
    y = 1540,
    z = 0,
    stationary = false,
    aggressive = true)
public final class SirCaradocTheSunderedKnight extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Sir Caradoc, the Sundered Knight";

  public SirCaradocTheSunderedKnight(MonsterDef definition, float x, float y)
      throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Sir Caradoc, the Sundered Knight",
        "${monster.sir_caradoc_the_sundered_knight}",
        62000,
        0,
        10,
        65000000,
        740,
        1680,
        30000L,
        "MonsDraconianPlate#k",
        "MonsDraconianPlateA#k",
        "MonsDraconianPlateC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1100,
        2750,
        java.util.List.of(
            new MonsterDef.LootDrop("caradocs_sundered_blade", 0.02f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.3f),
            new MonsterDef.LootDrop("mana_elixir", 0.2f)),
        false,
        0.0f,
        605,
        575,
        300,
        220,
        0,
        220,
        0,
        new int[] {80, 140, 110, 100, 200, 20, 100, 100, 100, 100, 100, 100},
        550,
        2200,
        0,
        550,
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
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d825+715", 6600, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Sir Caradoc, the Sundered Knight", "Sir Caradoc"),
        java.util.Map.of());
  }
}
