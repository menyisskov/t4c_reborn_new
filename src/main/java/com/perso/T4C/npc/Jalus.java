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

@Spawn(type = "Jalus", x = 2838, y = 1177, z = 0, stationary = false, aggressive = false)
public final class Jalus extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Jalus";

  public static final String DISPLAY_NAME = "${npc.jalus}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.jalus}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.0.0}", "${npc.topic_keyword.jalus.0.1}"),
                  "${npc.topic.jalus.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jalus.1.0}",
                      "${npc.topic_keyword.jalus.1.1}",
                      "${npc.topic_keyword.jalus.1.2}"),
                  "${npc.topic.jalus.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.2.0}"), "${npc.topic.jalus.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.3.0}"), "${npc.topic.jalus.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.4.0}"), "${npc.topic.jalus.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.5.0}", "${npc.topic_keyword.jalus.5.1}"),
                  "${npc.topic.jalus.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.6.0}"), "${npc.topic.jalus.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.7.0}"), "${npc.topic.jalus.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jalus.8.0}",
                      "${npc.topic_keyword.jalus.8.1}",
                      "${npc.topic_keyword.jalus.8.2}"),
                  "${npc.topic.jalus.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jalus.9.0}"), "${npc.topic.jalus.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jalus.10.0}",
                      "${npc.topic_keyword.jalus.10.1}",
                      "${npc.topic_keyword.jalus.10.2}",
                      "${npc.topic_keyword.jalus.10.3}",
                      "${npc.topic_keyword.jalus.10.4}"),
                  "${npc.topic.jalus.10}",
                  List.of())),
          "JalusNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Jalus(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
