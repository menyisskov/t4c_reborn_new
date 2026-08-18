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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "KyathosShatterskull",
    x = 875,
    y = 1010,
    z = 0,
    stationary = false,
    aggressive = false)
public final class KyathosShatterskull extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "KyathosShatterskull";

  public static final String DISPLAY_NAME = "${npc.kyathosshatterskull}";

  public static final String SPRITE_BASE = "64kCentaurKing";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.kyathosshatterskull}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.0.0}",
                      "${npc.topic_keyword.kyathosshatterskull.0.1}"),
                  "${npc.topic.kyathosshatterskull.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.1.0}",
                      "${npc.topic_keyword.kyathosshatterskull.1.1}",
                      "${npc.topic_keyword.kyathosshatterskull.1.2}"),
                  "${npc.topic.kyathosshatterskull.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.2.0}",
                      "${npc.topic_keyword.kyathosshatterskull.2.1}"),
                  "${npc.topic.kyathosshatterskull.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.3.0}",
                      "${npc.topic_keyword.kyathosshatterskull.3.1}"),
                  "${npc.topic.kyathosshatterskull.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.4.0}",
                      "${npc.topic_keyword.kyathosshatterskull.4.1}"),
                  "${npc.topic.kyathosshatterskull.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.5.0}",
                      "${npc.topic_keyword.kyathosshatterskull.5.1}"),
                  "${npc.topic.kyathosshatterskull.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.6.0}",
                      "${npc.topic_keyword.kyathosshatterskull.6.1}"),
                  "${npc.topic.kyathosshatterskull.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.7.0}",
                      "${npc.topic_keyword.kyathosshatterskull.7.1}"),
                  "${npc.topic.kyathosshatterskull.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.8.0}",
                      "${npc.topic_keyword.kyathosshatterskull.8.1}"),
                  "${npc.topic.kyathosshatterskull.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.9.0}",
                      "${npc.topic_keyword.kyathosshatterskull.9.1}",
                      "${npc.topic_keyword.kyathosshatterskull.9.2}",
                      "${npc.topic_keyword.kyathosshatterskull.9.3}"),
                  "${npc.topic.kyathosshatterskull.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kyathosshatterskull.10.0}",
                      "${npc.topic_keyword.kyathosshatterskull.10.1}",
                      "${npc.topic_keyword.kyathosshatterskull.10.2}",
                      "${npc.topic_keyword.kyathosshatterskull.10.3}",
                      "${npc.topic_keyword.kyathosshatterskull.10.4}"),
                  "${npc.topic.kyathosshatterskull.10}",
                  List.of())),
          "KyathosShatterskullNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public KyathosShatterskull(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
