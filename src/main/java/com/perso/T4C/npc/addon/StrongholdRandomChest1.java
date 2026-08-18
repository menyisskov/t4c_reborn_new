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

public final class StrongholdRandomChest1 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "StrongholdRandomChest1";

  public static final String DISPLAY_NAME = "${npc.strongholdrandomchest1}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.strongholdrandomchest1}",
          List.of(),
          "WoodenChestNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.hasItem("violet_crystal_shard")) {

          c.sayKey("npc.randomchest.empty");

          return;
        }

        int state = c.flag("ADDON_RANDOM_CHEST");

        if (state == 0) {

          state = 1 + (int) (Math.random() * 7);

          c.flag("ADDON_RANDOM_CHEST", state);
        }

        if (state == 1) {

          c.giveItem("violet_crystal_shard");

          c.sayKey("npc.randomchest.found");

          c.flag("ADDON_RANDOM_CHEST", 1 + (int) (Math.random() * 7));

        } else c.sayKey("npc.randomchest.empty");
      }
    };
  }

  public StrongholdRandomChest1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
