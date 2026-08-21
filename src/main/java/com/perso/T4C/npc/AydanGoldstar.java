package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "AydanGoldstar", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class AydanGoldstar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "AydanGoldstar";

  public static final String DISPLAY_NAME = "${npc.aydangoldstar}";

  public static final String SPRITE_BASE = "@invisible";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.aydangoldstar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aydangoldstar.0.0}",
                      "${npc.topic_keyword.aydangoldstar.0.1}"),
                  null,
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aydangoldstar.1.0}",
                      "${npc.topic_keyword.aydangoldstar.1.1}",
                      "${npc.topic_keyword.aydangoldstar.1.2}"),
                  null,
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.aydangoldstar.2.0}",
                      "${npc.topic_keyword.aydangoldstar.2.1}",
                      "${npc.topic_keyword.aydangoldstar.2.2}",
                      "${npc.topic_keyword.aydangoldstar.2.3}"),
                  null,
                  List.of())),
          "AydanGoldstarNPC",
          new NpcSpec.CombatProfile(1, 1, 10, 10, 10, 0, 0, 0, "1d3"));

  public AydanGoldstar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }
}
