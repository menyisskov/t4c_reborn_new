package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "NexusStone2", x = 2195, y = 751, z = 0, stationary = true, aggressive = false)
public final class NexusStone2 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "NexusStone2";

  public static final String DISPLAY_NAME = "${npc.nexusstone2}";

  public static final String SPRITE_BASE = "@static:Horloge Solaire";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.nexusstone2}",
          List.of(),
          "NexusStoneNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("ADDON_STORYLINE_PROGRESS") < 27) {

            c.sayKey("npc.nexus.ancient");

            return;
          }

          if (!c.hasItem("runed_stone_tablet")) {

            c.sayKey("npc.nexus.noTablet");

            return;
          }

          if (c.flag("ADDON_NEXUS_2_ACTIVATED") == 0) {

            c.takeItem("runed_stone_tablet");

            c.flag("ADDON_NEXUS_2_ACTIVATED", 1);

            c.flag("ADDON_NEXUS_STONES_ACTIVATED", c.flag("ADDON_NEXUS_STONES_ACTIVATED") + 1);

            c.sayKey(
                c.flag("ADDON_NEXUS_STONES_ACTIVATED") == 5
                    ? "npc.nexus.last"
                    : "npc.nexus.activated");

            c.giveItem("runed_stone_tablet");

          } else c.sayKey("npc.nexus.done");
        }
      };

  public NexusStone2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
