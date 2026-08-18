package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class RavingLunatic extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public RavingLunatic(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < .02
        ? message("npc.ravinglunatic.shout." + (int) (Math.random() * 3))
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Raving Lunatic",
        "${monster.raving_lunatic}",
        0,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(
            new MonsterDef.LootDrop("G4 Permit", 0.4f), new MonsterDef.LootDrop("I4 Permit", 0.2f)),
        false,
        0.0f,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
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
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
