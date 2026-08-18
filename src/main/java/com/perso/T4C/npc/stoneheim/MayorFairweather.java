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

public final class MayorFairweather extends ScriptedNpc {

  public static final String ID = "MayorFairweather";

  public static final String DISPLAY_NAME = "${npc.mayorfairweather}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.mayorfairweather}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.0.0}",
                      "${npc.topic_keyword.mayorfairweather.0.1}"),
                  "${npc.topic.mayorfairweather.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.1.0}",
                      "${npc.topic_keyword.mayorfairweather.1.1}",
                      "${npc.topic_keyword.mayorfairweather.1.2}"),
                  "${npc.topic.mayorfairweather.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.2.0}",
                      "${npc.topic_keyword.mayorfairweather.2.1}"),
                  "${npc.topic.mayorfairweather.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mayorfairweather.3.0}"),
                  "${npc.topic.mayorfairweather.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mayorfairweather.4.0}"),
                  "${npc.topic.mayorfairweather.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.5.0}",
                      "${npc.topic_keyword.mayorfairweather.5.1}",
                      "${npc.topic_keyword.mayorfairweather.5.2}"),
                  "${npc.topic.mayorfairweather.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.6.0}",
                      "${npc.topic_keyword.mayorfairweather.6.1}"),
                  "${npc.topic.mayorfairweather.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.7.0}",
                      "${npc.topic_keyword.mayorfairweather.7.1}",
                      "${npc.topic_keyword.mayorfairweather.7.2}"),
                  "${npc.topic.mayorfairweather.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.8.0}",
                      "${npc.topic_keyword.mayorfairweather.8.1}",
                      "${npc.topic_keyword.mayorfairweather.8.2}",
                      "${npc.topic_keyword.mayorfairweather.8.3}"),
                  "${npc.topic.mayorfairweather.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mayorfairweather.9.0}",
                      "${npc.topic_keyword.mayorfairweather.9.1}",
                      "${npc.topic_keyword.mayorfairweather.9.2}",
                      "${npc.topic_keyword.mayorfairweather.9.3}",
                      "${npc.topic_keyword.mayorfairweather.9.4}"),
                  "${npc.topic.mayorfairweather.9}",
                  List.of())),
          "MayorFairweatherNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("THANK") && k.contains("NOTE")) {

          if (c.itemCount("thank_you_note") >= 6) {

            c.sayKey("npc.mayor.notes.ask");

            c.askYesNo("mayor_notes");

          } else c.sayKey("npc.mayor.notes.need");

          return true;
        }

        if (k.contains("CERTIFICATE") && k.contains("RECOGNITION")) {

          if (c.itemCount("certificate_of_recognition") >= 6) {

            c.sayKey("npc.mayor.certificate.ask");

            c.askYesNo("mayor_certificate");

          } else c.sayKey("npc.mayor.certificate.need");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("mayor_notes".equals(s)) {

          if (yes && c.itemCount("thank_you_note") >= 6) {

            for (int i = 0; i < 6; i++) c.takeItem("thank_you_note");

            c.giveItem("lost_blade_of_the_dragon");

            c.giveXp(c.player().getLevel() * 3000);

            c.sayKey("npc.mayor.notes.done");
          }

          return true;
        }

        if ("mayor_certificate".equals(s)) {

          if (yes && c.itemCount("certificate_of_recognition") >= 6) {

            for (int i = 0; i < 6; i++) c.takeItem("certificate_of_recognition");

            c.giveItem("lost_helm_of_the_dragon");

            c.giveXp(c.player().getLevel() * 2000);

            c.giveGold(c.player().getLevel() * 1000);

            c.sayKey("npc.mayor.certificate.done");
          }

          return true;
        }

        return false;
      }
    };
  }

  public MayorFairweather(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
