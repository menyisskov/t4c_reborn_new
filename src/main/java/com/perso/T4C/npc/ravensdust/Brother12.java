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

@Spawn(type = "Brother12", x = 680, y = 2390, z = 1, stationary = false, aggressive = false)
public final class Brother12 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String ID = "Brother12";

  public static final String DISPLAY_NAME = "${npc.brother12}";

  public static final String SPRITE_BASE = "Zombie";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.brother12}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother12.0.0}"),
                  "${npc.topic.brother12.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother12.1.0}"),
                  "${npc.topic.brother12.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.2.0}",
                      "${npc.topic_keyword.brother12.2.1}",
                      "${npc.topic_keyword.brother12.2.2}",
                      "${npc.topic_keyword.brother12.2.3}",
                      "${npc.topic_keyword.brother12.2.4}"),
                  "${npc.topic.brother12.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.3.0}",
                      "${npc.topic_keyword.brother12.3.1}",
                      "${npc.topic_keyword.brother12.3.2}",
                      "${npc.topic_keyword.brother12.3.3}",
                      "${npc.topic_keyword.brother12.3.4}"),
                  "${npc.topic.brother12.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.4.0}", "${npc.topic_keyword.brother12.4.1}"),
                  "${npc.topic.brother12.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.5.0}", "${npc.topic_keyword.brother12.5.1}"),
                  "${npc.topic.brother12.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.6.0}", "${npc.topic_keyword.brother12.6.1}"),
                  "${npc.topic.brother12.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother12.7.0}"),
                  "${npc.topic.brother12.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother12.8.0}"),
                  "${npc.topic.brother12.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.9.0}", "${npc.topic_keyword.brother12.9.1}"),
                  "${npc.topic.brother12.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother12.10.0}"),
                  "${npc.topic.brother12.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.brother12.11.0}"),
                  "${npc.topic.brother12.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.12.0}",
                      "${npc.topic_keyword.brother12.12.1}",
                      "${npc.topic_keyword.brother12.12.2}",
                      "${npc.topic_keyword.brother12.12.3}"),
                  "${npc.topic.brother12.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.13.0}", "${npc.topic_keyword.brother12.13.1}"),
                  "${npc.topic.brother12.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.brother12.14.0}",
                      "${npc.topic_keyword.brother12.14.1}",
                      "${npc.topic_keyword.brother12.14.2}",
                      "${npc.topic_keyword.brother12.14.3}",
                      "${npc.topic_keyword.brother12.14.4}"),
                  "${npc.topic.brother12.14}",
                  List.of())),
          "DeadBrotherNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new DeadBrotherBehavior(12);
  }

  public Brother12(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
