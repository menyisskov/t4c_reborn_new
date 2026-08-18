package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;

public final class GiantBlackWidow extends NamedEventMonster {

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
}
