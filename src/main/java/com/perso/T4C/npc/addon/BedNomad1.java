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

public final class BedNomad1 extends ScriptedNpc {

  public static final String ID = "BedNomad1";

  public static final String DISPLAY_NAME = "${npc.bednomad1}";

  public static final String SPRITE_BASE = "@static:RockDoor1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.bednomad1}",
          List.of(),
          "BedNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("ADDON_BED1_SEARCHED") == 0) {

            c.flag("ADDON_BED1_SEARCHED", 1);

            c.player().addGold(1200);

            c.sayKey("npc.bed.found");

          } else c.sayKey("npc.bed.empty");
        }
      };

  public BedNomad1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
