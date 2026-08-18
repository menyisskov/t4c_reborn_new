package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Scholar1 extends ScriptedNpc {

  public static final String ID = "Scholar1";

  public static final String DISPLAY_NAME = "${npc.scholar1}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.scholar1}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.scholar1.0.0}"),
                  "${npc.topic.scholar1.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.scholar1.1.0}",
                      "${npc.topic_keyword.scholar1.1.1}",
                      "${npc.topic_keyword.scholar1.1.2}"),
                  "${npc.topic.scholar1.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.scholar1.2.0}",
                      "${npc.topic_keyword.scholar1.2.1}",
                      "${npc.topic_keyword.scholar1.2.2}",
                      "${npc.topic_keyword.scholar1.2.3}",
                      "${npc.topic_keyword.scholar1.2.4}"),
                  "${npc.topic.scholar1.2}",
                  List.of())),
          "HighPriestGuntharNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }

  public Scholar1(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
