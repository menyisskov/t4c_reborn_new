package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ArganorIargh", x = 1689, y = 1232, z = 0, stationary = false, aggressive = false)
public final class ArganorIargh extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ArganorIargh";

  public static final String DISPLAY_NAME = "${npc.arganoriargh}";

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
          "${npc.welcome.arganoriargh}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.arganoriargh.0.0}",
                      "${npc.topic_keyword.arganoriargh.0.1}"),
                  "${npc.topic.arganoriargh.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.arganoriargh.1.0}",
                      "${npc.topic_keyword.arganoriargh.1.1}",
                      "${npc.topic_keyword.arganoriargh.1.2}"),
                  "${npc.topic.arganoriargh.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.arganoriargh.2.0}",
                      "${npc.topic_keyword.arganoriargh.2.1}"),
                  "${npc.topic.arganoriargh.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.arganoriargh.3.0}"),
                  "${npc.topic.arganoriargh.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.arganoriargh.4.0}"),
                  "${npc.topic.arganoriargh.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.arganoriargh.5.0}"),
                  "${npc.topic.arganoriargh.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.arganoriargh.6.0}",
                      "${npc.topic_keyword.arganoriargh.6.1}",
                      "${npc.topic_keyword.arganoriargh.6.2}"),
                  "${npc.topic.arganoriargh.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.arganoriargh.7.0}",
                      "${npc.topic_keyword.arganoriargh.7.1}"),
                  "${npc.topic.arganoriargh.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.arganoriargh.8.0}",
                      "${npc.topic_keyword.arganoriargh.8.1}",
                      "${npc.topic_keyword.arganoriargh.8.2}",
                      "${npc.topic_keyword.arganoriargh.8.3}",
                      "${npc.topic_keyword.arganoriargh.8.4}"),
                  "${npc.topic.arganoriargh.8}",
                  List.of())),
          "Nobleman",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public ArganorIargh(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
