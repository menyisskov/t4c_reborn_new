package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

// Upper-tier trash of The Fading Veil (center 4120,1560 r130, worldZ 0) — animated plate armor
// from Sir Caradoc's fallen retinue, reusing the armored "SkeletonKing" animation/sound family
// (Undead Sentinel precedent). Three extra spawns stand near Caradoc's border glade (3965,1460) as
// his guard.
@Spawn(type = "Sundered Sentinel", x = 4374, y = 1789, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4364, y = 1808, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4150, y = 1798, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4087, y = 1811, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4157, y = 1678, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4476, y = 1733, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4530, y = 1975, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4248, y = 1972, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4223, y = 2052, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4504, y = 1931, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4330, y = 1908, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4499, y = 1898, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4276, y = 1925, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4366, y = 1905, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4456, y = 1869, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4498, y = 2012, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4439, y = 1940, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4260, y = 1761, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4154, y = 2018, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4354, y = 1835, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4124, y = 1885, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4378, y = 1763, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4171, y = 1735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4411, y = 1757, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4302, y = 1780, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4140, y = 1812, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4439, y = 1848, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4578, y = 1854, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4231, y = 1918, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4500, y = 1779, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4188, y = 1866, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4387, y = 1818, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4212, y = 1857, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4226, y = 1945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4259, y = 1958, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4229, y = 2074, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4319, y = 2014, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4417, y = 2039, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4391, y = 2036, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4404, y = 2018, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
public final class SunderedSentinel extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Sundered Sentinel";

  public SunderedSentinel(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Sundered Sentinel",
        "${monster.sundered_sentinel}",
        61600,
        0,
        9,
        260000,
        300,
        640,
        30000L,
        "64kSkeletonKing#m",
        "64kSkeletonKingA#k",
        "64kSkeletonKingC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        MonsterGoldCurve.goldMin(275),
        MonsterGoldCurve.goldMax(275),
        java.util.List.of(
            ),
        false,
        0.0f,
        560,
        560,
        280,
        170,
        0,
        170,
        0,
        new int[] {70, 130, 100, 90, 170, 30, 100, 100, 100, 100, 100, 100},
        275,
        1300,
        0,
        500,
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
        java.util.List.of(new MonsterDef.Attack("1d840+728", 6720, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Sundered Sentinel"),
        java.util.Map.of());
  }
}
