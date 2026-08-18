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
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PrincessDelilah extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "PrincessDelilah";

  public static final String DISPLAY_NAME = "${npc.princessdelilah}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe"),
              new NpcSpec.Part(BodyPart.HEAD, "PupGoldenCrown")),
          0,
          List.of(),
          "${npc.welcome.princessdelilah}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.0.0}"),
                  "${npc.topic.princessdelilah.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.1.0}",
                      "${npc.topic_keyword.princessdelilah.1.1}",
                      "${npc.topic_keyword.princessdelilah.1.2}"),
                  "${npc.topic.princessdelilah.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.2.0}",
                      "${npc.topic_keyword.princessdelilah.2.1}",
                      "${npc.topic_keyword.princessdelilah.2.2}",
                      "${npc.topic_keyword.princessdelilah.2.3}"),
                  "${npc.topic.princessdelilah.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.3.0}"),
                  "${npc.topic.princessdelilah.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.4.0}"),
                  "${npc.topic.princessdelilah.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.5.0}",
                      "${npc.topic_keyword.princessdelilah.5.1}"),
                  "${npc.topic.princessdelilah.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.6.0}"),
                  "${npc.topic.princessdelilah.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.7.0}"),
                  "${npc.topic.princessdelilah.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.8.0}"),
                  "${npc.topic.princessdelilah.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.princessdelilah.9.0}"),
                  "${npc.topic.princessdelilah.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.10.0}",
                      "${npc.topic_keyword.princessdelilah.10.1}"),
                  "${npc.topic.princessdelilah.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.11.0}",
                      "${npc.topic_keyword.princessdelilah.11.1}"),
                  "${npc.topic.princessdelilah.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.12.0}",
                      "${npc.topic_keyword.princessdelilah.12.1}"),
                  "${npc.topic.princessdelilah.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.13.0}",
                      "${npc.topic_keyword.princessdelilah.13.1}"),
                  "${npc.topic.princessdelilah.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.14.0}",
                      "${npc.topic_keyword.princessdelilah.14.1}"),
                  "${npc.topic.princessdelilah.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.15.0}",
                      "${npc.topic_keyword.princessdelilah.15.1}",
                      "${npc.topic_keyword.princessdelilah.15.2}",
                      "${npc.topic_keyword.princessdelilah.15.3}"),
                  "${npc.topic.princessdelilah.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.princessdelilah.16.0}",
                      "${npc.topic_keyword.princessdelilah.16.1}",
                      "${npc.topic_keyword.princessdelilah.16.2}",
                      "${npc.topic_keyword.princessdelilah.16.3}",
                      "${npc.topic_keyword.princessdelilah.16.4}"),
                  "${npc.topic.princessdelilah.16}",
                  List.of())),
          "PrincessDelilahNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1) {

          c.sayKey("npc.delilah.alignment");

          return;
        }

        if (c.globalFlag("__QUEST_BISHOP_ILLNESS") == 0) {

          if (c.flag("__QUEST_CUTHANA") == 0) c.sayKey("npc.delilah.ask_chamberlain");
          else if (!c.hasItem("cuthana_herb")) c.sayKey("npc.delilah.find_cuthana");
          else {

            while (c.hasItem("cuthana_herb")) c.takeItem("cuthana_herb");

            if (c.karma() <= 100 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
              c.karma(c.karma() + 30 * (500 - c.karma()) / 500);

            c.flag("__QUEST_CUTHANA", 2);

            c.globalFlag("__QUEST_BISHOP_ILLNESS", 1);

            c.globalFlag(
                "__GLOBAL_QUEST_CROWBANNER_TIMER",
                (int)
                    (System.currentTimeMillis() / 1000L
                        + 4 * 3600L * (1 + (int) (Math.random() * 6))));

            c.sayKey("npc.delilah.cuthana");
          }

        } else if (c.flag("__QUEST_CUTHANA") == 2) c.sayKey("npc.delilah.indebted");
        else c.sayKey("npc.delilah.praying");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("GOLD") || k.equals("MONEY") || k.equals("WEALTH")) {

          if (c.flag("__QUEST_CUTHANA") == 2) {

            c.sayKey("npc.delilah.gold.ask");

            c.askYesNo("paid_gold");

          } else c.sayKey("npc.delilah.gold.done");

          return true;
        }

        if (k.equals("BALORK") || k.equals("MARK") || k.equals("BRAND") || k.equals("CURSE")) {

          if (c.flag("__BALORK_BRAND") >= 1 && c.flag("__QUEST_CUTHANA") == 2) {

            c.sayKey("npc.delilah.curse.offer");

            c.askYesNo("remove_curse");

          } else
            c.sayKey(
                c.flag("__BALORK_BRAND") >= 1
                    ? "npc.delilah.curse.present"
                    : "npc.delilah.curse.none");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if ("paid_gold".equals(state)) {

          if (yes) {

            c.giveGold(10000);

            c.flag("__QUEST_CUTHANA", 3);

            int access = c.flag("__QUEST_ISLAND_ACCESS");

            if (c.karma() <= -50 * (access + 1) && c.karma() >= -100 * (access + 1))
              c.karma(c.karma() - 20 * (500 + c.karma()) / 500);
            else if (c.karma() >= 50 * (access + 1))
              c.karma(c.karma() - 10 * (500 + c.karma()) / 500);

            c.sayKey("npc.delilah.gold.reward");

          } else c.sayKey("npc.delilah.gold.no");

          return true;
        }

        if ("remove_curse".equals(state)) {

          if (!yes) {

            c.sayKey("npc.delilah.curse.no");

            return true;
          }

          c.flag("__QUEST_CUTHANA", 4);

          c.flag("__BALORK_BRAND", 0);

          if (c.karma() <= 100 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
            c.karma(c.karma() + 20 * (500 - c.karma()) / 500);

          c.flag("__FLAG_GOOD_QUEST_COMPLETED_ON_RD", 1);

          for (String i :
              java.util.List.of(
                  "royal_key_1",
                  "royal_key_2",
                  "royal_key_3",
                  "royal_key_4",
                  "royal_key_5",
                  "royal_key_6")) while (c.hasItem(i)) c.takeItem(i);

          c.sayKey("npc.delilah.curse.removed");

          return true;
        }

        return false;
      }
    };
  }

  public PrincessDelilah(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
