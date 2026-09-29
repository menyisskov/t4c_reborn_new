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
@Spawn(type = "Sundered Sentinel", x = 4210, y = 1630, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4130, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4170, y = 1675, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4150, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4245, y = 1540, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 3995, y = 1575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4135, y = 1435, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4105, y = 1685, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4185, y = 1451, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4147, y = 1485, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4140, y = 1552, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4221, y = 1549, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4046, y = 1558, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4211, y = 1572, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4045, y = 1590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4160, y = 1598, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4039, y = 1636, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4100, y = 1641, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4118, y = 1659, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 4140, y = 1672, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 3955, y = 1455, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 3975, y = 1455, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 3965, y = 1472, z = 0, stationary = false, aggressive = true)
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
        840000,
        300,
        640,
        30000L,
        "64kSkeletonKing#m",
        "64kSkeletonKingA#k",
        "64kSkeletonKingC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        MonsterGoldCurve.goldMin(560),
        MonsterGoldCurve.goldMax(560),
        java.util.List.of(
            new MonsterDef.LootDrop("serious_healing_potion", 0.1f),
            new MonsterDef.LootDrop("mana_elixir", 0.05f)),
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
        560,
        2240,
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
