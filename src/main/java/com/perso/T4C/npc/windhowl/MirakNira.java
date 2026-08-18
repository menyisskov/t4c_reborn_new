package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MirakNira extends ScriptedNpc {

  public static final String ID = "MirakNira";

  public static final String DISPLAY_NAME = "${npc.miraknira}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.miraknira}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miraknira.0.0}", "${npc.topic_keyword.miraknira.0.1}"),
                  "${npc.topic.miraknira.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miraknira.1.0}",
                      "${npc.topic_keyword.miraknira.1.1}",
                      "${npc.topic_keyword.miraknira.1.2}"),
                  "${npc.topic.miraknira.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.2.0}"),
                  "${npc.topic.miraknira.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miraknira.3.0}",
                      "${npc.topic_keyword.miraknira.3.1}",
                      "${npc.topic_keyword.miraknira.3.2}"),
                  "${npc.topic.miraknira.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.4.0}"),
                  "${npc.topic.miraknira.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.5.0}"),
                  "${npc.topic.miraknira.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miraknira.6.0}", "${npc.topic_keyword.miraknira.6.1}"),
                  "${npc.topic.miraknira.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.7.0}"),
                  "${npc.topic.miraknira.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.8.0}"),
                  "${npc.topic.miraknira.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.9.0}"),
                  "${npc.topic.miraknira.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miraknira.10.0}"),
                  "${npc.topic.miraknira.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miraknira.11.0}",
                      "${npc.topic_keyword.miraknira.11.1}",
                      "${npc.topic_keyword.miraknira.11.2}",
                      "${npc.topic_keyword.miraknira.11.3}",
                      "${npc.topic_keyword.miraknira.11.4}"),
                  "${npc.topic.miraknira.11}",
                  List.of())),
          "MirakNiraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("OLD HERMIT")) {

          if (c.flag("__FLAG_TALKED_TO_HERMIT") == 0) c.sayKey("npc.mirak.hermit.unknown");
          else if (c.flag("__FLAG_DAUGHTER_OF_THE_HERMIT") == 1)
            c.sayKey("npc.mirak.hermit.father");
          else if (c.flag("__FLAG_HERMIT_COUNTER") == 1) {

            c.sayKey("npc.mirak.hermit.admit");

            c.flag("__FLAG_HERMIT_COUNTER", 2);

          } else if (c.flag("__FLAG_HERMIT_COUNTER") == 2) {

            c.sayKey("npc.mirak.hermit.reveal");

            c.flag("__FLAG_DAUGHTER_OF_THE_HERMIT", 1);

            c.flag("__FLAG_HERMIT_COUNTER", 0);

          } else {

            c.sayKey("npc.mirak.hermit.deny");

            c.flag("__FLAG_HERMIT_COUNTER", 1);
          }

          return true;
        }

        if (k.equals("TRUST")) {

          if (c.flag("__FLAG_DAUGHTER_OF_THE_HERMIT") != 1) {

            c.sayKey("npc.mirak.trust.denied");

            return true;
          }

          if (c.flag("__FLAG_FIRST_TIME_TRUST") == 0) {

            c.flag("__FLAG_FIRST_TIME_TRUST", 1);

            c.flag("__GOBLINS_KILLED_BY_HERO", 0);

            c.sayKey("npc.mirak.trust.quest");

          } else if (c.flag("__GOBLINS_KILLED_BY_HERO") >= 100) {

            if (c.flag("__FLAG_RING_OF_TRUST_GIVEN") == 1) c.sayKey("npc.mirak.trust.done");
            else {

              c.giveItem("ring_of_trust");

              c.flag("__FLAG_RING_OF_TRUST_GIVEN", 1);

              if (c.karma() < 100 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
                c.karma(c.karma() + 10 * (500 - c.karma()) / 500);

              c.sayKey("npc.mirak.trust.reward");
            }

          } else c.sayKey("npc.mirak.trust.progress");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public MirakNira(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
