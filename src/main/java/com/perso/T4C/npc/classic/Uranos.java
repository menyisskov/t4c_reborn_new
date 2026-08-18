package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingAndFleeBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Uranos extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Uranos";

  public static final String DISPLAY_NAME = "${npc.uranos}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.uranos}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.0.0}", "${npc.topic_keyword.uranos.0.1}"),
                  "${npc.topic.uranos.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.uranos.1.0}",
                      "${npc.topic_keyword.uranos.1.1}",
                      "${npc.topic_keyword.uranos.1.2}"),
                  "${npc.topic.uranos.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.2.0}"), "${npc.topic.uranos.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.uranos.3.0}",
                      "${npc.topic_keyword.uranos.3.1}",
                      "${npc.topic_keyword.uranos.3.2}"),
                  "${npc.topic.uranos.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.4.0}"), "${npc.topic.uranos.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.5.0}"), "${npc.topic.uranos.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.6.0}"), "${npc.topic.uranos.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.7.0}"), "${npc.topic.uranos.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.uranos.8.0}"), "${npc.topic.uranos.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.uranos.9.0}",
                      "${npc.topic_keyword.uranos.9.1}",
                      "${npc.topic_keyword.uranos.9.2}"),
                  "${npc.topic.uranos.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.uranos.10.0}",
                      "${npc.topic_keyword.uranos.10.1}",
                      "${npc.topic_keyword.uranos.10.2}",
                      "${npc.topic_keyword.uranos.10.3}",
                      "${npc.topic_keyword.uranos.10.4}"),
                  "${npc.topic.uranos.10}",
                  List.of())),
          "UranosNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingAndFleeBehavior(
        true,
        List.of(
            new LearnScreen.TrainingOffer("stone_shard", 6, 1328, true),
            new LearnScreen.TrainingOffer("shatter", 11, 14292, true)),
        "npc.markam.attacked.");
  }

  public Uranos(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
