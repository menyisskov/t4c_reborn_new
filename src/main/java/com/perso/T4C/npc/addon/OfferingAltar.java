package com.perso.T4C.npc.addon;

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

@Spawn(type = "OfferingAltar", x = 1095, y = 1504, z = 1, stationary = true, aggressive = false)
public final class OfferingAltar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "OfferingAltar";

  public static final String DISPLAY_NAME = "${npc.offeringaltar}";

  public static final String SPRITE_BASE = "@static:DungeonWell";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.offeringaltar}",
          List.of(),
          "WellNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("ADDON_STORYLINE_PROGRESS") == 17
            && c.hasItem("flask_of_gluriurl_blood")
            && c.hasItem("corrupt_heart")) {

          c.takeItem("flask_of_gluriurl_blood");

          c.takeItem("corrupt_heart");

          c.giveItem("heart_of_gluriurl");

          c.sayKey("npc.offeringaltar.complete");

        } else if (c.flag("ADDON_STORYLINE_PROGRESS") == 17 && c.hasItem("corrupt_heart"))
          c.sayKey("npc.offeringaltar.nothing");
        else c.sayKey("npc.offeringaltar.recess");
      }
    };
  }

  public OfferingAltar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
