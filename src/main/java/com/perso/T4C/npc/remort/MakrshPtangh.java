package com.perso.T4C.npc.remort;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.*;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class MakrshPtangh extends ScriptedNpc {

  public static final String ID = "MakrshPtangh";

  public static final String DISPLAY_NAME = "${npc.makrshptangh}";

  public static final String SPRITE_BASE = "64kLich";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.makrshptangh}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.makrshptangh.0.0}",
                      "${npc.topic_keyword.makrshptangh.0.1}"),
                  null,
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.makrshptangh.1.0}",
                      "${npc.topic_keyword.makrshptangh.1.1}"),
                  null,
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.makrshptangh.2.0}",
                      "${npc.topic_keyword.makrshptangh.2.1}",
                      "${npc.topic_keyword.makrshptangh.2.2}"),
                  null,
                  List.of())),
          "MakrshPtanghNPC",
          new NpcSpec.CombatProfile(200, 1000000, 215, 194, 194, 1000000, 0, 65535, "1d3"));

  public MakrshPtangh(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  public static final class SpawnerBehavior implements NpcBehavior {

    @Override
    public void onPopup(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

      if (c.globalFlag("MAKRSH_PTANGH_IS_FIGHTING") != 1) return;

      int deadTimer = c.globalFlag("MAKRSH_PTANGH_DEAD_TIMER");

      int now = (int) (System.currentTimeMillis() / 1000L);

      if (now < deadTimer || now >= deadTimer + 24000) {

        c.globalFlag("MAKRSH_PTANGH_IS_FIGHTING", 2);

        c.summon("MAKRSHPTANGH", 2265, 295, 0);
      }
    }
  }

  @Override
  protected boolean canTalkThroughWalls() {

    return true;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onInitialise(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.npc().setStationary(true);

        c.npc().setStationaryAnimation(false);
      }

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey("npc.welcome.makrshptangh");

        c.summon("MakrshPtangh2", 2265, 145, 1);

        c.globalFlag("__GLOBAL_FLAG_MAKRSH_PTANGH_IS_FIGHTING", 0);

        c.castSelfSpell(10707);

        c.teleport(2255, 135, 1);

        c.selfDestructNpc();
      }
    };
  }
}
