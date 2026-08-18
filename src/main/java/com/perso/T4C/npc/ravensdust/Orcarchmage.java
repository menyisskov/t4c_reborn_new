package com.perso.T4C.npc.ravensdust;

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

@Spawn(type = "Orcarchmage", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class Orcarchmage extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String ID = "Orcarchmage";

  public static final String DISPLAY_NAME = "${npc.orcarchmage}";

  public static final String SPRITE_BASE = "Orc";

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
          "OrcArchMageNPC",
          new NpcSpec.CombatProfile(42, 1812, 57, 52, 52, 21, 514, 178, "1d58+44)"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.orcarchmage.death");

        c.flag("QUEST_KILLED_ORC_MAGUS", 1);

        c.globalFlag("GLOBAL_FLAG_ORC_QUEST", 0);
      }
    };
  }

  public Orcarchmage(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
