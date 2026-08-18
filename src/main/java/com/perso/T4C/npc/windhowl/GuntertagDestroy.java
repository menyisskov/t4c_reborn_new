package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class GuntertagDestroy extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String ID = "GuntertagDestroy";

  public static final String DISPLAY_NAME = "${npc.guntertagdestroy}";

  public static final String SPRITE_BASE = "Orc";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.guntertagdestroy}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guntertagdestroy.0.0}",
                      "${npc.topic_keyword.guntertagdestroy.0.1}",
                      "${npc.topic_keyword.guntertagdestroy.0.2}"),
                  "${npc.topic.guntertagdestroy.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guntertagdestroy.1.0}",
                      "${npc.topic_keyword.guntertagdestroy.1.1}"),
                  "${npc.topic.guntertagdestroy.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guntertagdestroy.2.0}"),
                  "${npc.topic.guntertagdestroy.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guntertagdestroy.3.0}",
                      "${npc.topic_keyword.guntertagdestroy.3.1}",
                      "${npc.topic_keyword.guntertagdestroy.3.2}"),
                  "${npc.topic.guntertagdestroy.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guntertagdestroy.4.0}"),
                  "${npc.topic.guntertagdestroy.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guntertagdestroy.5.0}",
                      "${npc.topic_keyword.guntertagdestroy.5.1}",
                      "${npc.topic_keyword.guntertagdestroy.5.2}",
                      "${npc.topic_keyword.guntertagdestroy.5.3}",
                      "${npc.topic_keyword.guntertagdestroy.5.4}"),
                  "${npc.topic.guntertagdestroy.5}",
                  List.of())),
          "GuntertagDestroyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 310, 65535, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_USER_IS_A_TRAITOR") >= 3) {

          c.sayKey("npc.guntertag.traitor");

          c.npc().provoke();

        } else c.sayKey("npc.guntertag.welcome");
      }
    };
  }

  public GuntertagDestroy(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
