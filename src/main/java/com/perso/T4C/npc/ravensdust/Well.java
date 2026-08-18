package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class Well extends ScriptedNpc {

  public static final String ID = "Well";

  public static final String DISPLAY_NAME = "${npc.well}";

  public static final String SPRITE_BASE = "@static:DungeonWell";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.well}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.well.0.0}"), "${npc.topic.well.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.well.1.0}"), "${npc.topic.well.1}", List.of())),
          "WellNPC",
          new NpcSpec.CombatProfile(200, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.STATIONARY;
  }

  public Well(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
