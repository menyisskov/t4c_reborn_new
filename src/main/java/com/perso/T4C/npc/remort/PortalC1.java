package com.perso.T4C.npc.remort;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class PortalC1 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "PortalC1";

  public static final String DISPLAY_NAME = "${npc.portalc1}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.portalc1}",
          List.of(),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1_000_000, 500, 500, 500, 1_000_000, 1, 65_535, "1d3"));

  public PortalC1(NpcContext context) throws GameException {

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
        public void onInitialise(NpcBehaviorContext context) {

          context.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext context) {

          if (!context.hasItem("c1_permit")) {

            context.systemMessage("${npc.welcome.portalc1}");

            context.endConversation();

            return;
          }

          context.teleport(2722, 2378, 0);

          context.endConversation();
        }
      };
}
