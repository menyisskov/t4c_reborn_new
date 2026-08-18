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

public final class MetalChestNomad2 extends ScriptedNpc {

  public static final String ID = "MetalChestNomad2";

  public static final String DISPLAY_NAME = "${npc.metalchestnomad2}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.metalchestnomad2}",
          List.of(),
          "MetalChestNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.npc().setStationary(true);
      }

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.systemMessageKey(
            c.isInRange(4) ? "npc.metalchestnomad2.empty" : "npc.metalchestnomad2.too_far");

        c.endConversation();
      }
    };
  }

  public MetalChestNomad2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
