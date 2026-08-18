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

public final class MarsacCred extends ScriptedNpc {

  public static final String ID = "MarsacCred";

  public static final String DISPLAY_NAME = "${npc.marsaccred}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.marsaccred}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marsaccred.0.0}", "${npc.topic_keyword.marsaccred.0.1}"),
                  "${npc.topic.marsaccred.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marsaccred.1.0}",
                      "${npc.topic_keyword.marsaccred.1.1}",
                      "${npc.topic_keyword.marsaccred.1.2}"),
                  "${npc.topic.marsaccred.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marsaccred.2.0}",
                      "${npc.topic_keyword.marsaccred.2.1}",
                      "${npc.topic_keyword.marsaccred.2.2}"),
                  "${npc.topic.marsaccred.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.3.0}"),
                  "${npc.topic.marsaccred.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.4.0}"),
                  "${npc.topic.marsaccred.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.5.0}"),
                  "${npc.topic.marsaccred.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.6.0}"),
                  "${npc.topic.marsaccred.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.7.0}"),
                  "${npc.topic.marsaccred.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.8.0}"),
                  "${npc.topic.marsaccred.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.9.0}"),
                  "${npc.topic.marsaccred.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.10.0}"),
                  "${npc.topic.marsaccred.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.11.0}"),
                  "${npc.topic.marsaccred.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.12.0}"),
                  "${npc.topic.marsaccred.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marsaccred.13.0}",
                      "${npc.topic_keyword.marsaccred.13.1}",
                      "${npc.topic_keyword.marsaccred.13.2}"),
                  "${npc.topic.marsaccred.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.marsaccred.14.0}"),
                  "${npc.topic.marsaccred.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.marsaccred.15.0}",
                      "${npc.topic_keyword.marsaccred.15.1}",
                      "${npc.topic_keyword.marsaccred.15.2}",
                      "${npc.topic_keyword.marsaccred.15.3}",
                      "${npc.topic_keyword.marsaccred.15.4}"),
                  "${npc.topic.marsaccred.15}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("URANOS")) {

          if (c.flag("__QUEST_URANOS") == 1) {

            c.flag("__QUEST_URANOS_INGREDIENTS", 1);

            c.sayKey("npc.marsac.uranos");
          }

          return true;
        }

        if (k.contains("SCROLL OF ENCHANTMENT")) {

          c.sayKey("npc.marsac.scroll.ask");

          c.askYesNo("marsac_scroll");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"marsac_scroll".equals(s)) return false;

        if (yes) {

          if (c.itemCount("plain_blank_scroll") >= 1
              && c.itemCount("human_bone") >= 1
              && c.itemCount("runic_scripting_kit") >= 1
              && c.player().getGold() >= 4000) {

            c.takeItem("plain_blank_scroll");

            c.takeItem("human_bone");

            c.player().addGold(-4000);

            c.giveItem("scroll_of_enchantment");

            c.sayKey("npc.marsac.scroll.done");

          } else c.sayKey("npc.marsac.scroll.missing");
        }

        return true;
      }
    };
  }

  public MarsacCred(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
