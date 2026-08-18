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

public final class MetalChestNomad1 extends ScriptedNpc {

  public static final String ID = "MetalChestNomad1";

  public static final String DISPLAY_NAME = "${npc.metalchestnomad1}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.metalchestnomad1}",
          List.of(),
          "MetalChestNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("ADDON_STORYLINE_PROGRESS") == 12 && c.flag("ADDON_TABLE1_SEARCHED") == 0) {

          c.flag("ADDON_TABLE1_SEARCHED", 1);

          c.sayKey("npc.metalchestnomad1.key");

          c.giveItem("small_key");

        } else if (c.flag("ADDON_STORYLINE_PROGRESS") == 12
            && c.flag("ADDON_TABLE1_SEARCHED") == 1) {

          c.flag("ADDON_TABLE1_SEARCHED", 2);

          c.sayKey("npc.metalchestnomad1.gold");

          c.player().addGold(1000);

        } else c.sayKey("npc.metalchest.empty");
      }
    };
  }

  public MetalChestNomad1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
