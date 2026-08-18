package com.perso.T4C.npc.arakas;

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
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;

@Spawn(type = "RenegadeOrcLeader", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "RENEGADEORCLEADER",
    x = 1076,
    y = 276,
    z = 1,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class RenegadeOrcLeader extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public static final String ID = "RenegadeOrcLeader";

  public static final String DISPLAY_NAME = "${npc.renegadeorcleader}";

  public static final String SPRITE_BASE = "Orc";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.renegadeorcleader}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.0.0}"),
                  "${npc.topic.renegadeorcleader.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.1.0}"),
                  "${npc.topic.renegadeorcleader.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.2.0}"),
                  "${npc.topic.renegadeorcleader.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.renegadeorcleader.3.0}",
                      "${npc.topic_keyword.renegadeorcleader.3.1}"),
                  "${npc.topic.renegadeorcleader.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.4.0}"),
                  "${npc.topic.renegadeorcleader.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.5.0}"),
                  "${npc.topic.renegadeorcleader.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.6.0}"),
                  "${npc.topic.renegadeorcleader.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.7.0}"),
                  "${npc.topic.renegadeorcleader.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.8.0}"),
                  "${npc.topic.renegadeorcleader.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.renegadeorcleader.9.0}",
                      "${npc.topic_keyword.renegadeorcleader.9.1}"),
                  "${npc.topic.renegadeorcleader.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.renegadeorcleader.10.0}",
                      "${npc.topic_keyword.renegadeorcleader.10.1}"),
                  "${npc.topic.renegadeorcleader.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.renegadeorcleader.11.0}"),
                  "${npc.topic.renegadeorcleader.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.renegadeorcleader.12.0}",
                      "${npc.topic_keyword.renegadeorcleader.12.1}"),
                  "${npc.topic.renegadeorcleader.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.renegadeorcleader.13.0}",
                      "${npc.topic_keyword.renegadeorcleader.13.1}",
                      "${npc.topic_keyword.renegadeorcleader.13.2}"),
                  "${npc.topic.renegadeorcleader.13}",
                  List.of())),
          "_RenegadeOrcLeader",
          new NpcSpec.CombatProfile(25, 926, 40, 37, 37, 12, 310, 110, "1d29+21"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SCRIBBLE-PAPER")) {

          c.askYesNo("givescroll");

          return true;
        }

        if (k.contains("DROP SCROLL") || k.contains("GIVE SCROLL")) {

          if (c.flag("__QUEST_KALASTOR_MISSION") == 1 && c.player().getLevel() >= 25)
            c.askYesNo("darksword");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("givescroll".equals(state)) {

          if (yes && c.flag("__QUEST_KALASTOR_MISSION") == 1 && c.player().getLevel() >= 25)
            c.askYesNo("darksword");

          return true;
        }

        if ("darksword".equals(state)) {

          if (yes) {

            if (c.hasItem("gloomblade")) {

              c.takeItem("gloomblade");

              c.giveItem("scroll_of_silversky");

              if (c.flag("__QUEST_ISLAND_ACCESS") == 0) c.flag("__QUEST_ISLAND_ACCESS", 1);

            } else c.npc().provoke();
          }

          return true;
        }

        return false;
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int access = c.flag("QUEST_ISLAND_ACCESS");

        if (c.karma() >= -100 * (access + 1)) {

          c.karma(c.karma() - (3 * (500 + c.karma()) / 500));
        }
      }
    };
  }

  public RenegadeOrcLeader(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
