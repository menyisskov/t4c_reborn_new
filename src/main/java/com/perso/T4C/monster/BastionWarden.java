package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bastion Warden", x = 2620, y = 2850, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bastion Warden", x = 2680, y = 2910, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bastion Warden", x = 2610, y = 2920, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bastion Warden", x = 2690, y = 2840, z = 0, stationary = false, aggressive = true)
public final class BastionWarden extends DataMonster {
  // Reuses the "Green Troll" animation/sound family already used by Grott/Toll Troll — a
  // hulking guardian, distinct from the Agmorkian drake-kin sprite reserved for the Drakes.
  public static final String SOUND_ATTACK = "Troll Attack.wav";
  public static final String SOUND_DEATH = "Troll Dying.wav";
  public static final String SOUND_HIT = "Troll Hit.wav";

  public static final String CANONICAL_NAME = "Bastion Warden";

  public BastionWarden(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bastion Warden",
        "${monster.bastion_warden}",
        51800,
        0,
        11,
        4750000,
        590,
        1000,
        30000L,
        "GreenTroll#f",
        "GreenTrollA#g",
        "GreenTrollC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        600,
        1750,
        java.util.List.of(new MonsterDef.LootDrop("bastion_sentinels_mantle", 0.03f)),
        false,
        0.0f,
        300,
        290,
        235,
        70,
        0,
        175,
        0,
        new int[] {90, 140, 100, 120, 100, 100, 100, 100, 100, 100, 100, 100},
        430,
        741,
        0,
        280,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        85,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d596+515", 3462, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("BastionWarden"),
        java.util.Map.of());
  }
}
