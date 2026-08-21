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

@Spawn(type = "SkipperRedBeard", x = 1570, y = 1226, z = 0, stationary = false, aggressive = false)
public final class SkipperRedBeard extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SkipperRedBeard";

  public static final String DISPLAY_NAME = "${npc.skipperredbeard}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.skipperredbeard}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skipperredbeard.0.0}",
                      "${npc.topic_keyword.skipperredbeard.0.1}"),
                  "${npc.topic.skipperredbeard.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skipperredbeard.1.0}",
                      "${npc.topic_keyword.skipperredbeard.1.1}",
                      "${npc.topic_keyword.skipperredbeard.1.2}"),
                  "${npc.topic.skipperredbeard.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skipperredbeard.2.0}",
                      "${npc.topic_keyword.skipperredbeard.2.1}",
                      "${npc.topic_keyword.skipperredbeard.2.2}"),
                  "${npc.topic.skipperredbeard.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.skipperredbeard.3.0}"),
                  "${npc.topic.skipperredbeard.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.skipperredbeard.4.0}"),
                  "${npc.topic.skipperredbeard.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.skipperredbeard.5.0}"),
                  "${npc.topic.skipperredbeard.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.skipperredbeard.6.0}"),
                  "${npc.topic.skipperredbeard.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skipperredbeard.7.0}",
                      "${npc.topic_keyword.skipperredbeard.7.1}",
                      "${npc.topic_keyword.skipperredbeard.7.2}",
                      "${npc.topic_keyword.skipperredbeard.7.3}",
                      "${npc.topic_keyword.skipperredbeard.7.4}"),
                  "${npc.topic.skipperredbeard.7}",
                  List.of())),
          "SailorRedBeard",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public SkipperRedBeard(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
