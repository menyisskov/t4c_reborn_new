package com.perso.T4C.npc.classic;

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

public final class TwinNevanis extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TwinNevanis";

  public static final String DISPLAY_NAME = "${npc.twinnevanis}";

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
          "${npc.welcome.twinnevanis}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinnevanis.0.0}",
                      "${npc.topic_keyword.twinnevanis.0.1}"),
                  "${npc.topic.twinnevanis.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinnevanis.1.0}",
                      "${npc.topic_keyword.twinnevanis.1.1}",
                      "${npc.topic_keyword.twinnevanis.1.2}",
                      "${npc.topic_keyword.twinnevanis.1.3}"),
                  "${npc.topic.twinnevanis.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinnevanis.2.0}",
                      "${npc.topic_keyword.twinnevanis.2.1}",
                      "${npc.topic_keyword.twinnevanis.2.2}"),
                  "${npc.topic.twinnevanis.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.3.0}"),
                  "${npc.topic.twinnevanis.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.4.0}"),
                  "${npc.topic.twinnevanis.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.5.0}"),
                  "${npc.topic.twinnevanis.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.6.0}"),
                  "${npc.topic.twinnevanis.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.7.0}"),
                  "${npc.topic.twinnevanis.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.8.0}"),
                  "${npc.topic.twinnevanis.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinnevanis.9.0}"),
                  "${npc.topic.twinnevanis.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinnevanis.10.0}",
                      "${npc.topic_keyword.twinnevanis.10.1}",
                      "${npc.topic_keyword.twinnevanis.10.2}",
                      "${npc.topic_keyword.twinnevanis.10.3}",
                      "${npc.topic_keyword.twinnevanis.10.4}"),
                  "${npc.topic.twinnevanis.10}",
                  List.of())),
          "Priest",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public TwinNevanis(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
