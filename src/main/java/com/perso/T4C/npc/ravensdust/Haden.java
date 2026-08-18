package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;

@Spawn(type = "Haden", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "HADEN",
    x = 87,
    y = 2654,
    z = 0,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class Haden extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Haden";

  public static final String DISPLAY_NAME = "${npc.haden}";

  public static final String SPRITE_BASE = "Thief";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.haden}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.0.0}"), "${npc.topic.haden.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.1.0}"), "${npc.topic.haden.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.2.0}"), "${npc.topic.haden.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.3.0}"), "${npc.topic.haden.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.4.0}"), "${npc.topic.haden.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.5.0}"), "${npc.topic.haden.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.6.0}", "${npc.topic_keyword.haden.6.1}"),
                  "${npc.topic.haden.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.haden.7.0}"), "${npc.topic.haden.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.haden.8.0}",
                      "${npc.topic_keyword.haden.8.1}",
                      "${npc.topic_keyword.haden.8.2}"),
                  "${npc.topic.haden.8}",
                  List.of())),
          "HadenNPC",
          new NpcSpec.CombatProfile(55, 1321, 70, 64, 64, 15, 670, 290, "1d81+62"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onDeath(NpcBehaviorContext c) {

        if (c.flag("QUEST_VISITOR_SPOTTED") == 7 && c.flag("FLAG_KILLED_HADEN") == 0) {

          c.flag("FLAG_KILLED_HADEN", 1);

          c.flag("QUEST_VISITOR_SPOTTED", 8);

          if (c.itemCount("blade_of_ruin") == 0) c.giveItem("blade_of_ruin");
        }
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (c.flag("__FLAG_KILLED_HADEN") == 1) {

          c.sayKey("npc.haden.killed");

          c.npc().provoke();

        } else c.sayKey("npc.haden.welcome");
      }
    };
  }

  public Haden(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
