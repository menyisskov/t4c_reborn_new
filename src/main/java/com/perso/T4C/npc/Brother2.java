package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Brother2", x = 465, y = 2460, z = 1, stationary = false, aggressive = false)
public final class Brother2 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String ID = "Brother2";

  public static final String DISPLAY_NAME = "${npc.brother2}";

  public static final String SPRITE_BASE = "Zombie";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.brother2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother2.0.0}"),
                  "${npc.topic.brother2.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother2.1.0}",
                      "${npc.topic_keyword.brother2.1.1}",
                      "${npc.topic_keyword.brother2.1.2}",
                      "${npc.topic_keyword.brother2.1.3}",
                      "${npc.topic_keyword.brother2.1.4}"),
                  "${npc.topic.brother2.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother2.2.0}",
                      "${npc.topic_keyword.brother2.2.1}",
                      "${npc.topic_keyword.brother2.2.2}",
                      "${npc.topic_keyword.brother2.2.3}",
                      "${npc.topic_keyword.brother2.2.4}"),
                  "${npc.topic.brother2.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother2.3.0}", "${npc.topic_keyword.brother2.3.1}"),
                  "${npc.topic.brother2.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother2.4.0}", "${npc.topic_keyword.brother2.4.1}"),
                  "${npc.topic.brother2.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother2.5.0}", "${npc.topic_keyword.brother2.5.1}"),
                  "${npc.topic.brother2.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother2.6.0}",
                      "${npc.topic_keyword.brother2.6.1}",
                      "${npc.topic_keyword.brother2.6.2}",
                      "${npc.topic_keyword.brother2.6.3}"),
                  "${npc.topic.brother2.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother2.7.0}",
                      "${npc.topic_keyword.brother2.7.1}",
                      "${npc.topic_keyword.brother2.7.2}",
                      "${npc.topic_keyword.brother2.7.3}",
                      "${npc.topic_keyword.brother2.7.4}"),
                  "${npc.topic.brother2.7}",
                  List.of())),
          "DeadBrotherNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new DeadBrotherBehavior(2);
  }

  public Brother2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
