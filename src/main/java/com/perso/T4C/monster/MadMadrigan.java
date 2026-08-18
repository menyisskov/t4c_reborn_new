package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class MadMadrigan extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public MadMadrigan(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onSpawn(Player player) {

    return selfSpell("spell.mob_invisibility_spell");
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player player) {

    return Math.random() < 1.0 / 30
        ? message(Math.random() < .5 ? "npc.madmadrigan.shout.0" : "npc.madmadrigan.shout.1")
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MADMADRIGAN",
        "${monster.madmadrigan}",
        710,
        0,
        22,
        7875,
        34,
        78,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        62,
        192,
        java.util.List.of(),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        46,
        46,
        22,
        new int[] {77, 77, 77, 77, 103, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1099431936,
        10011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
