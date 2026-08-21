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
    type = "HunterGruttWorgwloth",
    x = 1140,
    y = 450,
    z = 0,
    stationary = false,
    aggressive = false)
public final class HunterGruttWorgwloth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HunterGruttWorgwloth";

  public static final String DISPLAY_NAME = "${npc.huntergruttworgwloth}";

  public static final String SPRITE_BASE = "64kSkavenSkavenger";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.huntergruttworgwloth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntergruttworgwloth.0.0}",
                      "${npc.topic_keyword.huntergruttworgwloth.0.1}"),
                  "${npc.topic.huntergruttworgwloth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntergruttworgwloth.1.0}",
                      "${npc.topic_keyword.huntergruttworgwloth.1.1}",
                      "${npc.topic_keyword.huntergruttworgwloth.1.2}"),
                  "${npc.topic.huntergruttworgwloth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntergruttworgwloth.2.0}"),
                  "${npc.topic.huntergruttworgwloth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntergruttworgwloth.3.0}"),
                  "${npc.topic.huntergruttworgwloth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntergruttworgwloth.4.0}"),
                  "${npc.topic.huntergruttworgwloth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntergruttworgwloth.5.0}"),
                  "${npc.topic.huntergruttworgwloth.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntergruttworgwloth.6.0}",
                      "${npc.topic_keyword.huntergruttworgwloth.6.1}",
                      "${npc.topic_keyword.huntergruttworgwloth.6.2}",
                      "${npc.topic_keyword.huntergruttworgwloth.6.3}"),
                  "${npc.topic.huntergruttworgwloth.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntergruttworgwloth.7.0}",
                      "${npc.topic_keyword.huntergruttworgwloth.7.1}",
                      "${npc.topic_keyword.huntergruttworgwloth.7.2}",
                      "${npc.topic_keyword.huntergruttworgwloth.7.3}",
                      "${npc.topic_keyword.huntergruttworgwloth.7.4}"),
                  "${npc.topic.huntergruttworgwloth.7}",
                  List.of())),
          "HunterGruttWorgwlothNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public HunterGruttWorgwloth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
