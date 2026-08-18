package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "DwarthonStoneface", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class DwarthonStoneface extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DwarthonStoneface";

  public static final String DISPLAY_NAME = "${npc.dwarthonstoneface}";

  public static final String SPRITE_BASE = "BlackWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.dwarthonstoneface}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.0.0}",
                      "${npc.topic_keyword.dwarthonstoneface.0.1}",
                      "${npc.topic_keyword.dwarthonstoneface.0.2}",
                      "${npc.topic_keyword.dwarthonstoneface.0.3}"),
                  "${npc.topic.dwarthonstoneface.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.1.0}",
                      "${npc.topic_keyword.dwarthonstoneface.1.1}"),
                  "${npc.topic.dwarthonstoneface.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.2.0}",
                      "${npc.topic_keyword.dwarthonstoneface.2.1}"),
                  "${npc.topic.dwarthonstoneface.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.3.0}",
                      "${npc.topic_keyword.dwarthonstoneface.3.1}"),
                  "${npc.topic.dwarthonstoneface.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.4.0}"),
                  "${npc.topic.dwarthonstoneface.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.5.0}"),
                  "${npc.topic.dwarthonstoneface.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.6.0}",
                      "${npc.topic_keyword.dwarthonstoneface.6.1}"),
                  "${npc.topic.dwarthonstoneface.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.7.0}"),
                  "${npc.topic.dwarthonstoneface.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.8.0}"),
                  "${npc.topic.dwarthonstoneface.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.9.0}"),
                  "${npc.topic.dwarthonstoneface.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.10.0}"),
                  "${npc.topic.dwarthonstoneface.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.11.0}"),
                  "${npc.topic.dwarthonstoneface.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.12.0}"),
                  "${npc.topic.dwarthonstoneface.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.13.0}",
                      "${npc.topic_keyword.dwarthonstoneface.13.1}"),
                  "${npc.topic.dwarthonstoneface.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.14.0}"),
                  "${npc.topic.dwarthonstoneface.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.15.0}"),
                  "${npc.topic.dwarthonstoneface.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.16.0}",
                      "${npc.topic_keyword.dwarthonstoneface.16.1}"),
                  "${npc.topic.dwarthonstoneface.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dwarthonstoneface.17.0}"),
                  "${npc.topic.dwarthonstoneface.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.18.0}",
                      "${npc.topic_keyword.dwarthonstoneface.18.1}"),
                  "${npc.topic.dwarthonstoneface.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.19.0}",
                      "${npc.topic_keyword.dwarthonstoneface.19.1}",
                      "${npc.topic_keyword.dwarthonstoneface.19.2}",
                      "${npc.topic_keyword.dwarthonstoneface.19.3}",
                      "${npc.topic_keyword.dwarthonstoneface.19.4}"),
                  "${npc.topic.dwarthonstoneface.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dwarthonstoneface.20.0}",
                      "${npc.topic_keyword.dwarthonstoneface.20.1}",
                      "${npc.topic_keyword.dwarthonstoneface.20.2}",
                      "${npc.topic_keyword.dwarthonstoneface.20.3}"),
                  "${npc.topic.dwarthonstoneface.20}",
                  List.of())),
          "DwarthonStonefaceNPC",
          new NpcSpec.CombatProfile(45, 1992, 60, 55, 55, 22, 550, 190, "1d64+49"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_BLACKBLOOD_WANTS_YOU") >= 1) {

          c.sayKey("${npc.topic.dwarthonstoneface.1}");

          c.flag("__FLAG_BLACKBLOOD_WANTS_YOU", c.flag("__FLAG_BLACKBLOOD_WANTS_YOU") - 1);

          c.npc().provoke();
        }
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("SUCK")
            || k.equals("ASSHOLE")
            || k.trim().equals("ASS")
            || k.equals("COCK")
            || k.equals("DICK")) {

          c.sayKey("${npc.topic.dwarthonstoneface.19}");

          c.askYesNo("head");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean answer) {

        if (!"head".equals(state)) return false;

        c.sayKey(
            answer ? "${npc.topic.dwarthonstoneface.19}" : "${npc.topic.dwarthonstoneface.19}");

        c.npc().provoke();

        return true;
      }

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if ((int) (Math.random() * 30) == 0) c.shoutKey("npc.dwarthonstoneface.attack");
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = (int) (Math.random() * 30);

        if (roll == 0) c.shoutKey("npc.dwarthonstoneface.attacked.1");
        else if (roll == 1) {

          c.shoutKey("npc.dwarthonstoneface.attacked.2");

          int n = 1 + (int) (Math.random() * 3);

          for (int i = 0; i < n; i++) c.summon("Nightblade", c.npcTileX() - 5, c.npcTileY() - 5, 0);
        }
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.shoutKey("npc.dwarthonstoneface.death");
      }
    };
  }

  public DwarthonStoneface(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
