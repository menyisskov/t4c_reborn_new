package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class JagarKar extends ScriptedNpc {

  public static final String ID = "JagarKar";

  public static final String DISPLAY_NAME = "${npc.jagarkar}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.jagarkar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.0.0}", "${npc.topic_keyword.jagarkar.0.1}"),
                  "${npc.topic.jagarkar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.1.0}"),
                  "${npc.topic.jagarkar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jagarkar.2.0}",
                      "${npc.topic_keyword.jagarkar.2.1}",
                      "${npc.topic_keyword.jagarkar.2.2}"),
                  "${npc.topic.jagarkar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.3.0}", "${npc.topic_keyword.jagarkar.3.1}"),
                  "${npc.topic.jagarkar.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.4.0}"),
                  "${npc.topic.jagarkar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.5.0}"),
                  "${npc.topic.jagarkar.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.6.0}"),
                  "${npc.topic.jagarkar.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.7.0}"),
                  "${npc.topic.jagarkar.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.8.0}"),
                  "${npc.topic.jagarkar.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jagarkar.9.0}",
                      "${npc.topic_keyword.jagarkar.9.1}",
                      "${npc.topic_keyword.jagarkar.9.2}"),
                  "${npc.topic.jagarkar.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jagarkar.10.0}"),
                  "${npc.topic.jagarkar.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jagarkar.11.0}",
                      "${npc.topic_keyword.jagarkar.11.1}",
                      "${npc.topic_keyword.jagarkar.11.2}"),
                  "${npc.topic.jagarkar.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jagarkar.12.0}",
                      "${npc.topic_keyword.jagarkar.12.1}",
                      "${npc.topic_keyword.jagarkar.12.2}",
                      "${npc.topic_keyword.jagarkar.12.3}",
                      "${npc.topic_keyword.jagarkar.12.4}"),
                  "${npc.topic.jagarkar.12}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingBehavior(
        true,
        List.of(
            new LearnScreen.TrainingOffer("stun_blow", 1, 150, true),
            new LearnScreen.TrainingOffer("powerful_blow", 1, 2500, true)));
  }

  public JagarKar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
