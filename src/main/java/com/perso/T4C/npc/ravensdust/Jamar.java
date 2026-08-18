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

public final class Jamar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Jamar";

  public static final String DISPLAY_NAME = "${npc.jamar}";

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
          "${npc.welcome.jamar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.0.0}"), "${npc.topic.jamar.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.1.0}"), "${npc.topic.jamar.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.2.0}"), "${npc.topic.jamar.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.3.0}"), "${npc.topic.jamar.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.4.0}"), "${npc.topic.jamar.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.5.0}"), "${npc.topic.jamar.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.6.0}"), "${npc.topic.jamar.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.7.0}"), "${npc.topic.jamar.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.8.0}"), "${npc.topic.jamar.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.9.0}"), "${npc.topic.jamar.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jamar.10.0}"), "${npc.topic.jamar.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jamar.11.0}",
                      "${npc.topic_keyword.jamar.11.1}",
                      "${npc.topic_keyword.jamar.11.2}"),
                  "${npc.topic.jamar.11}",
                  List.of())),
          "DesertNomad",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private void giveLetter(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int n = c.itemCount("letter_to_jamar");

        if (n == 0) {

          c.sayKey("npc.jamar.letter.need");

          return;
        }

        while (c.hasItem("letter_to_jamar")) c.takeItem("letter_to_jamar");

        if (c.flag("__QUEST_ROYAL_KEY5") >= 9) {

          c.sayKey("npc.jamar.letter.valid");

          if (c.flag("__QUEST_ROYAL_KEY5") == 9 && !c.hasItem("letter_to_kalir")) {

            c.giveItem("letter_to_kalir");

            c.flag("__QUEST_ROYAL_KEY5", 10);
          }

        } else c.sayKey("npc.jamar.letter.old");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("GIVE LETTER")) {

          giveLetter(c);

          return true;
        }

        if (k.equals("KALIR")) {

          c.sayKey("npc.jamar.kalir");

          giveLetter(c);

          return true;
        }

        if (k.equals("ROYAL KEY")) {

          c.sayKey(
              c.flag("__QUEST_ROYAL_KEY5") >= 10 ? "npc.jamar.key.no" : "npc.jamar.key.secret");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Jamar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
