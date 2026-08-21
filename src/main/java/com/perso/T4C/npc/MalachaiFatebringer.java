package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "MalachaiFatebringer",
    x = 900,
    y = 1110,
    z = 0,
    stationary = false,
    aggressive = false)
public final class MalachaiFatebringer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MalachaiFatebringer";

  public static final String DISPLAY_NAME = "${npc.malachaifatebringer}";

  public static final String SPRITE_BASE = "64kCentaurArcher";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.malachaifatebringer}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.0.0}",
                      "${npc.topic_keyword.malachaifatebringer.0.1}"),
                  "${npc.topic.malachaifatebringer.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.1.0}",
                      "${npc.topic_keyword.malachaifatebringer.1.1}",
                      "${npc.topic_keyword.malachaifatebringer.1.2}"),
                  "${npc.topic.malachaifatebringer.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.2.0}",
                      "${npc.topic_keyword.malachaifatebringer.2.1}"),
                  "${npc.topic.malachaifatebringer.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.3.0}",
                      "${npc.topic_keyword.malachaifatebringer.3.1}",
                      "${npc.topic_keyword.malachaifatebringer.3.2}"),
                  "${npc.topic.malachaifatebringer.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.4.0}",
                      "${npc.topic_keyword.malachaifatebringer.4.1}"),
                  "${npc.topic.malachaifatebringer.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.5.0}",
                      "${npc.topic_keyword.malachaifatebringer.5.1}",
                      "${npc.topic_keyword.malachaifatebringer.5.2}"),
                  "${npc.topic.malachaifatebringer.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.6.0}",
                      "${npc.topic_keyword.malachaifatebringer.6.1}"),
                  "${npc.topic.malachaifatebringer.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.7.0}",
                      "${npc.topic_keyword.malachaifatebringer.7.1}",
                      "${npc.topic_keyword.malachaifatebringer.7.2}"),
                  "${npc.topic.malachaifatebringer.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.8.0}",
                      "${npc.topic_keyword.malachaifatebringer.8.1}"),
                  "${npc.topic.malachaifatebringer.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.9.0}",
                      "${npc.topic_keyword.malachaifatebringer.9.1}"),
                  "${npc.topic.malachaifatebringer.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.10.0}",
                      "${npc.topic_keyword.malachaifatebringer.10.1}"),
                  "${npc.topic.malachaifatebringer.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.11.0}",
                      "${npc.topic_keyword.malachaifatebringer.11.1}"),
                  "${npc.topic.malachaifatebringer.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.12.0}",
                      "${npc.topic_keyword.malachaifatebringer.12.1}"),
                  "${npc.topic.malachaifatebringer.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.13.0}",
                      "${npc.topic_keyword.malachaifatebringer.13.1}"),
                  "${npc.topic.malachaifatebringer.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.14.0}",
                      "${npc.topic_keyword.malachaifatebringer.14.1}",
                      "${npc.topic_keyword.malachaifatebringer.14.2}",
                      "${npc.topic_keyword.malachaifatebringer.14.3}"),
                  "${npc.topic.malachaifatebringer.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.malachaifatebringer.15.0}",
                      "${npc.topic_keyword.malachaifatebringer.15.1}",
                      "${npc.topic_keyword.malachaifatebringer.15.2}",
                      "${npc.topic_keyword.malachaifatebringer.15.3}",
                      "${npc.topic_keyword.malachaifatebringer.15.4}"),
                  "${npc.topic.malachaifatebringer.15}",
                  List.of())),
          "MalachaiFatebringerNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public MalachaiFatebringer(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
