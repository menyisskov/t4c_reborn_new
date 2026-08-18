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

public final class CorsairMagere extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "CorsairMagere";

  public static final String DISPLAY_NAME = "${npc.corsairmagere}";

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
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.corsairmagere}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.corsairmagere.0.0}",
                      "${npc.topic_keyword.corsairmagere.0.1}"),
                  "${npc.topic.corsairmagere.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.1.0}"),
                  "${npc.topic.corsairmagere.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.2.0}"),
                  "${npc.topic.corsairmagere.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.3.0}"),
                  "${npc.topic.corsairmagere.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.corsairmagere.4.0}",
                      "${npc.topic_keyword.corsairmagere.4.1}",
                      "${npc.topic_keyword.corsairmagere.4.2}"),
                  "${npc.topic.corsairmagere.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.5.0}"),
                  "${npc.topic.corsairmagere.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.corsairmagere.6.0}",
                      "${npc.topic_keyword.corsairmagere.6.1}"),
                  "${npc.topic.corsairmagere.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.7.0}"),
                  "${npc.topic.corsairmagere.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.8.0}"),
                  "${npc.topic.corsairmagere.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.9.0}"),
                  "${npc.topic.corsairmagere.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.corsairmagere.10.0}",
                      "${npc.topic_keyword.corsairmagere.10.1}"),
                  "${npc.topic.corsairmagere.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.11.0}"),
                  "${npc.topic.corsairmagere.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.12.0}"),
                  "${npc.topic.corsairmagere.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.13.0}"),
                  "${npc.topic.corsairmagere.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.corsairmagere.14.0}"),
                  "${npc.topic.corsairmagere.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.corsairmagere.15.0}",
                      "${npc.topic_keyword.corsairmagere.15.1}",
                      "${npc.topic_keyword.corsairmagere.15.2}",
                      "${npc.topic_keyword.corsairmagere.15.3}"),
                  "${npc.topic.corsairmagere.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.corsairmagere.16.0}",
                      "${npc.topic_keyword.corsairmagere.16.1}",
                      "${npc.topic_keyword.corsairmagere.16.2}",
                      "${npc.topic_keyword.corsairmagere.16.3}",
                      "${npc.topic_keyword.corsairmagere.16.4}"),
                  "${npc.topic.corsairmagere.16}",
                  List.of())),
          "CorsairTheowaldNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public CorsairMagere(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
