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

public final class TwinShovanis extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TwinShovanis";

  public static final String DISPLAY_NAME = "${npc.twinshovanis}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.twinshovanis}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinshovanis.0.0}",
                      "${npc.topic_keyword.twinshovanis.0.1}"),
                  "${npc.topic.twinshovanis.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinshovanis.1.0}",
                      "${npc.topic_keyword.twinshovanis.1.1}",
                      "${npc.topic_keyword.twinshovanis.1.2}",
                      "${npc.topic_keyword.twinshovanis.1.3}"),
                  "${npc.topic.twinshovanis.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.2.0}"),
                  "${npc.topic.twinshovanis.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinshovanis.3.0}",
                      "${npc.topic_keyword.twinshovanis.3.1}",
                      "${npc.topic_keyword.twinshovanis.3.2}"),
                  "${npc.topic.twinshovanis.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.4.0}"),
                  "${npc.topic.twinshovanis.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinshovanis.5.0}",
                      "${npc.topic_keyword.twinshovanis.5.1}",
                      "${npc.topic_keyword.twinshovanis.5.2}"),
                  "${npc.topic.twinshovanis.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.6.0}"),
                  "${npc.topic.twinshovanis.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.7.0}"),
                  "${npc.topic.twinshovanis.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.8.0}"),
                  "${npc.topic.twinshovanis.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.9.0}"),
                  "${npc.topic.twinshovanis.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.twinshovanis.10.0}"),
                  "${npc.topic.twinshovanis.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.twinshovanis.11.0}",
                      "${npc.topic_keyword.twinshovanis.11.1}",
                      "${npc.topic_keyword.twinshovanis.11.2}",
                      "${npc.topic_keyword.twinshovanis.11.3}",
                      "${npc.topic_keyword.twinshovanis.11.4}"),
                  "${npc.topic.twinshovanis.11}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public TwinShovanis(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
