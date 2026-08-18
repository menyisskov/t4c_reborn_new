package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ratsputin", x = 963, y = 581, z = 2, stationary = false, aggressive = true)
public final class r198Ratsputin extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public r198Ratsputin(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Ratsputin",
        "${monster.ratsputin}",
        847,
        0,
        9,
        4192,
        42,
        94,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        142,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of mana", 0.04f),
            new MonsterDef.LootDrop("Dead Fishes", 0.005f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {100, 50, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1076101120,
        20003,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d53+41", 540, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 90, 10096, 4, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
