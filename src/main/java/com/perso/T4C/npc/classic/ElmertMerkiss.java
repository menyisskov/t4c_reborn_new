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

public final class ElmertMerkiss extends ScriptedNpc {

  public static final String ID = "ElmertMerkiss";

  public static final String DISPLAY_NAME = "${npc.elmertmerkiss}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupRomanShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.elmertmerkiss}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.0.0}"),
                  "${npc.topic.elmertmerkiss.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.1.0}"),
                  "${npc.topic.elmertmerkiss.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elmertmerkiss.2.0}",
                      "${npc.topic_keyword.elmertmerkiss.2.1}",
                      "${npc.topic_keyword.elmertmerkiss.2.2}"),
                  "${npc.topic.elmertmerkiss.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elmertmerkiss.3.0}",
                      "${npc.topic_keyword.elmertmerkiss.3.1}"),
                  "${npc.topic.elmertmerkiss.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.4.0}"),
                  "${npc.topic.elmertmerkiss.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.5.0}"),
                  "${npc.topic.elmertmerkiss.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.6.0}"),
                  "${npc.topic.elmertmerkiss.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.7.0}"),
                  "${npc.topic.elmertmerkiss.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.8.0}"),
                  "${npc.topic.elmertmerkiss.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.9.0}"),
                  "${npc.topic.elmertmerkiss.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.10.0}"),
                  "${npc.topic.elmertmerkiss.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.11.0}"),
                  "${npc.topic.elmertmerkiss.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.12.0}"),
                  "${npc.topic.elmertmerkiss.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.13.0}"),
                  "${npc.topic.elmertmerkiss.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.14.0}"),
                  "${npc.topic.elmertmerkiss.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elmertmerkiss.15.0}"),
                  "${npc.topic.elmertmerkiss.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elmertmerkiss.16.0}",
                      "${npc.topic_keyword.elmertmerkiss.16.1}",
                      "${npc.topic_keyword.elmertmerkiss.16.2}",
                      "${npc.topic_keyword.elmertmerkiss.16.3}"),
                  "${npc.topic.elmertmerkiss.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elmertmerkiss.17.0}",
                      "${npc.topic_keyword.elmertmerkiss.17.1}",
                      "${npc.topic_keyword.elmertmerkiss.17.2}",
                      "${npc.topic_keyword.elmertmerkiss.17.3}",
                      "${npc.topic_keyword.elmertmerkiss.17.4}"),
                  "${npc.topic.elmertmerkiss.17}",
                  List.of())),
          "Guard_One",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public ElmertMerkiss(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
