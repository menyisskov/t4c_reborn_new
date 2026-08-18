package com.perso.T4C.npc.classic;

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

@Spawn(type = "ProfessionTrainer", x = 2500, y = 2500, z = 0, stationary = true, aggressive = false)
public final class ProfessionTrainer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ProfessionTrainer";

  public static final String DISPLAY_NAME = "${npc.professiontrainer}";

  public static final String SPRITE_BASE = "@invisible";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.professiontrainer}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.professiontrainer.0.0}",
                      "${npc.topic_keyword.professiontrainer.0.1}"),
                  "${npc.topic.professiontrainer.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.professiontrainer.1.0}",
                      "${npc.topic_keyword.professiontrainer.1.1}",
                      "${npc.topic_keyword.professiontrainer.1.2}",
                      "${npc.topic_keyword.professiontrainer.1.3}",
                      "${npc.topic_keyword.professiontrainer.1.4}"),
                  "${npc.topic.professiontrainer.1}",
                  List.of())),
          "ProfessionTrainerNPC",
          new NpcSpec.CombatProfile(1, 1, 10, 10, 10, 0, 0, 0, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public ProfessionTrainer(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
