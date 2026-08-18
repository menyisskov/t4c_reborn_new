package com.perso.T4C.npc.remort;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class ExitGate extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ExitGate";

  public static final String DISPLAY_NAME = "${npc.exitgate}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.exitgate}",
          List.of(),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ExitGate(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          int x = c.viewFlag("ARENA_RETURN_X"),
              y = c.viewFlag("ARENA_RETURN_Y"),
              w = c.viewFlag("ARENA_RETURN_WORLD");

          if (x != 0 || y != 0 || w != 0) c.teleport(x, y, w);
          else {

            c.systemMessageKey("npc.exitgate.error");

            c.teleport(2941, 1062, 0);
          }

          c.endConversation();
        }
      };
}
