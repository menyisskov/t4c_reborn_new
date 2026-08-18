package com.perso.T4C.npc.skraug;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.script.ScriptedNpc;
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

      private final String[] a = {"Yaaaaagh! Yew DIE!", "Yew die now!", "KILL! KILL! OY KILL YEW!"};

      private final String[] h = {
        "Dis not hurt me!", "Oy not feel pain!", "Izzat all yew can dew?"
      };

      private final String[] d = {
        "Og! Dat wuz fun! Oy dye guud death!", "Oy is dead!", "Agh! Oy die merry and bloody!"
      };

      public void onAttack(NpcBehaviorContext c) {

        c.shout(a[(int) (Math.random() * a.length)]);
      }

      public void onAttacked(NpcBehaviorContext c) {

        c.shout(h[(int) (Math.random() * h.length)]);
      }

      public void onDeath(NpcBehaviorContext c) {

        c.shout(d[(int) (Math.random() * d.length)]);
      }
    };
  }
}
