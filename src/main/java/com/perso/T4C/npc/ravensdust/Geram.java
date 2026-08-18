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

public final class Geram extends ScriptedNpc {

  public static final String ID = "Geram";

  public static final String DISPLAY_NAME = "${npc.geram}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.geram}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geram.0.0}", "${npc.topic_keyword.geram.0.1}"),
                  "${npc.topic.geram.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geram.1.0}", "${npc.topic_keyword.geram.1.1}"),
                  "${npc.topic.geram.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geram.2.0}"), "${npc.topic.geram.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.geram.3.0}",
                      "${npc.topic_keyword.geram.3.1}",
                      "${npc.topic_keyword.geram.3.2}"),
                  "${npc.topic.geram.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geram.4.0}"), "${npc.topic.geram.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geram.5.0}", "${npc.topic_keyword.geram.5.1}"),
                  "${npc.topic.geram.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.geram.6.0}"), "${npc.topic.geram.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.geram.7.0}",
                      "${npc.topic_keyword.geram.7.1}",
                      "${npc.topic_keyword.geram.7.2}",
                      "${npc.topic_keyword.geram.7.3}"),
                  "${npc.topic.geram.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.geram.8.0}",
                      "${npc.topic_keyword.geram.8.1}",
                      "${npc.topic_keyword.geram.8.2}",
                      "${npc.topic_keyword.geram.8.3}",
                      "${npc.topic_keyword.geram.8.4}"),
                  "${npc.topic.geram.8}",
                  List.of())),
          "GeramNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey("npc.geram.gold.ask");

        c.askYesNo("geram_gold");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("WORK")) {

          c.sayKey("npc.geram.work.ask");

          c.askYesNo("geram_work");

          return true;
        }

        if (k.contains("COMBINATION")) {

          if (c.flag("__QUEST_VAULT_CODE_TWO") == 0) c.sayKey("npc.geram.combination.no");
          else {

            c.sayKey("npc.geram.combination.ask");

            c.askYesNo("geram_beer");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("geram_gold".equals(s)) {

          if (yes && c.player().getGold() >= 1) {

            c.player().addGold(-1);

            c.sayKey("npc.geram.gold.done");
          }

          return true;
        }

        if ("geram_work".equals(s)) {

          c.sayKey(yes ? "npc.geram.work.yes" : "npc.geram.work.no");

          return true;
        }

        if ("geram_beer".equals(s)) {

          if (yes && c.hasItem("empty_beer_mug")) {

            c.takeItem("empty_beer_mug");

            c.say(
                String.format(
                    java.util.Locale.ROOT,
                    "Thank you for the ale! The 2nd number of the combination is %d to the LEFT.",
                    c.flag("__QUEST_VAULT_CODE_TWO")));
          }

          return true;
        }

        return false;
      }
    };
  }

  public Geram(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
