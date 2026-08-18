package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class OracleQuickChest2a extends ScriptedNpc {

  public static final String ID = "OracleQuickChest2a";

  public static final String DISPLAY_NAME = "${npc.oraclequickchest2a}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.oraclequickchest2a}",
          List.of(),
          "OracleChestNPC",
          new NpcSpec.CombatProfile(100, 1000000, 10, 10, 10, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (!c.isInRange(4)) {

          c.systemMessageKey("message.oracle.chest.close");

          return;
        }

        int s = c.flag("CHAMBER_OF_QUICKNESS_ACTIVATED");

        if (s == 0) {

          c.systemMessageKey("message.oracle.quick.key");

          c.giveItem("enchanted_glass_key");

          c.flag("CHAMBER_OF_QUICKNESS_ACTIVATED", 1);

          c.castTargetSpell(10434);

        } else if (s == 1) c.systemMessageKey("message.oracle.chest.empty");
        else if (s == 2) {

          c.systemMessageKey("message.oracle.quick.key");

          c.systemMessageKey("message.oracle.quick.key_disappears");

          c.flag("CHAMBER_OF_QUICKNESS_ACTIVATED", 1);

          c.castTargetSpell(10434);
        }
      }
    };
  }

  public OracleQuickChest2a(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
