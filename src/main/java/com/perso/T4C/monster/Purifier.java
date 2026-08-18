package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBPURIFIER", x = 1846, y = 2869, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1881, y = 2862, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1919, y = 2939, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1920, y = 2745, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1929, y = 2817, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1948, y = 2787, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1954, y = 2844, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 1989, y = 2811, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBPURIFIER", x = 2031, y = 2732, z = 0, stationary = false, aggressive = false)
public final class Purifier extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public Purifier(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < 1.0 / 11
        ? message("npc.purifier.shout")
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBPURIFIER",
        "${monster.mobpurifier}",
        100,
        0,
        1,
        100,
        1,
        2,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
