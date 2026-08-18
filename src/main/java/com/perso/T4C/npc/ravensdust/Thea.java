package com.perso.T4C.npc.ravensdust;

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

public final class Thea extends ScriptedNpc {

  public static final String ID = "Thea";

  public static final String DISPLAY_NAME = "${npc.thea}";

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
          "${npc.welcome.thea}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.0.0}", "${npc.topic_keyword.thea.0.1}"),
                  "${npc.topic.thea.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.1.0}"), "${npc.topic.thea.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thea.2.0}",
                      "${npc.topic_keyword.thea.2.1}",
                      "${npc.topic_keyword.thea.2.2}"),
                  "${npc.topic.thea.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.3.0}"), "${npc.topic.thea.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thea.4.0}",
                      "${npc.topic_keyword.thea.4.1}",
                      "${npc.topic_keyword.thea.4.2}"),
                  "${npc.topic.thea.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.5.0}"), "${npc.topic.thea.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.6.0}"), "${npc.topic.thea.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.7.0}"), "${npc.topic.thea.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.8.0}"), "${npc.topic.thea.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.9.0}"), "${npc.topic.thea.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.10.0}"), "${npc.topic.thea.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.11.0}"), "${npc.topic.thea.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.12.0}", "${npc.topic_keyword.thea.12.1}"),
                  "${npc.topic.thea.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.13.0}"), "${npc.topic.thea.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.14.0}"), "${npc.topic.thea.14}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thea.15.0}"), "${npc.topic.thea.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thea.16.0}",
                      "${npc.topic_keyword.thea.16.1}",
                      "${npc.topic_keyword.thea.16.2}",
                      "${npc.topic_keyword.thea.16.3}",
                      "${npc.topic_keyword.thea.16.4}"),
                  "${npc.topic.thea.16}",
                  List.of())),
          "TheaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private boolean night() {

        int h = java.time.LocalTime.now().getHour();

        return h < 6 || h >= 18;
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("ROYAL KEY")) {

          if (night()) {

            c.sayKey("npc.thea.key.night");

            if (c.flag("__QUEST_ROYAL_KEY4") == 0) c.flag("__QUEST_ROYAL_KEY4", 1);

          } else c.sayKey("npc.thea.key.day");

          return true;
        }

        if (k.equals("PRIESTESS")) {

          if (c.flag("__QUEST_ROYAL_KEY4") >= 1) {

            c.sayKey("npc.thea.priestess.known");

            if (c.flag("__QUEST_ROYAL_KEY4") == 1) c.flag("__QUEST_ROYAL_KEY4", 2);

          } else c.sayKey("npc.thea.priestess.unknown");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Thea(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
