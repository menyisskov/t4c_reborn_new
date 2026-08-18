package com.perso.T4C.npc.oracle;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public abstract class OracleGuardianBase extends ScriptedNpc {

  private final int variant;

  protected OracleGuardianBase(String id, int variant, NpcContext context) throws GameException {

    super(spec(id), context);

    this.variant = variant;
  }

  public static NpcSpec spec(String id) {

    return new NpcSpec(
        id,
        "${npc." + id.toLowerCase() + "}",
        "OracleGuardian",
        List.of(),
        0,
        List.of(),
        null,
        List.of(),
        "OracleGuardian",
        new NpcSpec.CombatProfile(100, 1000000, 100, 100, 100, 1000000, 0, 65535, "1d3"));
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onDeath(NpcBehaviorContext c) {

        int n = c.itemCount("oddly_shaped_shard_of_stone");

        int target = variant;

        if (n == target) {

          c.giveItem("oddly_shaped_shard_of_stone");

          c.systemMessageKey("message.oracle.shard.pick");

        } else if (n > target) {

          for (int i = 0; i < n; i++) c.takeItem("oddly_shaped_shard_of_stone");

        } else c.systemMessageKey("message.oracle.shard.crumble");
      }
    };
  }
}
