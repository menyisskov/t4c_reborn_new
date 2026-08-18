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

public final class DariusMoonglow extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DariusMoonglow";

  public static final String DISPLAY_NAME = "${npc.dariusmoonglow}";

  public static final String SPRITE_BASE = "PaysanModel1";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.dariusmoonglow}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.0.0}",
                      "${npc.topic_keyword.dariusmoonglow.0.1}"),
                  "${npc.topic.dariusmoonglow.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.1.0}",
                      "${npc.topic_keyword.dariusmoonglow.1.1}"),
                  "${npc.topic.dariusmoonglow.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.2.0}",
                      "${npc.topic_keyword.dariusmoonglow.2.1}",
                      "${npc.topic_keyword.dariusmoonglow.2.2}"),
                  "${npc.topic.dariusmoonglow.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.3.0}"),
                  "${npc.topic.dariusmoonglow.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.4.0}"),
                  "${npc.topic.dariusmoonglow.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.5.0}"),
                  "${npc.topic.dariusmoonglow.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.6.0}",
                      "${npc.topic_keyword.dariusmoonglow.6.1}"),
                  "${npc.topic.dariusmoonglow.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.7.0}"),
                  "${npc.topic.dariusmoonglow.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.8.0}"),
                  "${npc.topic.dariusmoonglow.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.9.0}"),
                  "${npc.topic.dariusmoonglow.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.10.0}"),
                  "${npc.topic.dariusmoonglow.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.11.0}"),
                  "${npc.topic.dariusmoonglow.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.12.0}"),
                  "${npc.topic.dariusmoonglow.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dariusmoonglow.13.0}"),
                  "${npc.topic.dariusmoonglow.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.14.0}",
                      "${npc.topic_keyword.dariusmoonglow.14.1}"),
                  "${npc.topic.dariusmoonglow.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.15.0}",
                      "${npc.topic_keyword.dariusmoonglow.15.1}",
                      "${npc.topic_keyword.dariusmoonglow.15.2}",
                      "${npc.topic_keyword.dariusmoonglow.15.3}"),
                  "${npc.topic.dariusmoonglow.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dariusmoonglow.16.0}",
                      "${npc.topic_keyword.dariusmoonglow.16.1}",
                      "${npc.topic_keyword.dariusmoonglow.16.2}",
                      "${npc.topic_keyword.dariusmoonglow.16.3}",
                      "${npc.topic_keyword.dariusmoonglow.16.4}"),
                  "${npc.topic.dariusmoonglow.16}",
                  List.of())),
          "Lower_Rank_Druid",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("GEM")) {

          int q = c.flag("__QUEST_COMPLETED_GEM");

          if (q == 0) {

            c.flag("__QUEST_COMPLETED_GEM", 1);

            c.sayKey("npc.darius.gems.ask");

          } else if (q == 1
              && c.hasItem("red_gem")
              && c.hasItem("green_gem")
              && c.hasItem("yellow_gem")
              && c.hasItem("blue_gem")) {

            c.takeItem("red_gem");

            c.takeItem("green_gem");

            c.takeItem("yellow_gem");

            c.takeItem("blue_gem");

            c.giveItem("nature_garb");

            c.flag("__QUEST_COMPLETED_GEM", 2);

            c.sayKey("npc.darius.gems.done");

          } else if (q == 1) c.sayKey("npc.darius.gems.missing");
          else c.sayKey("npc.darius.gems.thanks");

          return true;
        }

        if (k.equals("HEAL")) {

          int missing = c.player().getMaxHp() - c.player().getCurrentHp();

          if (missing <= 0) c.sayKey("npc.darius.heal.none");
          else if (c.player().getLevel() < 5) {

            c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());

            c.castTargetSpell(10767);

            c.sayKey("npc.darius.heal.free");

          } else {

            c.sayKey("npc.darius.heal.ask");

            c.askYesNo("darius_heal");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"darius_heal".equals(s)) return false;

        if (!yes) return true;

        int missing = c.player().getMaxHp() - c.player().getCurrentHp(),
            gold = c.player().getGold();

        if (gold < missing) {

          if (gold > 0) {

            c.player().addGold(-gold);

            c.player().applyHeal(c.player().getCurrentHp() + gold, c.player().getMaxHp());

            c.sayKey("npc.darius.heal.partial");

          } else c.sayKey("npc.darius.heal.poor");

        } else {

          c.player().addGold(-missing);

          c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());

          c.sayKey("npc.darius.heal.done");
        }

        c.castTargetSpell(10767);

        return true;
      }
    };
  }

  public DariusMoonglow(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
