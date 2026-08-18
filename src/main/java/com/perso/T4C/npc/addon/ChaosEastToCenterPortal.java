package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class ChaosEastToCenterPortal extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChaosEastToCenterPortal";

  public static final String DISPLAY_NAME = "${npc.chaoseasttocenterportal}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.chaoseasttocenterportal}",
          List.of(),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public ChaosEastToCenterPortal(NpcContext context) throws GameException {

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

          if (!c.isInRange(4)) {

            c.sayKey("npc.portal.too_far");

            c.endConversation();

            return;
          }

          if (c.flag("ADDON_TERROR_DEMON_KILLED") == 1
              && c.flag("ADDON_CHAOS_DEMON_KILLED") == 1
              && c.flag("ADDON_DARKNESS_DEMON_KILLED") == 1) c.teleport(1624, 1662, 1);
          else c.sayKey("npc.welcome.chaoseasttocenterportal");

          c.endConversation();
        }
      };
}
