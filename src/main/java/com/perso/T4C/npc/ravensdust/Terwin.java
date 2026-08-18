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

public final class Terwin extends ScriptedNpc {

  public static final String ID = "Terwin";

  public static final String DISPLAY_NAME = "${npc.terwin}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupNormalSword"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.terwin}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.0.0}", "${npc.topic_keyword.terwin.0.1}"),
                  "${npc.topic.terwin.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.1.0}"), "${npc.topic.terwin.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.2.0}", "${npc.topic_keyword.terwin.2.1}"),
                  "${npc.topic.terwin.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.3.0}"), "${npc.topic.terwin.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.4.0}"), "${npc.topic.terwin.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.5.0}"), "${npc.topic.terwin.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.6.0}", "${npc.topic_keyword.terwin.6.1}"),
                  "${npc.topic.terwin.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.7.0}"), "${npc.topic.terwin.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.8.0}", "${npc.topic_keyword.terwin.8.1}"),
                  "${npc.topic.terwin.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.terwin.9.0}"), "${npc.topic.terwin.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.terwin.10.0}",
                      "${npc.topic_keyword.terwin.10.1}",
                      "${npc.topic_keyword.terwin.10.2}",
                      "${npc.topic_keyword.terwin.10.3}"),
                  "${npc.topic.terwin.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.terwin.11.0}",
                      "${npc.topic_keyword.terwin.11.1}",
                      "${npc.topic_keyword.terwin.11.2}",
                      "${npc.topic_keyword.terwin.11.3}",
                      "${npc.topic_keyword.terwin.11.4}"),
                  "${npc.topic.terwin.11}",
                  List.of())),
          "Guard_set_two",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("POTION OF HERO")) {

          if (c.flag("__QUEST_ROYAL_KEY2") == 2 && c.hasItem("potion_of_heroism")) {

            c.sayKey("npc.terwin.potion.ask");

            c.askYesNo("terwin_potion");
          }

          return true;
        }

        if (k.equals("ROYAL KEY")) {

          int q = c.flag("__QUEST_DEAD_BROTHERS");

          if (q < 12) c.sayKey("npc.terwin.key.locked");
          else if (c.flag("__QUEST_ROYAL_KEY2") <= 3) {

            c.sayKey("npc.terwin.key.bribe");

            if (c.flag("__QUEST_ROYAL_KEY2") == 0) c.flag("__QUEST_ROYAL_KEY2", 1);

          } else {

            c.sayKey("npc.terwin.key.info");

            if (c.flag("__QUEST_ROYAL_KEY2") == 4) c.flag("__QUEST_ROYAL_KEY2", 5);
          }

          return true;
        }

        if (k.equals("HONEST")) {

          int q = c.flag("__QUEST_ROYAL_KEY2");

          if (q == 1) {

            if (c.karma() <= 50) c.sayKey("npc.terwin.honest.bad");
            else {

              c.sayKey("npc.terwin.honest.potion");

              c.flag("__QUEST_ROYAL_KEY2", 2);
            }

          } else if (q == 2) c.sayKey("npc.terwin.honest.wait");
          else if (q == 3) {

            c.sayKey("npc.terwin.honest.bribe");

            c.askYesNo("terwin_bribe");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("terwin_potion".equals(s)) {

          if (yes && c.hasItem("potion_of_heroism")) {

            c.takeItem("potion_of_heroism");

            c.flag("__QUEST_ROYAL_KEY2", 3);

            c.sayKey("npc.terwin.potion.done");
          }

          return true;
        }

        if ("terwin_bribe".equals(s)) {

          if (yes && c.player().getGold() >= 5000) {

            c.player().addGold(-5000);

            c.flag("__QUEST_ROYAL_KEY2", 4);

            c.sayKey("npc.terwin.bribe.done");
          }

          return true;
        }

        return false;
      }
    };
  }

  public Terwin(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
