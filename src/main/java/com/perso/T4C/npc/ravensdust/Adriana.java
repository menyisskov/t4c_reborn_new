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

public final class Adriana extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Adriana";

  public static final String DISPLAY_NAME = "${npc.adriana}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoLeatherArms"),
              new NpcSpec.Part(BodyPart.BACK, "WoLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "WoLeatherLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "WoChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "WoLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "WoLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.adriana}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.0.0}"), "${npc.topic.adriana.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.1.0}"), "${npc.topic.adriana.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.2.0}"), "${npc.topic.adriana.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.3.0}", "${npc.topic_keyword.adriana.3.1}"),
                  "${npc.topic.adriana.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.4.0}"), "${npc.topic.adriana.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.5.0}"), "${npc.topic.adriana.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.6.0}", "${npc.topic_keyword.adriana.6.1}"),
                  "${npc.topic.adriana.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.7.0}"), "${npc.topic.adriana.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.8.0}"), "${npc.topic.adriana.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.9.0}"), "${npc.topic.adriana.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.adriana.10.0}"),
                  "${npc.topic.adriana.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.adriana.11.0}",
                      "${npc.topic_keyword.adriana.11.1}",
                      "${npc.topic_keyword.adriana.11.2}",
                      "${npc.topic_keyword.adriana.11.3}",
                      "${npc.topic_keyword.adriana.11.4}"),
                  "${npc.topic.adriana.11}",
                  List.of())),
          "AdrianaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("BOOK") && k.contains("WARFARE")) {

          if (c.hasItem("book_of_warfare")) {

            c.sayKey("npc.adriana.book.offer");

            c.askYesNo("book");

          } else c.sayKey("npc.adriana.book.missing");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"book".equals(state)) return false;

        if (yes && c.hasItem("book_of_warfare")) {

          c.takeItem("book_of_warfare");

          c.giveItem("blade_of_sharpness");

          c.sayKey("npc.adriana.book.yes");

        } else if (yes) c.sayKey("npc.adriana.book.gone");
        else c.sayKey("npc.adriana.book.no");

        return true;
      }
    };
  }

  public Adriana(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
