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

public final class Kalir extends ScriptedNpc {

  public static final String ID = "Kalir";

  public static final String DISPLAY_NAME = "${npc.kalir}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupBarossaShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.kalir}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.0.0}"), "${npc.topic.kalir.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.1.0}"), "${npc.topic.kalir.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.2.0}"), "${npc.topic.kalir.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.3.0}"), "${npc.topic.kalir.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.4.0}"), "${npc.topic.kalir.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.5.0}"), "${npc.topic.kalir.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.6.0}"), "${npc.topic.kalir.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.7.0}"), "${npc.topic.kalir.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.8.0}", "${npc.topic_keyword.kalir.8.1}"),
                  "${npc.topic.kalir.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.9.0}"), "${npc.topic.kalir.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.10.0}"), "${npc.topic.kalir.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kalir.11.0}"), "${npc.topic.kalir.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kalir.12.0}",
                      "${npc.topic_keyword.kalir.12.1}",
                      "${npc.topic_keyword.kalir.12.2}"),
                  "${npc.topic.kalir.12}",
                  List.of())),
          "DesertNomad",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("GIVE LETTER")) {

          while (c.hasItem("letter_to_kalir")) c.takeItem("letter_to_kalir");

          if (c.flag("__QUEST_ROYAL_KEY5") >= 10) {

            c.sayKey("npc.kalir.letter.valid");

            if (c.flag("__QUEST_ROYAL_KEY5") == 10) c.flag("__QUEST_ROYAL_KEY5", 11);

          } else c.sayKey("npc.kalir.letter.invalid");

          return true;
        }

        if (k.equals("ROYAL KEY")) {

          c.sayKey(c.flag("__QUEST_ROYAL_KEY5") <= 10 ? "npc.kalir.key.no" : "npc.kalir.key.yes");

          if (c.flag("__QUEST_ROYAL_KEY5") == 11) c.flag("__QUEST_ROYAL_KEY5", 12);

          return true;
        }

        if (k.equals("JAMAR")) {

          if (c.flag("__QUEST_ROYAL_KEY5") == 8 || c.flag("__QUEST_ROYAL_KEY5") == 9) {

            c.sayKey("npc.kalir.jamar.ask");

            c.askYesNo("kalir_letter");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"kalir_letter".equals(s)) return false;

        if (yes) {

          if (c.flag("__QUEST_ROYAL_KEY5") == 8) c.flag("__QUEST_ROYAL_KEY5", 9);

          if (!c.hasItem("letter_to_jamar")) c.giveItem("letter_to_jamar");

          c.sayKey("npc.kalir.letter.give");
        }

        return true;
      }
    };
  }

  public Kalir(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
