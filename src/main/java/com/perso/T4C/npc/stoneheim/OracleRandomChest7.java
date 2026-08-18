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
    type = "OracleRandomChest7",
    x = 2808,
    y = 2478,
    z = 2,
    stationary = true,
    aggressive = false)
public final class OracleRandomChest7 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OracleRandomChest7";

  public static final String DISPLAY_NAME = "${npc.oraclerandomchest7}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.oraclerandomchest7}",
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

        if (c.hasItem("dull_copper_key")) {

          c.systemMessageKey("message.oracle.chest.empty");

          return;
        }

        int r = c.globalFlag("ORACLE_RANDOM_CHEST");

        if (r == 0) r = 1 + (int) (Math.random() * 11);

        if (r == 1) {

          c.giveItem("dull_copper_key");

          c.systemMessageKey("message.oracle.random.key");

          c.globalFlag("ORACLE_RANDOM_CHEST", 1 + (int) (Math.random() * 11));

        } else {

          c.systemMessageKey("message.oracle.chest.empty");

          c.globalFlag("ORACLE_RANDOM_CHEST", r);
        }
      }
    };
  }

  public OracleRandomChest7(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
