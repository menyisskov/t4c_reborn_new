package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class DoktorSpine extends ScriptedNpc {

  public static final String ID = "DoktorSpine";

  public DoktorSpine(NpcContext c) throws GameException {

    super(
        new NpcSpec(
            ID,
            "${npc.doktorspine}",
            "DoktorSpine",
            List.of(),
            0,
            List.of(),
            null,
            List.of(),
            "DoktorSpine",
            new NpcSpec.CombatProfile(100, 1000000, 100, 100, 100, 1000000, 0, 65535, "1d3")),
        c);
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      public void onAttack(NpcBehaviorContext c) {

        if ((int) (Math.random() * 40) == 0) c.shoutKey("npc.doktorspine.shout");
      }
    };
  }
}
