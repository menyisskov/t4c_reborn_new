package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;

public final class GiantBlackWidow extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public GiantBlackWidow(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return hatch();
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    return hatch();
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    List<String> summons = new ArrayList<>();

    int[][] offsets = {{-1, -1}, {0, -1}, {-1, 0}, {1, 0}, {0, 1}};

    for (int[] o : offsets)
      summons.add(
          "WIDOWHATCHLING@" + (int) getPosition().x + o[0] + "," + (int) getPosition().y + o[1]);

    return MonsterScriptBridge.Effects.empty();
  }

  private MonsterScriptBridge.Effects hatch() {

    return MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
         "MOBGIANTBLACKWIDOW",
        "${monster.mobgiantblackwidow}",
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
