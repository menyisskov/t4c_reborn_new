package com.perso.T4C.npc.stoneheim;

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

@Spawn(type = "ChiefWuuthgoran", x = 1230, y = 420, z = 0, stationary = false, aggressive = false)
public final class ChiefWuuthgoran extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ChiefWuuthgoran";

  public static final String DISPLAY_NAME = "${npc.chiefwuuthgoran}";

  public static final String SPRITE_BASE = "64kSkavenPeon";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.chiefwuuthgoran}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chiefwuuthgoran.0.0}",
                      "${npc.topic_keyword.chiefwuuthgoran.0.1}"),
                  "${npc.topic.chiefwuuthgoran.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chiefwuuthgoran.1.0}",
                      "${npc.topic_keyword.chiefwuuthgoran.1.1}",
                      "${npc.topic_keyword.chiefwuuthgoran.1.2}"),
                  "${npc.topic.chiefwuuthgoran.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.chiefwuuthgoran.2.0}"),
                  "${npc.topic.chiefwuuthgoran.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.chiefwuuthgoran.3.0}",
                      "${npc.topic_keyword.chiefwuuthgoran.3.1}",
                      "${npc.topic_keyword.chiefwuuthgoran.3.2}",
                      "${npc.topic_keyword.chiefwuuthgoran.3.3}",
                      "${npc.topic_keyword.chiefwuuthgoran.3.4}"),
                  "${npc.topic.chiefwuuthgoran.3}",
                  List.of())),
          "ChiefWuuthgoranNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  public ChiefWuuthgoran(NpcContext context) throws GameException {

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
