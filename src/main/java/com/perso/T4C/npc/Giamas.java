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

@Spawn(type = "Giamas", x = 1548, y = 2399, z = 0, stationary = false, aggressive = false)
public final class Giamas extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Giamas";

  public static final String DISPLAY_NAME = "${npc.giamas}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupFlail")),
          0,
          List.of(),
          "${npc.welcome.giamas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.0.0}"), "${npc.topic.giamas.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.1.0}"), "${npc.topic.giamas.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.2.0}", "${npc.topic_keyword.giamas.2.1}"),
                  "${npc.topic.giamas.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.3.0}"), "${npc.topic.giamas.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.giamas.4.0}",
                      "${npc.topic_keyword.giamas.4.1}",
                      "${npc.topic_keyword.giamas.4.2}"),
                  "${npc.topic.giamas.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.5.0}", "${npc.topic_keyword.giamas.5.1}"),
                  "${npc.topic.giamas.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.6.0}", "${npc.topic_keyword.giamas.6.1}"),
                  "${npc.topic.giamas.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.giamas.7.0}"), "${npc.topic.giamas.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.giamas.8.0}",
                      "${npc.topic_keyword.giamas.8.1}",
                      "${npc.topic_keyword.giamas.8.2}"),
                  "${npc.topic.giamas.8}",
                  List.of())),
          "GiamasNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Giamas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
