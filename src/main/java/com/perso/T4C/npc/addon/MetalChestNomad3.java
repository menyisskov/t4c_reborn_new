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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "MetalChestNomad3", x = 2565, y = 1499, z = 0, stationary = true, aggressive = false)
public final class MetalChestNomad3 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MetalChestNomad3";

  public static final String DISPLAY_NAME = "${npc.metalchestnomad3}";

  public static final String SPRITE_BASE = "@static:Chest";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.metalchestnomad3}",
          List.of(),
          "MetalChestNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (!c.hasItem("small_key")) {

            c.sayKey("npc.chest.locked");

            return;
          }

          if (c.flag("ADDON_STORYLINE_PROGRESS") == 12
              && c.flag("ADDON_METALCHEST3_SEARCHED") == 0) {

            c.takeItem("small_key");

            c.giveItem("owain_letter_to_nomad");

            c.flag("ADDON_METALCHEST3_SEARCHED", 1);

            c.flag("ADDON_STORYLINE_PROGRESS", 13);

            c.sayKey("npc.chest.opened");

          } else c.sayKey("npc.chest.empty");
        }
      };

  public MetalChestNomad3(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
