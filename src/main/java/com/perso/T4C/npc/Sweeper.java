package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Sweeper", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class Sweeper extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String ID = "Sweeper";

  public static final String DISPLAY_NAME = "${npc.sweeper}";

  public static final String SPRITE_BASE = "Rat";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "",
          List.of(),
          "SweeperNPC",
          new NpcSpec.CombatProfile(1, 27, 16, 15, 15, 0, 22, 14, "1d4+1"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onPopup(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.globalFlag("TOTAL_SWEEPERS_PRESENT", c.globalFlag("TOTAL_SWEEPERS_PRESENT") + 1);
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int present = c.globalFlag("TOTAL_SWEEPERS_PRESENT");

        if (present > 0) c.globalFlag("TOTAL_SWEEPERS_PRESENT", present - 1);

        c.flag("SWEEPERS_KILLED_BY_USER", c.flag("SWEEPERS_KILLED_BY_USER") + 1);

        c.giveItem("arcane_scroll");

        c.systemMessageKey("npc.sweeper.scroll_reward");
      }
    };
  }

  public Sweeper(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
