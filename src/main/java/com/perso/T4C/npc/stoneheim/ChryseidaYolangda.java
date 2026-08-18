package com.perso.T4C.npc.stoneheim;

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

public final class ChryseidaYolangda extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChryseidaYolangda";

  public static final String DISPLAY_NAME = "${npc.chryseidayolangda}";

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
          "${npc.welcome.chryseidayolangda}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.0.0}",
                      "${npc.topic_keyword.chryseidayolangda.0.1}"),
                  "${npc.topic.chryseidayolangda.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.1.0}",
                      "${npc.topic_keyword.chryseidayolangda.1.1}",
                      "${npc.topic_keyword.chryseidayolangda.1.2}"),
                  "${npc.topic.chryseidayolangda.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.2.0}",
                      "${npc.topic_keyword.chryseidayolangda.2.1}"),
                  "${npc.topic.chryseidayolangda.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.3.0}"),
                  "${npc.topic.chryseidayolangda.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.4.0}",
                      "${npc.topic_keyword.chryseidayolangda.4.1}"),
                  "${npc.topic.chryseidayolangda.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.5.0}"),
                  "${npc.topic.chryseidayolangda.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.6.0}"),
                  "${npc.topic.chryseidayolangda.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.7.0}"),
                  "${npc.topic.chryseidayolangda.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.8.0}"),
                  "${npc.topic.chryseidayolangda.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.9.0}",
                      "${npc.topic_keyword.chryseidayolangda.9.1}"),
                  "${npc.topic.chryseidayolangda.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.10.0}"),
                  "${npc.topic.chryseidayolangda.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.11.0}"),
                  "${npc.topic.chryseidayolangda.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.12.0}",
                      "${npc.topic_keyword.chryseidayolangda.12.1}",
                      "${npc.topic_keyword.chryseidayolangda.12.2}",
                      "${npc.topic_keyword.chryseidayolangda.12.3}"),
                  "${npc.topic.chryseidayolangda.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.13.0}"),
                  "${npc.topic.chryseidayolangda.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.14.0}",
                      "${npc.topic_keyword.chryseidayolangda.14.1}",
                      "${npc.topic_keyword.chryseidayolangda.14.2}",
                      "${npc.topic_keyword.chryseidayolangda.14.3}",
                      "${npc.topic_keyword.chryseidayolangda.14.4}"),
                  "${npc.topic.chryseidayolangda.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.15.0}",
                      "${npc.topic_keyword.chryseidayolangda.15.1}",
                      "${npc.topic_keyword.chryseidayolangda.15.2}",
                      "${npc.topic_keyword.chryseidayolangda.15.3}"),
                  "${npc.topic.chryseidayolangda.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.16.0}"),
                  "${npc.topic.chryseidayolangda.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chryseidayolangda.17.0}",
                      "${npc.topic_keyword.chryseidayolangda.17.1}"),
                  "${npc.topic.chryseidayolangda.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chryseidayolangda.18.0}"),
                  "${npc.topic.chryseidayolangda.18}",
                  List.of())),
          "ChryseidaYolangdaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("ORACLE")) {

          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") >= 1
                  ? "npc.chryseida.oracle.good"
                  : "npc.chryseida.oracle.neutral");

          return true;
        }

        if (k.equals("SEARCH")) {

          c.flag("__FLAG_CHRYSEIDA_TOLD_ABOUT_MELTAR", 1);

          c.sayKey("npc.chryseida.search");

          return true;
        }

        if (k.contains("HELP")
            || k.contains("HINT")
            || k.contains("CLUE")
            || k.contains("FORETEL")) {

          int now = (int) (System.currentTimeMillis() / 1000);

          if (c.flag("__QUEST_TIMER_CHRYSEIDA_HINTS") <= now) {

            c.flag("__QUEST_TIMER_CHRYSEIDA_HINTS", now + 3600);

            c.sayKey("npc.chryseida.hint");

          } else c.sayKey("npc.chryseida.hint.wait");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public ChryseidaYolangda(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
