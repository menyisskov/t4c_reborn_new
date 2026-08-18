package com.perso.T4C.npc.ravensdust;

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

public final class ElysanaBlackrose extends ScriptedNpc {

  public static final String ID = "ElysanaBlackrose";

  public static final String DISPLAY_NAME = "${npc.elysanablackrose}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "WoLeatherLegs"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.elysanablackrose}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.0.0}",
                      "${npc.topic_keyword.elysanablackrose.0.1}"),
                  "${npc.topic.elysanablackrose.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.1.0}",
                      "${npc.topic_keyword.elysanablackrose.1.1}"),
                  "${npc.topic.elysanablackrose.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.2.0}"),
                  "${npc.topic.elysanablackrose.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.3.0}"),
                  "${npc.topic.elysanablackrose.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.4.0}"),
                  "${npc.topic.elysanablackrose.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.5.0}"),
                  "${npc.topic.elysanablackrose.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.6.0}",
                      "${npc.topic_keyword.elysanablackrose.6.1}"),
                  "${npc.topic.elysanablackrose.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.7.0}",
                      "${npc.topic_keyword.elysanablackrose.7.1}"),
                  "${npc.topic.elysanablackrose.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.8.0}"),
                  "${npc.topic.elysanablackrose.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.9.0}"),
                  "${npc.topic.elysanablackrose.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.10.0}"),
                  "${npc.topic.elysanablackrose.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.11.0}",
                      "${npc.topic_keyword.elysanablackrose.11.1}"),
                  "${npc.topic.elysanablackrose.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.12.0}"),
                  "${npc.topic.elysanablackrose.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.13.0}",
                      "${npc.topic_keyword.elysanablackrose.13.1}"),
                  "${npc.topic.elysanablackrose.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.14.0}"),
                  "${npc.topic.elysanablackrose.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.15.0}"),
                  "${npc.topic.elysanablackrose.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.16.0}"),
                  "${npc.topic.elysanablackrose.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.17.0}",
                      "${npc.topic_keyword.elysanablackrose.17.1}"),
                  "${npc.topic.elysanablackrose.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.18.0}",
                      "${npc.topic_keyword.elysanablackrose.18.1}"),
                  "${npc.topic.elysanablackrose.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.19.0}"),
                  "${npc.topic.elysanablackrose.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.20.0}"),
                  "${npc.topic.elysanablackrose.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.21.0}",
                      "${npc.topic_keyword.elysanablackrose.21.1}"),
                  "${npc.topic.elysanablackrose.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.22.0}"),
                  "${npc.topic.elysanablackrose.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elysanablackrose.23.0}"),
                  "${npc.topic.elysanablackrose.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.24.0}",
                      "${npc.topic_keyword.elysanablackrose.24.1}",
                      "${npc.topic_keyword.elysanablackrose.24.2}",
                      "${npc.topic_keyword.elysanablackrose.24.3}"),
                  "${npc.topic.elysanablackrose.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elysanablackrose.25.0}",
                      "${npc.topic_keyword.elysanablackrose.25.1}",
                      "${npc.topic_keyword.elysanablackrose.25.2}",
                      "${npc.topic_keyword.elysanablackrose.25.3}",
                      "${npc.topic_keyword.elysanablackrose.25.4}"),
                  "${npc.topic.elysanablackrose.25}",
                  List.of())),
          "ElysanaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (c.flag("__QUEST_FIXED_ALIGNMENT") >= 1) c.sayKey("npc.elysana.expected");
        else {

          int h = java.time.LocalTime.now().getHour();

          c.sayKey(h >= 6 && h < 18 ? "npc.elysana.day" : "npc.elysana.night");
        }
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("INGREDIENT")) {

          if (c.hasItem("pouch_of_woody_nightshade")
              && c.hasItem("pouch_of_blue_cohosh")
              && c.hasItem("pouch_of_witch_hazel")) {

            c.sayKey("npc.elysana.ingredients.ready");

            c.askYesNo("ingredients");

          } else c.sayKey("npc.elysana.ingredients.need");

          return true;
        }

        if (k.contains("GREEN") && k.contains("GEMSTONE")) {

          c.sayKey("npc.elysana.gemstone.offer");

          c.askYesNo("gemstone");

          return true;
        }

        if (k.equals("TEACH") || k.equals("LEARN")) {

          c.openSpellLearning(java.util.List.of("minor_combat_sense", "mana_burst"));

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if (!yes) {

          c.sayKey("npc.elysana.no");

          return true;
        }

        if ("ingredients".equals(state)) {

          if (c.hasItem("pouch_of_woody_nightshade")
              && c.hasItem("pouch_of_blue_cohosh")
              && c.hasItem("pouch_of_witch_hazel")) {

            c.takeItem("pouch_of_woody_nightshade");

            c.takeItem("pouch_of_blue_cohosh");

            c.takeItem("pouch_of_witch_hazel");

            c.giveItem("black_lizardskin_boots");

            c.giveXp(8000);

            c.sayKey("npc.elysana.ingredients.done");

          } else c.sayKey("npc.elysana.ingredients.missing");

          return true;
        }

        if ("gemstone".equals(state)) {

          if (c.player().getGold() >= 20000) {

            c.player().addGold(-20000);

            c.giveItem("green_gemstone");

            c.sayKey("npc.elysana.gemstone.done");

          } else c.sayKey("npc.elysana.gemstone.poor");

          return true;
        }

        return false;
      }
    };
  }

  public ElysanaBlackrose(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
