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
import java.util.List;

public final class DionysusSilverstream extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DionysusSilverstream";

  public static final String DISPLAY_NAME = "${npc.dionysussilverstream}";

  public static final String SPRITE_BASE = "64kCentaurShaman";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.dionysussilverstream}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.0.0}",
                      "${npc.topic_keyword.dionysussilverstream.0.1}"),
                  "${npc.topic.dionysussilverstream.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.1.0}",
                      "${npc.topic_keyword.dionysussilverstream.1.1}",
                      "${npc.topic_keyword.dionysussilverstream.1.2}"),
                  "${npc.topic.dionysussilverstream.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.2.0}",
                      "${npc.topic_keyword.dionysussilverstream.2.1}"),
                  "${npc.topic.dionysussilverstream.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.3.0}",
                      "${npc.topic_keyword.dionysussilverstream.3.1}"),
                  "${npc.topic.dionysussilverstream.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.4.0}",
                      "${npc.topic_keyword.dionysussilverstream.4.1}"),
                  "${npc.topic.dionysussilverstream.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.5.0}",
                      "${npc.topic_keyword.dionysussilverstream.5.1}"),
                  "${npc.topic.dionysussilverstream.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.6.0}",
                      "${npc.topic_keyword.dionysussilverstream.6.1}"),
                  "${npc.topic.dionysussilverstream.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.7.0}",
                      "${npc.topic_keyword.dionysussilverstream.7.1}"),
                  "${npc.topic.dionysussilverstream.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.8.0}",
                      "${npc.topic_keyword.dionysussilverstream.8.1}"),
                  "${npc.topic.dionysussilverstream.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.9.0}",
                      "${npc.topic_keyword.dionysussilverstream.9.1}"),
                  "${npc.topic.dionysussilverstream.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dionysussilverstream.10.0}"),
                  "${npc.topic.dionysussilverstream.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.11.0}",
                      "${npc.topic_keyword.dionysussilverstream.11.1}"),
                  "${npc.topic.dionysussilverstream.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dionysussilverstream.12.0}"),
                  "${npc.topic.dionysussilverstream.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.13.0}",
                      "${npc.topic_keyword.dionysussilverstream.13.1}"),
                  "${npc.topic.dionysussilverstream.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.14.0}",
                      "${npc.topic_keyword.dionysussilverstream.14.1}",
                      "${npc.topic_keyword.dionysussilverstream.14.2}"),
                  "${npc.topic.dionysussilverstream.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.15.0}",
                      "${npc.topic_keyword.dionysussilverstream.15.1}",
                      "${npc.topic_keyword.dionysussilverstream.15.2}"),
                  "${npc.topic.dionysussilverstream.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dionysussilverstream.16.0}"),
                  "${npc.topic.dionysussilverstream.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dionysussilverstream.17.0}"),
                  "${npc.topic.dionysussilverstream.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dionysussilverstream.18.0}"),
                  "${npc.topic.dionysussilverstream.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.19.0}",
                      "${npc.topic_keyword.dionysussilverstream.19.1}",
                      "${npc.topic_keyword.dionysussilverstream.19.2}"),
                  "${npc.topic.dionysussilverstream.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.20.0}",
                      "${npc.topic_keyword.dionysussilverstream.20.1}"),
                  "${npc.topic.dionysussilverstream.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.dionysussilverstream.21.0}"),
                  "${npc.topic.dionysussilverstream.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.22.0}",
                      "${npc.topic_keyword.dionysussilverstream.22.1}",
                      "${npc.topic_keyword.dionysussilverstream.22.2}",
                      "${npc.topic_keyword.dionysussilverstream.22.3}"),
                  "${npc.topic.dionysussilverstream.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.dionysussilverstream.23.0}",
                      "${npc.topic_keyword.dionysussilverstream.23.1}",
                      "${npc.topic_keyword.dionysussilverstream.23.2}",
                      "${npc.topic_keyword.dionysussilverstream.23.3}"),
                  "${npc.topic.dionysussilverstream.23}",
                  List.of())),
          "DionysusSilverstreamNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private boolean night() {

        int h = java.time.LocalTime.now().getHour();

        return h >= 18 || h < 6;
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("LUTE") && k.contains("PEACE")) {

          if (night()
              && c.flag("__QUEST_DIONYSUS_RECHARGE_HARP") == 3
              && c.hasItem("lute_of_peace")) {

            c.takeItem("lute_of_peace");

            c.flag("__QUEST_DIONYSUS_RECHARGE_HARP", 0);

            c.giveXp(c.player().getLevel() * 750);

            c.giveGold(c.player().getLevel() * 2000);

            if (Math.random() < .333) c.giveItem("robe_of_heaven");

            c.sayKey("npc.dionysus.lute.done");

          } else c.sayKey(night() ? "npc.dionysus.lute.info" : "npc.dionysus.lute.day");

          return true;
        }

        if (k.contains("RECHARG")) {

          if (night() && c.flag("__QUEST_DIONYSUS_RECHARGE_HARP") == 0) {

            c.sayKey("npc.dionysus.recharge.ask");

            c.askYesNo("dionysus_harp");

          } else
            c.sayKey(night() ? "npc.dionysus.recharge.already" : "npc.dionysus.recharge.night");

          return true;
        }

        if (k.contains("SKELETAL") && k.contains("CENTAUR")) {

          int n = c.flag("__FLAG_SKELETAL_CENTAURS_KILLED");

          if (n >= 12) {

            c.sayKey("npc.dionysus.centaur.reward.ask");

            c.askYesNo("dionysus_reward");

          } else c.sayKey("npc.dionysus.centaur.progress");

          return true;
        }

        if (k.contains("STAFF") && k.contains("HOPE")) {

          if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1
              || c.flag("__FLAG_LIGHTBRINGER_OF_ARTHERK") == 0)
            c.sayKey("npc.dionysus.staff.denied");
          else if (c.itemCount("symbol_of_hope") >= 5 && c.itemCount("demon_tree_wood") >= 2) {

            c.sayKey("npc.dionysus.staff.ask");

            c.askYesNo("dionysus_staff");

          } else c.sayKey("npc.dionysus.staff.info");

          return true;
        }

        if (k.equals("HEAL") || k.contains("HEALING")) {

          if (c.player().getCurrentHp() >= c.player().getMaxHp()) {

            c.sayKey("npc.dionysus.heal.none");

          } else if (c.player().getLevel() < 5) {

            c.player().setCurrentHp(c.player().getMaxHp());

            c.sayKey("npc.dionysus.heal.free");

          } else {

            c.sayKey("npc.dionysus.heal.ask");

            c.askYesNo("dionysus_heal");
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("dionysus_harp".equals(s)) {

          if (yes) {

            c.flag("__QUEST_DIONYSUS_RECHARGE_HARP", 1);

            c.giveItem("lute_of_peace");

            c.sayKey("npc.dionysus.recharge.done");
          }

          return true;
        }

        if ("dionysus_reward".equals(s)) {

          if (yes) {

            c.flag("__FLAG_SKELETAL_CENTAURS_KILLED", 0);

            c.giveItem("scroll_of_horse_friendship");

            c.giveXp(c.player().getLevel() * 1500);

            c.sayKey("npc.dionysus.centaur.reward");
          }

          return true;
        }

        if ("dionysus_staff".equals(s)) {

          if (yes && c.itemCount("symbol_of_hope") >= 5 && c.itemCount("demon_tree_wood") >= 2) {

            for (int i = 0; i < 5; i++) c.takeItem("symbol_of_hope");

            for (int i = 0; i < 2; i++) c.takeItem("demon_tree_wood");

            c.giveItem("staff_of_hope");

            c.sayKey("npc.dionysus.staff.done");
          }

          return true;
        }

        if ("dionysus_heal".equals(s)) {

          if (yes) {

            int missing = c.player().getMaxHp() - c.player().getCurrentHp(),
                cost = Math.max(0, missing / 2),
                gold = c.player().getGold();

            if (gold == 0) c.sayKey("npc.dionysus.heal.poor");
            else {

              int paid = Math.min(cost, gold);

              c.player().addGold(-paid);

              c.player()
                  .setCurrentHp(
                      Math.min(c.player().getMaxHp(), c.player().getCurrentHp() + paid * 2));

              c.sayKey(paid < cost ? "npc.dionysus.heal.partial" : "npc.dionysus.heal.done");
            }

          } else c.sayKey("npc.dionysus.heal.no");

          return true;
        }

        return false;
      }
    };
  }

  public DionysusSilverstream(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
