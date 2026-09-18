package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Activates a monster that was already authored in this codebase (real health 206,623, dodge
// 4010, and an attack list that already only casts one spell — matching real canon's "Fireball
// only, unlike its lesser kin" distinction) but had zero @Spawn points, exactly like
// LesserDrake/GreaterDrake before the previous content pass activated them. See
// docs/content-ideas/2026-09-canon-verified-additions.md for the "Drake's Lair" zone this
// activation is part of.
@Spawn(type = "Arch Drake", x = 2200, y = 2500, z = 0, stationary = false, aggressive = true)
public final class ArchDrake extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Arch Drake";

  public ArchDrake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Arch Drake",
        "${monster.arch_drake}",
        206623,
        0,
        0,
        // xpOnDeath: MonsterDef.xpOnDeath is `int`. XpCurveHardener's own formula would want
        // ~280B here (3-4 kills/level at level 1000) — that overflows `int` (max ~2.1B). Capped
        // just under Integer.MAX_VALUE instead of left at the legacy value's 0 (unreachable
        // before this activation, so its old xpOnDeath was never meaningful): still the single
        // biggest XP award in the game, not curve-accurate at the very top of a hard-capped
        // curve. See docs/content-ideas/2026-09-canon-verified-additions.md.
        2100000000,
        1353,
        3067,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1799,
        5500,
        java.util.List.of(new MonsterDef.LootDrop("archdrakes_molten_heart", 0.008f)),
        false,
        0.0f,
        1015,
        914,
        914,
        1215,
        0,
        914,
        0,
        new int[] {63, 5000, 63, -63, 63, 5000, 100, 100, 100, 100, 100, 100},
        1000,
        4010,
        0,
        1082081280,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d1715+1352", 12010, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10095, 2, 20)),
        false,
        0,
        java.util.List.of("ArchDrake"),
        java.util.Map.of());
  }
}
