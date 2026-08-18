package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.PortalBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;

public final class DoppelgangerPortal7a extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DoppelgangerPortal7a";

  public static final String DISPLAY_NAME = "${npc.doppelgangerportal7a}";

  public static final String SPRITE_BASE = "@static:SimplePortal-a";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.doppelgangerportal7a}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doppelgangerportal7a.0.0}"),
                  "${npc.topic.doppelgangerportal7a.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doppelgangerportal7a.1.0}"),
                  "${npc.topic.doppelgangerportal7a.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.doppelgangerportal7a.2.0}",
                      "${npc.topic_keyword.doppelgangerportal7a.2.1}",
                      "${npc.topic_keyword.doppelgangerportal7a.2.2}",
                      "${npc.topic_keyword.doppelgangerportal7a.2.3}",
                      "${npc.topic_keyword.doppelgangerportal7a.2.4}"),
                  "${npc.topic.doppelgangerportal7a.2}",
                  List.of())),
          "PortalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public DoppelgangerPortal7a(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new PortalBehavior(2956, 1335, 1, null, "${npc.welcome.doppelgangerportal7a}");
}
