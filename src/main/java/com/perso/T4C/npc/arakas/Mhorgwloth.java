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

@Spawn(type = "Mhorgwloth", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "MHORGWLOTH",
    x = 1566,
    y = 174,
    z = 0,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class Mhorgwloth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Troll Attack.wav";
  public static final String SOUND_DEATH = "Troll Dying.wav";
  public static final String SOUND_HIT = "Troll Hit.wav";

  public static final String ID = "Mhorgwloth";

  public static final String DISPLAY_NAME = "${npc.mhorgwloth}";

  public static final String SPRITE_BASE = "Troll";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.mhorgwloth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mhorgwloth.0.0}"),
                  "${npc.topic.mhorgwloth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.1.0}",
                      "${npc.topic_keyword.mhorgwloth.1.1}",
                      "${npc.topic_keyword.mhorgwloth.1.2}"),
                  "${npc.topic.mhorgwloth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.2.0}",
                      "${npc.topic_keyword.mhorgwloth.2.1}",
                      "${npc.topic_keyword.mhorgwloth.2.2}"),
                  "${npc.topic.mhorgwloth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mhorgwloth.3.0}"),
                  "${npc.topic.mhorgwloth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mhorgwloth.4.0}"),
                  "${npc.topic.mhorgwloth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.5.0}", "${npc.topic_keyword.mhorgwloth.5.1}"),
                  "${npc.topic.mhorgwloth.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mhorgwloth.6.0}"),
                  "${npc.topic.mhorgwloth.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mhorgwloth.7.0}"),
                  "${npc.topic.mhorgwloth.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.8.0}",
                      "${npc.topic_keyword.mhorgwloth.8.1}",
                      "${npc.topic_keyword.mhorgwloth.8.2}"),
                  "${npc.topic.mhorgwloth.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.9.0}",
                      "${npc.topic_keyword.mhorgwloth.9.1}",
                      "${npc.topic_keyword.mhorgwloth.9.2}"),
                  "${npc.topic.mhorgwloth.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.10.0}",
                      "${npc.topic_keyword.mhorgwloth.10.1}",
                      "${npc.topic_keyword.mhorgwloth.10.2}",
                      "${npc.topic_keyword.mhorgwloth.10.3}",
                      "${npc.topic_keyword.mhorgwloth.10.4}"),
                  "${npc.topic.mhorgwloth.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mhorgwloth.11.0}"),
                  "${npc.topic.mhorgwloth.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mhorgwloth.12.0}",
                      "${npc.topic_keyword.mhorgwloth.12.1}"),
                  "${npc.topic.mhorgwloth.12}",
                  List.of())),
          "_Mhorgwloth",
          new NpcSpec.CombatProfile(30, 581, 45, 41, 41, 15, 370, 130, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("STONE OF LIFE")) {

          if (c.globalFlag("QUEST_STONE_OF_LIFE") > 1
              && c.globalFlag("WHO_HAS_THE_STONE_FLAG") == 2
              && c.globalFlag("TROLL_STONE_OF_LIFE") != 5) {

            c.askYesNo("diamond");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"diamond".equals(state)) return false;

        if (yes) {

          if (c.hasItem("diamond")) {

            c.globalFlag("TROLL_STONE_OF_LIFE", 5);

            c.takeItem("diamond");

            c.giveItem("stone_of_life");

          } else c.npc().provoke();

        } else c.npc().provoke();

        return true;
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.globalFlag("TROLL_QUEST_DONE") == 1) c.globalFlag("TROLL_QUEST_DONE", 2);

        int access = c.flag("__QUEST_ISLAND_ACCESS");

        if (c.karma() >= -100 * (access + 1)) c.karma(c.karma() - 3 * (500 + c.karma()) / 500);
      }
    };
  }

  public Mhorgwloth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
