package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Upper-tier trash of The Fading Veil (center 1420,1560 r130, worldZ 0) — animated plate armor
// from Sir Caradoc's fallen retinue, reusing the armored "SkeletonKing" animation/sound family
// (Undead Sentinel precedent). Three extra spawns stand near Caradoc's own lair (1440,1540) as
// his guard.
@Spawn(type = "Sundered Sentinel", x = 1510, y = 1630, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1330, y = 1500, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1470, y = 1675, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1370, y = 1445, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1545, y = 1540, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1295, y = 1575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1435, y = 1435, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1405, y = 1685, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1430, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1450, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sundered Sentinel", x = 1440, y = 1552, z = 0, stationary = false, aggressive = true)
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
        560,
        1400,
        java.util.List.of(
            new MonsterDef.LootDrop("serious_healing_potion", 0.1f),
            new MonsterDef.LootDrop("mana_elixir", 0.05f),
            new MonsterDef.LootDrop("healing_potion", 0.1f)),
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
