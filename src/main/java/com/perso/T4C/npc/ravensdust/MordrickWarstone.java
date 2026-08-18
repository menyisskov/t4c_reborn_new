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

public final class MordrickWarstone extends ScriptedNpc {

  public static final String ID = "MordrickWarstone";

  public static final String DISPLAY_NAME = "${npc.mordrickwarstone}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupPlateHelm"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleAxe"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupPlateGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupPlateGloveL")),
          0,
          List.of(),
          "${npc.welcome.mordrickwarstone}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.0.0}",
                      "${npc.topic_keyword.mordrickwarstone.0.1}"),
                  "${npc.topic.mordrickwarstone.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.1.0}",
                      "${npc.topic_keyword.mordrickwarstone.1.1}",
                      "${npc.topic_keyword.mordrickwarstone.1.2}",
                      "${npc.topic_keyword.mordrickwarstone.1.3}"),
                  "${npc.topic.mordrickwarstone.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.2.0}",
                      "${npc.topic_keyword.mordrickwarstone.2.1}"),
                  "${npc.topic.mordrickwarstone.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.3.0}"),
                  "${npc.topic.mordrickwarstone.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.4.0}"),
                  "${npc.topic.mordrickwarstone.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.5.0}",
                      "${npc.topic_keyword.mordrickwarstone.5.1}"),
                  "${npc.topic.mordrickwarstone.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.6.0}"),
                  "${npc.topic.mordrickwarstone.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.7.0}"),
                  "${npc.topic.mordrickwarstone.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.8.0}"),
                  "${npc.topic.mordrickwarstone.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.9.0}"),
                  "${npc.topic.mordrickwarstone.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.10.0}"),
                  "${npc.topic.mordrickwarstone.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.11.0}",
                      "${npc.topic_keyword.mordrickwarstone.11.1}",
                      "${npc.topic_keyword.mordrickwarstone.11.2}"),
                  "${npc.topic.mordrickwarstone.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.12.0}"),
                  "${npc.topic.mordrickwarstone.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordrickwarstone.13.0}"),
                  "${npc.topic.mordrickwarstone.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.14.0}",
                      "${npc.topic_keyword.mordrickwarstone.14.1}",
                      "${npc.topic_keyword.mordrickwarstone.14.2}",
                      "${npc.topic_keyword.mordrickwarstone.14.3}"),
                  "${npc.topic.mordrickwarstone.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordrickwarstone.15.0}",
                      "${npc.topic_keyword.mordrickwarstone.15.1}",
                      "${npc.topic_keyword.mordrickwarstone.15.2}",
                      "${npc.topic_keyword.mordrickwarstone.15.3}",
                      "${npc.topic_keyword.mordrickwarstone.15.4}"),
                  "${npc.topic.mordrickwarstone.15}",
                  List.of())),
          "Royal_Guard",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase();

        if (k.contains("SWORD OF MIGHT")) {

          if (c.itemCount("sword_of_might") > 1) c.sayKey("message.mordrick.fake_swords");
          else if (c.hasItem("sword_of_might")) {

            c.giveItem("royal_key_6");

            c.takeItem("sword_of_might");

            c.flag("QUEST_ROYAL_KEY6", 6);

            c.sayKey("message.mordrick.key_given");

          } else c.sayKey("message.mordrick.need_sword");

          return true;
        }

        if (k.contains("ROYAL KEY")) {

          if (c.flag("QUEST_ROYAL_KEY6") < 5) c.sayKey("message.mordrick.not_cleared");
          else if (c.flag("QUEST_FIXED_ALIGNMENT") != 1) c.sayKey("message.mordrick.alignment");
          else if (c.karma() < 0) c.sayKey("message.mordrick.karma");
          else if (c.hasItem("sword_of_might")) {

            c.giveItem("royal_key_6");

            c.takeItem("sword_of_might");

            c.flag("QUEST_ROYAL_KEY6", 6);

            c.sayKey("message.mordrick.key_given");

          } else c.sayKey("message.mordrick.need_sword");

          return true;
        }

        return false;
      }
    };
  }

  public MordrickWarstone(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
