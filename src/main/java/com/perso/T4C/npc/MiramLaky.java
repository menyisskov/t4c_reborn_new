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

@Spawn(type = "MiramLaky", x = 1741, y = 1306, z = 0, stationary = false, aggressive = false)
public final class MiramLaky extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MiramLaky";

  public static final String DISPLAY_NAME = "${npc.miramlaky}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.miramlaky}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miramlaky.0.0}", "${npc.topic_keyword.miramlaky.0.1}"),
                  "${npc.topic.miramlaky.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miramlaky.1.0}"),
                  "${npc.topic.miramlaky.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miramlaky.2.0}",
                      "${npc.topic_keyword.miramlaky.2.1}",
                      "${npc.topic_keyword.miramlaky.2.2}"),
                  "${npc.topic.miramlaky.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miramlaky.3.0}"),
                  "${npc.topic.miramlaky.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miramlaky.4.0}",
                      "${npc.topic_keyword.miramlaky.4.1}",
                      "${npc.topic_keyword.miramlaky.4.2}"),
                  "${npc.topic.miramlaky.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miramlaky.5.0}"),
                  "${npc.topic.miramlaky.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miramlaky.6.0}"),
                  "${npc.topic.miramlaky.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.miramlaky.7.0}"),
                  "${npc.topic.miramlaky.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.miramlaky.8.0}",
                      "${npc.topic_keyword.miramlaky.8.1}",
                      "${npc.topic_keyword.miramlaky.8.2}",
                      "${npc.topic_keyword.miramlaky.8.3}",
                      "${npc.topic_keyword.miramlaky.8.4}"),
                  "${npc.topic.miramlaky.8}",
                  List.of())),
          "MiramLakyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public MiramLaky(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
