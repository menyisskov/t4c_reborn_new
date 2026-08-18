package com.perso.T4C.npc.windhowl;

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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "RylethCth", x = 1604, y = 1244, z = 0, stationary = false, aggressive = false)
public final class RylethCth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RylethCth";

  public static final String DISPLAY_NAME = "${npc.rylethcth}";

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
          "${npc.welcome.rylethcth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rylethcth.0.0}", "${npc.topic_keyword.rylethcth.0.1}"),
                  "${npc.topic.rylethcth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rylethcth.1.0}",
                      "${npc.topic_keyword.rylethcth.1.1}",
                      "${npc.topic_keyword.rylethcth.1.2}"),
                  "${npc.topic.rylethcth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rylethcth.2.0}"),
                  "${npc.topic.rylethcth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rylethcth.3.0}",
                      "${npc.topic_keyword.rylethcth.3.1}",
                      "${npc.topic_keyword.rylethcth.3.2}"),
                  "${npc.topic.rylethcth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rylethcth.4.0}"),
                  "${npc.topic.rylethcth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rylethcth.5.0}"),
                  "${npc.topic.rylethcth.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rylethcth.6.0}"),
                  "${npc.topic.rylethcth.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rylethcth.7.0}"),
                  "${npc.topic.rylethcth.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rylethcth.8.0}",
                      "${npc.topic_keyword.rylethcth.8.1}",
                      "${npc.topic_keyword.rylethcth.8.2}",
                      "${npc.topic_keyword.rylethcth.8.3}",
                      "${npc.topic_keyword.rylethcth.8.4}"),
                  "${npc.topic.rylethcth.8}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public RylethCth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
