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

public final class GrantHornkeep extends ScriptedNpc {

  public static final String ID = "GrantHornkeep";

  public static final String DISPLAY_NAME = "${npc.granthornkeep}";

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
          "${npc.welcome.granthornkeep}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.0.0}",
                      "${npc.topic_keyword.granthornkeep.0.1}"),
                  "${npc.topic.granthornkeep.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.1.0}",
                      "${npc.topic_keyword.granthornkeep.1.1}"),
                  "${npc.topic.granthornkeep.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.2.0}",
                      "${npc.topic_keyword.granthornkeep.2.1}"),
                  "${npc.topic.granthornkeep.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.granthornkeep.3.0}"),
                  "${npc.topic.granthornkeep.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.4.0}",
                      "${npc.topic_keyword.granthornkeep.4.1}"),
                  "${npc.topic.granthornkeep.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.5.0}",
                      "${npc.topic_keyword.granthornkeep.5.1}"),
                  "${npc.topic.granthornkeep.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.granthornkeep.6.0}"),
                  "${npc.topic.granthornkeep.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.granthornkeep.7.0}"),
                  "${npc.topic.granthornkeep.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.8.0}",
                      "${npc.topic_keyword.granthornkeep.8.1}",
                      "${npc.topic_keyword.granthornkeep.8.2}"),
                  "${npc.topic.granthornkeep.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.granthornkeep.9.0}"),
                  "${npc.topic.granthornkeep.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.10.0}",
                      "${npc.topic_keyword.granthornkeep.10.1}"),
                  "${npc.topic.granthornkeep.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.11.0}",
                      "${npc.topic_keyword.granthornkeep.11.1}",
                      "${npc.topic_keyword.granthornkeep.11.2}"),
                  "${npc.topic.granthornkeep.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.12.0}",
                      "${npc.topic_keyword.granthornkeep.12.1}",
                      "${npc.topic_keyword.granthornkeep.12.2}"),
                  "${npc.topic.granthornkeep.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.13.0}",
                      "${npc.topic_keyword.granthornkeep.13.1}"),
                  "${npc.topic.granthornkeep.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.granthornkeep.14.0}"),
                  "${npc.topic.granthornkeep.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.15.0}",
                      "${npc.topic_keyword.granthornkeep.15.1}"),
                  "${npc.topic.granthornkeep.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.granthornkeep.16.0}",
                      "${npc.topic_keyword.granthornkeep.16.1}",
                      "${npc.topic_keyword.granthornkeep.16.2}",
                      "${npc.topic_keyword.granthornkeep.16.3}",
                      "${npc.topic_keyword.granthornkeep.16.4}"),
                  "${npc.topic.granthornkeep.16}",
                  List.of())),
          "GrantHornkeepNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SECRET") && k.contains("DOCUMENT")) {

          if (c.itemCount("secret_document") >= 5) {

            c.sayKey("npc.grant.documents.ask");

            c.askYesNo("grant_documents");

          } else c.sayKey("npc.grant.documents.info");

          return true;
        }

        if (k.contains("NIGHTCREEPER")) {

          if (c.flag("__FLAG_NIGHTCREEPERS_KILLED") >= 9) {

            c.flag("__FLAG_NIGHTCREEPERS_KILLED", 0);

            c.giveItem("thank_you_note");

            c.giveXp(Math.min(c.player().getLevel() * 1500, 50000));

            c.sayKey("npc.grant.night.done");

          } else c.sayKey("npc.grant.night.need");

          return true;
        }

        if (k.contains("PILFERER")) {

          if (c.flag("__FLAG_PILFERERS_KILLED") >= 8) {

            c.flag("__FLAG_PILFERERS_KILLED", 0);

            c.giveItem("thank_you_note");

            c.giveXp(Math.min(c.player().getLevel() * 1000, 50000));

            c.sayKey("npc.grant.pilfer.done");

          } else c.sayKey("npc.grant.pilfer.need");

          return true;
        }

        if (k.contains("BELT") && k.contains("SKRAUG") && k.contains("STRENGTH")) {

          if (c.itemCount("belt_of_skraug_strength") >= 3) {

            c.sayKey("npc.grant.belts.ask");

            c.askYesNo("grant_belts");

          } else c.sayKey("npc.grant.belts.info");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("grant_documents".equals(s)) {

          if (yes && c.itemCount("secret_document") >= 5) {

            for (int i = 0; i < 5; i++) c.takeItem("secret_document");

            c.giveItem("certificate_of_recognition");

            c.giveXp(c.player().getLevel() * 2500);

            c.giveGold(c.player().getLevel() * 3500);

            c.sayKey("npc.grant.documents.done");
          }

          return true;
        }

        if ("grant_belts".equals(s)) {

          if (yes && c.itemCount("belt_of_skraug_strength") >= 3) {

            for (int i = 0; i < 3; i++) c.takeItem("belt_of_skraug_strength");

            c.giveItem("collector_book");

            c.giveXp(c.player().getLevel() * 3000);

            c.sayKey("npc.grant.belts.done");
          }

          return true;
        }

        return false;
      }
    };
  }

  public GrantHornkeep(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
