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

public final class RemortNPC2 extends ScriptedNpc {

  public static final String ID = "RemortNPC2";

  public static final String DISPLAY_NAME = "${npc.remortnpc2}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.remortnpc2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.0.0}", "${npc.topic_keyword.remortnpc2.0.1}"),
                  "${npc.topic.remortnpc2.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.1.0}",
                      "${npc.topic_keyword.remortnpc2.1.1}",
                      "${npc.topic_keyword.remortnpc2.1.2}"),
                  "${npc.topic.remortnpc2.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.2.0}", "${npc.topic_keyword.remortnpc2.2.1}"),
                  "${npc.topic.remortnpc2.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.3.0}"),
                  "${npc.topic.remortnpc2.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.4.0}"),
                  "${npc.topic.remortnpc2.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.5.0}"),
                  "${npc.topic.remortnpc2.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.6.0}"),
                  "${npc.topic.remortnpc2.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.7.0}"),
                  "${npc.topic.remortnpc2.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.8.0}",
                      "${npc.topic_keyword.remortnpc2.8.1}",
                      "${npc.topic_keyword.remortnpc2.8.2}",
                      "${npc.topic_keyword.remortnpc2.8.3}"),
                  "${npc.topic.remortnpc2.8}",
                  List.of())),
          "RemortNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_REMORT_PROCESS") == 0) c.sayKey("npc.remort.betran.need_alphan");
        else if (c.flag("__FLAG_REMORT_POINTS") > 0) c.sayKey("npc.remort.betran.welcome");
        else c.sayKey("npc.remort.betran.empty");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ATTRIBUTE") || k.equals("STAT")) {

          c.sayKey(
              c.flag("__FLAG_REMORT_POINTS") > 0
                  ? "npc.remort.betran.attributes"
                  : "npc.remort.betran.empty");

          return true;
        }

        String a =
            k.equals("STRENGTH")
                ? "strength"
                : k.equals("AGILITY")
                    ? "agility"
                    : k.equals("ENDURANCE")
                        ? "endurance"
                        : k.equals("INTELLIGENCE")
                            ? "intelligence"
                            : k.equals("WISDOM") ? "wisdom" : null;

        if (a != null) {

          int current =
              switch (a) {
                case "strength" -> c.player().getStrength();

                case "agility" -> c.player().getDexterity();

                case "endurance" -> c.player().getEndurance();

                case "intelligence" -> c.player().getIntelligence();

                default -> c.player().getWisdom();
              };

          int delta = current - (20 + c.flag("__FLAG_NUMBER_OF_REMORTS") * 5);

          if (delta > 9) c.sayKey("npc.remort.betran.cap");
          else {

            int cost = delta == 0 ? 1 : delta <= 3 ? 2 : delta <= 6 ? 3 : delta == 7 ? 4 : 5;

            c.sayKey("npc.remort.betran.cost", cost);

            c.askYesNo("remort_" + a);
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!s.startsWith("remort_")) return false;

        if (!yes) return true;

        String a = s.substring(7);

        int current =
            switch (a) {
              case "strength" -> c.player().getStrength();

              case "agility" -> c.player().getDexterity();

              case "endurance" -> c.player().getEndurance();

              case "intelligence" -> c.player().getIntelligence();

              default -> c.player().getWisdom();
            };

        int delta = current - (20 + c.flag("__FLAG_NUMBER_OF_REMORTS") * 5),
            cost = delta == 0 ? 1 : delta <= 3 ? 2 : delta <= 6 ? 3 : delta == 7 ? 4 : 5;

        if (delta > 9 || c.flag("__FLAG_REMORT_POINTS") < cost) {

          c.sayKey("npc.remort.betran.not_enough");

          return true;
        }

        c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - cost);

        if (a.equals("strength")) c.player().setStrength(current + 1);
        else if (a.equals("agility")) c.player().setDexterity(current + 1);
        else if (a.equals("endurance")) c.player().setEndurance(current + 1);
        else if (a.equals("intelligence")) c.player().setIntelligence(current + 1);
        else c.player().setWisdom(current + 1);

        c.sayKey("npc.remort.betran.done");

        return true;
      }
    };
  }

  public RemortNPC2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
