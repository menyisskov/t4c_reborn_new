package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "OracleQuickChest2b",
    x = 2760,
    y = 2264,
    z = 2,
    stationary = true,
    aggressive = false)
public final class OracleQuickChest2b extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleQuickChest2b";

  public static final String DISPLAY_NAME = "${npc.oraclequickchest2b}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.oraclequickchest2b}",
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

        if (s != 0) {

          if (c.hasItem("enchanted_glass_key")) {

            c.systemMessageKey("message.oracle.quick.unlock");

            c.systemMessageKey("message.oracle.quick.pulsating");

            c.takeItem("enchanted_glass_key");

            c.giveItem("pulsating_key");

            c.flag("CHAMBER_OF_QUICKNESS_ACTIVATED", 0);

            c.castTargetSpell(10435);

          } else {

            c.systemMessageKey("message.oracle.quick.fits");

            c.systemMessageKey("message.oracle.quick.prevents");
          }

        } else c.systemMessageKey("message.oracle.chest.locked");
      }
    };
  }

  public OracleQuickChest2b(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
