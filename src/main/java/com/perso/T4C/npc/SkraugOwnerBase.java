package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import java.util.List;

public abstract class SkraugOwnerBase extends ScriptedNpc {

  protected SkraugOwnerBase(String id, NpcContext c) throws GameException {

    super(
        new NpcSpec(
            id,
            "${npc." + id.toLowerCase() + "}",
            "Skraug",
            List.of(),
            0,
            List.of(),
            null,
            List.of(),
            "Skraug",
            new NpcSpec.CombatProfile(100, 1000000, 100, 100, 100, 1000000, 0, 65535, "1d3")),
        c);
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(NpcBehaviorContext c) {
        c.shoutKey("npc.skraug.shout.attack." + (int) (Math.random() * 3));
      }

      @Override
      public void onAttacked(NpcBehaviorContext c) {
        c.shoutKey("npc.skraug.shout.hit." + (int) (Math.random() * 3));
      }

      @Override
      public void onDeath(NpcBehaviorContext c) {
        c.shoutKey("npc.skraug.shout.death." + (int) (Math.random() * 3));
      }
    };
  }
}
