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
    type = "HunterTruggWorgwloth",
    x = 1085,
    y = 405,
    z = 0,
    stationary = false,
    aggressive = false)
public final class HunterTruggWorgwloth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HunterTruggWorgwloth";

  public static final String DISPLAY_NAME = "${npc.huntertruggworgwloth}";

  public static final String SPRITE_BASE = "64kSkavenSkavenger";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.huntertruggworgwloth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntertruggworgwloth.0.0}",
                      "${npc.topic_keyword.huntertruggworgwloth.0.1}"),
                  "${npc.topic.huntertruggworgwloth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntertruggworgwloth.1.0}"),
                  "${npc.topic.huntertruggworgwloth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntertruggworgwloth.2.0}"),
                  "${npc.topic.huntertruggworgwloth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntertruggworgwloth.3.0}",
                      "${npc.topic_keyword.huntertruggworgwloth.3.1}",
                      "${npc.topic_keyword.huntertruggworgwloth.3.2}"),
                  "${npc.topic.huntertruggworgwloth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntertruggworgwloth.4.0}",
                      "${npc.topic_keyword.huntertruggworgwloth.4.1}",
                      "${npc.topic_keyword.huntertruggworgwloth.4.2}"),
                  "${npc.topic.huntertruggworgwloth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.huntertruggworgwloth.5.0}"),
                  "${npc.topic.huntertruggworgwloth.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntertruggworgwloth.6.0}",
                      "${npc.topic_keyword.huntertruggworgwloth.6.1}",
                      "${npc.topic_keyword.huntertruggworgwloth.6.2}",
                      "${npc.topic_keyword.huntertruggworgwloth.6.3}"),
                  "${npc.topic.huntertruggworgwloth.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.huntertruggworgwloth.7.0}",
                      "${npc.topic_keyword.huntertruggworgwloth.7.1}",
                      "${npc.topic_keyword.huntertruggworgwloth.7.2}",
                      "${npc.topic_keyword.huntertruggworgwloth.7.3}",
                      "${npc.topic_keyword.huntertruggworgwloth.7.4}"),
                  "${npc.topic.huntertruggworgwloth.7}",
                  List.of())),
          "HunterTruggWorgwlothNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public HunterTruggWorgwloth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
