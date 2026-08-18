package com.perso.T4C.npc.stoneheim;

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

public final class BastienMilandra extends ScriptedNpc {

  public static final String ID = "BastienMilandra";

  public static final String DISPLAY_NAME = "${npc.bastienmilandra}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupPlateBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupPlateLegs"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupPlateGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupPlateGloveL")),
          0,
          List.of(),
          "${npc.welcome.bastienmilandra}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bastienmilandra.0.0}",
                      "${npc.topic_keyword.bastienmilandra.0.1}"),
                  "${npc.topic.bastienmilandra.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bastienmilandra.1.0}",
                      "${npc.topic_keyword.bastienmilandra.1.1}",
                      "${npc.topic_keyword.bastienmilandra.1.2}"),
                  "${npc.topic.bastienmilandra.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bastienmilandra.2.0}",
                      "${npc.topic_keyword.bastienmilandra.2.1}"),
                  "${npc.topic.bastienmilandra.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.bastienmilandra.3.0}"),
                  "${npc.topic.bastienmilandra.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bastienmilandra.4.0}",
                      "${npc.topic_keyword.bastienmilandra.4.1}"),
                  "${npc.topic.bastienmilandra.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.bastienmilandra.5.0}"),
                  "${npc.topic.bastienmilandra.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bastienmilandra.6.0}",
                      "${npc.topic_keyword.bastienmilandra.6.1}",
                      "${npc.topic_keyword.bastienmilandra.6.2}",
                      "${npc.topic_keyword.bastienmilandra.6.3}"),
                  "${npc.topic.bastienmilandra.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bastienmilandra.7.0}",
                      "${npc.topic_keyword.bastienmilandra.7.1}",
                      "${npc.topic_keyword.bastienmilandra.7.2}",
                      "${npc.topic_keyword.bastienmilandra.7.3}"),
                  "${npc.topic.bastienmilandra.7}",
                  List.of())),
          "BastienMilandraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private static final String BELIEVE = "npc:BastienMilandra:DO_YOU_BELIEVE";

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("NAME") || k.equals("WHO ARE YOU")) {

            c.sayKey("npc.bastien.name");

            c.askYesNo("believe");

            return true;
          }

          if (k.equals("WORK") || k.equals("WHAT DO YOU DO") || k.equals("OCCUPATION")) {

            c.sayKey("npc.bastien.work");

            c.askYesNo("believe2");

            return true;
          }

          if (k.equals("HELP") || k.equals("QUEST")) {

            c.sayKey("npc.bastien.help");

            c.askYesNo("believe3");

            return true;
          }

          if (k.equals("SERAPH")) {

            c.sayKey("npc.bastien.seraph");

            c.askYesNo("seraph");

            return true;
          }

          if (k.contains("ETHER") && k.contains("KEY")) {

            if (c.hasItem("broken_ethereal_key")) {

              c.sayKey("npc.bastien.key.ask");

              c.askYesNo("fix");

            } else c.sayKey("npc.bastien.key.none");

            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

          if (state.startsWith("believe")) {

            c.sayKey(yes ? "npc.bastien.believe.yes" : "npc.bastien.believe.no");

            if (yes) c.flag(BELIEVE, c.flag(BELIEVE) + 1);
            else c.flag(BELIEVE, 0);

            return true;
          }

          if ("seraph".equals(state)) {

            c.sayKey(yes ? "npc.bastien.seraph.yes" : "npc.bastien.seraph.no");

            return true;
          }

          if ("fix".equals(state)) {

            if (yes && c.flag(BELIEVE) >= 3 && c.hasItem("broken_ethereal_key")) {

              c.takeItem("broken_ethereal_key");

              c.giveItem("fixed_ethereal_key");

              c.sayKey("npc.bastien.key.fixed");

            } else if (yes) c.sayKey("npc.bastien.key.notnice");
            else c.sayKey("npc.bastien.no");

            return true;
          }

          return false;
        }
      };

  public BastienMilandra(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
