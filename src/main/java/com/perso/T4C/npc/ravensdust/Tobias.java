package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Tobias extends ScriptedNpc {

  public static final String ID = "Tobias";

  public static final String DISPLAY_NAME = "${npc.tobias}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.tobias}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.0.0}", "${npc.topic_keyword.tobias.0.1}"),
                  "${npc.topic.tobias.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.1.0}"), "${npc.topic.tobias.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.2.0}"), "${npc.topic.tobias.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.3.0}", "${npc.topic_keyword.tobias.3.1}"),
                  "${npc.topic.tobias.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.4.0}"), "${npc.topic.tobias.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.5.0}"), "${npc.topic.tobias.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.6.0}"), "${npc.topic.tobias.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.7.0}"), "${npc.topic.tobias.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.tobias.8.0}"), "${npc.topic.tobias.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.tobias.9.0}",
                      "${npc.topic_keyword.tobias.9.1}",
                      "${npc.topic_keyword.tobias.9.2}",
                      "${npc.topic_keyword.tobias.9.3}",
                      "${npc.topic_keyword.tobias.9.4}"),
                  "${npc.topic.tobias.9}",
                  List.of())),
          "Scribe",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase();

        if (k.contains("ROYAL KEY")) {

          if (c.flag("QUEST_ROYAL_KEY6") == 0) c.flag("QUEST_ROYAL_KEY6", 1);

          return false;
        }

        if (k.contains("BOOK OF WARFARE")) {

          c.askYesNo("TOBIAS_BOOK");

          return true;
        }

        if (k.equals("BOOK")) {

          c.askYesNo("TOBIAS_BOOK_GENERAL");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("TOBIAS_BOOK".equals(state)) {

          if (yes && c.player().getGold() >= 15000) {

            c.player().addGold(-15000);

            c.giveItem("book_of_warfare");

            c.sayKey("message.tobias.book_bought");

          } else if (yes) c.sayKey("message.tobias.not_enough");
          else c.sayKey("message.tobias.book_no");

          return true;
        }

        if ("TOBIAS_BOOK_GENERAL".equals(state)) {

          c.sayKey(yes ? "message.tobias.book_which" : "message.tobias.book_no");

          return true;
        }

        return false;
      }
    };
  }

  public Tobias(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
