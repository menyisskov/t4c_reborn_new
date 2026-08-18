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

public final class Guardman extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Guardman";

  public static final String DISPLAY_NAME = "${npc.guardman}";

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
          "${npc.welcome.guardman}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guardman.0.0}"),
                  "${npc.topic.guardman.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guardman.1.0}"),
                  "${npc.topic.guardman.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guardman.2.0}", "${npc.topic_keyword.guardman.2.1}"),
                  "${npc.topic.guardman.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guardman.3.0}",
                      "${npc.topic_keyword.guardman.3.1}",
                      "${npc.topic_keyword.guardman.3.2}"),
                  "${npc.topic.guardman.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.guardman.4.0}"),
                  "${npc.topic.guardman.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guardman.5.0}",
                      "${npc.topic_keyword.guardman.5.1}",
                      "${npc.topic_keyword.guardman.5.2}"),
                  "${npc.topic.guardman.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guardman.6.0}",
                      "${npc.topic_keyword.guardman.6.1}",
                      "${npc.topic_keyword.guardman.6.2}",
                      "${npc.topic_keyword.guardman.6.3}"),
                  "${npc.topic.guardman.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.guardman.7.0}",
                      "${npc.topic_keyword.guardman.7.1}",
                      "${npc.topic_keyword.guardman.7.2}",
                      "${npc.topic_keyword.guardman.7.3}",
                      "${npc.topic_keyword.guardman.7.4}"),
                  "${npc.topic.guardman.7}",
                  List.of())),
          "Guard_One",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  public Guardman(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }
}
