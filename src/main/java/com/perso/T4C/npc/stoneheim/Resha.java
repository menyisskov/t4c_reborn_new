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

public final class Resha extends ScriptedNpc {

  public static final String ID = "Resha";

  public static final String DISPLAY_NAME = "${npc.resha}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoPlateBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoPlateBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "WoPlateLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "WoPlateHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "WoPlateGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "WoPlateGloveL")),
          0,
          List.of(),
          "${npc.welcome.resha}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.0.0}", "${npc.topic_keyword.resha.0.1}"),
                  "${npc.topic.resha.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.1.0}", "${npc.topic_keyword.resha.1.1}"),
                  "${npc.topic.resha.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.2.0}", "${npc.topic_keyword.resha.2.1}"),
                  "${npc.topic.resha.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.3.0}", "${npc.topic_keyword.resha.3.1}"),
                  "${npc.topic.resha.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.resha.4.0}",
                      "${npc.topic_keyword.resha.4.1}",
                      "${npc.topic_keyword.resha.4.2}"),
                  "${npc.topic.resha.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.5.0}"), "${npc.topic.resha.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.6.0}", "${npc.topic_keyword.resha.6.1}"),
                  "${npc.topic.resha.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.resha.7.0}",
                      "${npc.topic_keyword.resha.7.1}",
                      "${npc.topic_keyword.resha.7.2}"),
                  "${npc.topic.resha.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.resha.8.0}", "${npc.topic_keyword.resha.8.1}"),
                  "${npc.topic.resha.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.resha.9.0}",
                      "${npc.topic_keyword.resha.9.1}",
                      "${npc.topic_keyword.resha.9.2}"),
                  "${npc.topic.resha.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.resha.10.0}",
                      "${npc.topic_keyword.resha.10.1}",
                      "${npc.topic_keyword.resha.10.2}",
                      "${npc.topic_keyword.resha.10.3}",
                      "${npc.topic_keyword.resha.10.4}"),
                  "${npc.topic.resha.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.resha.11.0}",
                      "${npc.topic_keyword.resha.11.1}",
                      "${npc.topic_keyword.resha.11.2}",
                      "${npc.topic_keyword.resha.11.3}",
                      "${npc.topic_keyword.resha.11.4}"),
                  "${npc.topic.resha.11}",
                  List.of())),
          "ReshaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("BOW") && k.contains("CENTAUR") && k.contains("SLAY")) {

          int n = c.itemCount("bow_of_centaur_slaying");

          if (n >= 5) {

            c.sayKey("npc.resha.bow.ask");

            c.askYesNo("resha_bows");

          } else c.sayKey(n > 0 ? "npc.resha.bow.progress" : "npc.resha.bow.info");

          return true;
        }

        if (k.equals("HELP") || k.equals("QUEST")) {

          c.sayKey(
              c.hasItem("bow_of_centaur_slaying")
                  ? "npc.resha.quest.bows"
                  : "npc.resha.quest.none");

          return true;
        }

        if (k.equals("ORACLE")) {

          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") >= 1
                  ? "npc.resha.oracle.good"
                  : "npc.resha.oracle.bad");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"resha_bows".equals(s)) return false;

        if (yes && c.itemCount("bow_of_centaur_slaying") >= 5) {

          for (int i = 0; i < 5; i++) c.takeItem("bow_of_centaur_slaying");

          c.giveItem("cloak_of_the_archer");

          c.giveXp(c.player().getLevel() * 2500);

          c.sayKey("npc.resha.bow.done");
        }

        return true;
      }
    };
  }

  public Resha(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
