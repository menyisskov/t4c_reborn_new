package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.TrainingBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Murmuntag", x = 2848, y = 1126, z = 0, stationary = false, aggressive = false)
public final class Murmuntag extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String ID = "Murmuntag";

  public static final String DISPLAY_NAME = "${npc.murmuntag}";

  public static final String SPRITE_BASE = "Orc";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.murmuntag}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.murmuntag.0.0}",
                      "${npc.topic_keyword.murmuntag.0.1}",
                      "${npc.topic_keyword.murmuntag.0.2}"),
                  "${npc.topic.murmuntag.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.murmuntag.1.0}",
                      "${npc.topic_keyword.murmuntag.1.1}",
                      "${npc.topic_keyword.murmuntag.1.2}"),
                  "${npc.topic.murmuntag.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.murmuntag.2.0}"),
                  "${npc.topic.murmuntag.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.murmuntag.3.0}"),
                  "${npc.topic.murmuntag.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.murmuntag.4.0}",
                      "${npc.topic_keyword.murmuntag.4.1}",
                      "${npc.topic_keyword.murmuntag.4.2}"),
                  "${npc.topic.murmuntag.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.murmuntag.5.0}"),
                  "${npc.topic.murmuntag.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.murmuntag.6.0}"),
                  "${npc.topic.murmuntag.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.murmuntag.7.0}"),
                  "${npc.topic.murmuntag.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.murmuntag.8.0}"),
                  "${npc.topic.murmuntag.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.murmuntag.9.0}",
                      "${npc.topic_keyword.murmuntag.9.1}",
                      "${npc.topic_keyword.murmuntag.9.2}",
                      "${npc.topic_keyword.murmuntag.9.3}",
                      "${npc.topic_keyword.murmuntag.9.4}"),
                  "${npc.topic.murmuntag.9}",
                  List.of())),
          "MurmuntagDestroyNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new TrainingBehavior(
        false, List.of(new LearnScreen.TrainingOffer("attack", 5000, 10, false)));
  }

  public Murmuntag(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
