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

public final class Torgas extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Torgas";

  public static final String DISPLAY_NAME = "${npc.torgas}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.torgas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.torgas.0.0}", "${npc.topic_keyword.torgas.0.1}"),
                  "${npc.topic.torgas.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.torgas.1.0}"), "${npc.topic.torgas.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.torgas.2.0}",
                      "${npc.topic_keyword.torgas.2.1}",
                      "${npc.topic_keyword.torgas.2.2}"),
                  "${npc.topic.torgas.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.torgas.3.0}", "${npc.topic_keyword.torgas.3.1}"),
                  "${npc.topic.torgas.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.torgas.4.0}"), "${npc.topic.torgas.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.torgas.5.0}", "${npc.topic_keyword.torgas.5.1}"),
                  "${npc.topic.torgas.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.torgas.6.0}",
                      "${npc.topic_keyword.torgas.6.1}",
                      "${npc.topic_keyword.torgas.6.2}",
                      "${npc.topic_keyword.torgas.6.3}"),
                  "${npc.topic.torgas.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.torgas.7.0}",
                      "${npc.topic_keyword.torgas.7.1}",
                      "${npc.topic_keyword.torgas.7.2}",
                      "${npc.topic_keyword.torgas.7.3}"),
                  "${npc.topic.torgas.7}",
                  List.of())),
          "TorgasNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text != null && text.toUpperCase(java.util.Locale.ROOT).equals("ORACLE")) {

          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") >= 1
                  ? "npc.torgas.oracle.good"
                  : "npc.torgas.oracle.bad");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Torgas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
