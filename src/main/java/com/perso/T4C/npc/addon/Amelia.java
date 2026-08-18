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
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Amelia extends ScriptedNpc {

  public static final String ID = "Amelia";

  public static final String DISPLAY_NAME = "${npc.amelia}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.amelia}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amelia.0.0}"), "${npc.topic.amelia.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amelia.1.0}"), "${npc.topic.amelia.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amelia.2.0}"), "${npc.topic.amelia.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amelia.3.0}"), "${npc.topic.amelia.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amelia.4.0}"), "${npc.topic.amelia.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.amelia.5.0}"), "${npc.topic.amelia.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.amelia.6.0}",
                      "${npc.topic_keyword.amelia.6.1}",
                      "${npc.topic_keyword.amelia.6.2}",
                      "${npc.topic_keyword.amelia.6.3}",
                      "${npc.topic_keyword.amelia.6.4}"),
                  "${npc.topic.amelia.6}",
                  List.of())),
          "GypsyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 21) c.sayKey("npc.welcome.amelia");
          else if (p == 21) c.sayKey("npc.amelia.progress.21");
          else if (p == 22) c.sayKey("npc.amelia.progress.22");
          else if (p < 42) c.sayKey("npc.amelia.progress.before42");
          else c.sayKey("npc.amelia.progress.complete");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("WHERE")) {

            if (p == 21) {

              c.flag("ADDON_STORYLINE_PROGRESS", 22);

              c.sayKey("npc.topic.amelia.3");

            } else c.sayKey("npc.amelia.unavailable");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.topic.amelia.4");

            return true;
          }

          if (k.equals("WORK")) {

            c.sayKey("npc.topic.amelia.5");

            return true;
          }

          if (k.equals("KILL")) {

            c.sayKey(p == 21 ? "npc.topic.amelia.0" : "npc.amelia.unavailable");

            return true;
          }

          if (k.equals("THEM")) {

            c.sayKey(p == 21 ? "npc.topic.amelia.1" : "npc.amelia.unavailable");

            return true;
          }

          if (k.equals("BRIGAND")) {

            c.sayKey((p == 21 || p == 22) ? "npc.topic.amelia.2" : "npc.amelia.unavailable");

            return true;
          }

          return false;
        }
      };

  public Amelia(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
