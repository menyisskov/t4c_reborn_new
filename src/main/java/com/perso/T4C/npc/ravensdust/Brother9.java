package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Brother9", x = 561, y = 2548, z = 1, stationary = false, aggressive = false)
public final class Brother9 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String ID = "Brother9";

  public static final String DISPLAY_NAME = "${npc.brother9}";

  public static final String SPRITE_BASE = "Zombie";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.brother9}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother9.0.0}"),
                  "${npc.topic.brother9.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother9.1.0}",
                      "${npc.topic_keyword.brother9.1.1}",
                      "${npc.topic_keyword.brother9.1.2}",
                      "${npc.topic_keyword.brother9.1.3}",
                      "${npc.topic_keyword.brother9.1.4}"),
                  "${npc.topic.brother9.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother9.2.0}",
                      "${npc.topic_keyword.brother9.2.1}",
                      "${npc.topic_keyword.brother9.2.2}",
                      "${npc.topic_keyword.brother9.2.3}",
                      "${npc.topic_keyword.brother9.2.4}"),
                  "${npc.topic.brother9.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother9.3.0}", "${npc.topic_keyword.brother9.3.1}"),
                  "${npc.topic.brother9.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother9.4.0}", "${npc.topic_keyword.brother9.4.1}"),
                  "${npc.topic.brother9.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother9.5.0}", "${npc.topic_keyword.brother9.5.1}"),
                  "${npc.topic.brother9.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother9.6.0}",
                      "${npc.topic_keyword.brother9.6.1}",
                      "${npc.topic_keyword.brother9.6.2}",
                      "${npc.topic_keyword.brother9.6.3}"),
                  "${npc.topic.brother9.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother9.7.0}",
                      "${npc.topic_keyword.brother9.7.1}",
                      "${npc.topic_keyword.brother9.7.2}",
                      "${npc.topic_keyword.brother9.7.3}",
                      "${npc.topic_keyword.brother9.7.4}"),
                  "${npc.topic.brother9.7}",
                  List.of())),
          "DeadBrotherNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new DeadBrotherBehavior(9);
  }

  public Brother9(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
