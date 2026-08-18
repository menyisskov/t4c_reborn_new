package com.perso.T4C.npc.addon;

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

public final class CrystalPortal extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CrystalPortal";

  public static final String DISPLAY_NAME = "${npc.crystalportal}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.crystalportal}",
          List.of(),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private final java.util.List<String> crystals =
            java.util.List.of(
                "crystal_kervian", "crystal_vharmes", "crystal_asgoth", "crystal_ramiel");

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("__FLAG_ADDON_CRYSTAL_KEY") == 1) {

            c.teleport(937, 2318, 1);

            c.endConversation();

            return;
          }

          if (crystals.stream().allMatch(c::hasItem)) {

            crystals.forEach(c::takeItem);

            c.flag("__FLAG_ADDON_CRYSTAL_KEY", 1);

            c.systemMessageKey("npc.crystalportal.opened");

            c.teleport(937, 2318, 1);

          } else {

            c.systemMessageKey("npc.crystalportal.depleted");

            c.endConversation();
          }
        }
      };

  public CrystalPortal(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
