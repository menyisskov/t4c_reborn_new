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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Eldrig", x = 340, y = 840, z = 0, stationary = false, aggressive = false)
public final class Eldrig extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Eldrig";

  public static final String DISPLAY_NAME = "${npc.eldrig}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupNormalSword"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.eldrig}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.0.0}", "${npc.topic_keyword.eldrig.0.1}"),
                  "${npc.topic.eldrig.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.1.0}"), "${npc.topic.eldrig.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.2.0}", "${npc.topic_keyword.eldrig.2.1}"),
                  "${npc.topic.eldrig.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldrig.3.0}",
                      "${npc.topic_keyword.eldrig.3.1}",
                      "${npc.topic_keyword.eldrig.3.2}"),
                  "${npc.topic.eldrig.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.4.0}", "${npc.topic_keyword.eldrig.4.1}"),
                  "${npc.topic.eldrig.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.5.0}"), "${npc.topic.eldrig.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldrig.6.0}",
                      "${npc.topic_keyword.eldrig.6.1}",
                      "${npc.topic_keyword.eldrig.6.2}"),
                  "${npc.topic.eldrig.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.7.0}", "${npc.topic_keyword.eldrig.7.1}"),
                  "${npc.topic.eldrig.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldrig.8.0}",
                      "${npc.topic_keyword.eldrig.8.1}",
                      "${npc.topic_keyword.eldrig.8.2}",
                      "${npc.topic_keyword.eldrig.8.3}"),
                  "${npc.topic.eldrig.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.eldrig.9.0}"), "${npc.topic.eldrig.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.eldrig.10.0}",
                      "${npc.topic_keyword.eldrig.10.1}",
                      "${npc.topic_keyword.eldrig.10.2}",
                      "${npc.topic_keyword.eldrig.10.3}",
                      "${npc.topic_keyword.eldrig.10.4}"),
                  "${npc.topic.eldrig.10}",
                  List.of())),
          "EldrigNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Eldrig(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
