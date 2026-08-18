package com.perso.T4C.npc.arakas;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class DorkenRotsmell extends ScriptedNpc {

  public static final String ID = "DorkenRotsmell";

  public static final String DISPLAY_NAME = "${npc.dorkenrotsmell}";

  public static final String SPRITE_BASE = "GoblinBoss";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.dorkenrotsmell}",
          List.of(),
          "_DorkenRotsmell",
          new NpcSpec.CombatProfile(15, 508, 32, 28, 28, 7, 190, 70, "1d17+12"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.globalFlag("GLOBAL_FLAG_GOBLIN_QUEST", 0);

        c.globalFlag(
            "GLOBAL_QUEST_WHO_IS_INFECTED",
            1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(3));
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public DorkenRotsmell(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
