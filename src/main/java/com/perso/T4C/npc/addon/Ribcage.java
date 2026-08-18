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

public final class Ribcage extends ScriptedNpc {

  public static final String ID = "Ribcage";

  public static final String DISPLAY_NAME = "${npc.ribcage}";

  public static final String SPRITE_BASE = "@static:Object_Ribcage";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.ribcage}",
          List.of(),
          "RibcageNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (!c.isInRange(4)) {

          c.systemMessageKey("npc.ribcage.too_far");

          return;
        }

        int p = c.flag("__FLAG_ADDON_STORYLINE_PROGRESS");

        if (p == 17 && c.hasItem("heart_of_gluriurl")) {

          c.systemMessageKey("npc.ribcage.darkstone");

          c.systemMessageKey("npc.ribcage.replace");

          c.giveXp(125000);

          c.takeItem("heart_of_gluriurl");

          c.giveItem("darkstone");

          c.flag("__FLAG_ADDON_STORYLINE_PROGRESS", 18);

          c.systemMessageKey("message.gluriurl.gypsy");

        } else if (p >= 18) c.systemMessageKey("npc.ribcage.heart");
        else c.systemMessageKey("npc.ribcage.darkstone");
      }
    };
  }

  public Ribcage(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
