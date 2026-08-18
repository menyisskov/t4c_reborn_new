package com.perso.T4C.npc.windhowl;

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

public final class TtayhMark extends ScriptedNpc {

  public static final String ID = "TtayhMark";

  public static final String DISPLAY_NAME = "${npc.ttayhmark}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.ttayhmark}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ttayhmark.0.0}", "${npc.topic_keyword.ttayhmark.0.1}"),
                  "${npc.topic.ttayhmark.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ttayhmark.1.0}",
                      "${npc.topic_keyword.ttayhmark.1.1}",
                      "${npc.topic_keyword.ttayhmark.1.2}"),
                  "${npc.topic.ttayhmark.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ttayhmark.2.0}",
                      "${npc.topic_keyword.ttayhmark.2.1}",
                      "${npc.topic_keyword.ttayhmark.2.2}"),
                  "${npc.topic.ttayhmark.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.3.0}"),
                  "${npc.topic.ttayhmark.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.4.0}"),
                  "${npc.topic.ttayhmark.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.5.0}"),
                  "${npc.topic.ttayhmark.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.6.0}"),
                  "${npc.topic.ttayhmark.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.7.0}"),
                  "${npc.topic.ttayhmark.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.8.0}"),
                  "${npc.topic.ttayhmark.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.9.0}"),
                  "${npc.topic.ttayhmark.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.ttayhmark.10.0}"),
                  "${npc.topic.ttayhmark.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.ttayhmark.11.0}",
                      "${npc.topic_keyword.ttayhmark.11.1}",
                      "${npc.topic_keyword.ttayhmark.11.2}",
                      "${npc.topic_keyword.ttayhmark.11.3}",
                      "${npc.topic_keyword.ttayhmark.11.4}"),
                  "${npc.topic.ttayhmark.11}",
                  List.of())),
          "Shopkeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final java.util.List<String> weapons =
          java.util.List.of(
              "polished_short_sword",
              "polished_long_sword",
              "polished_broadsword",
              "polished_hand_axe",
              "polished_dirk",
              "polished_dagger",
              "steel_reinforced_warhammer",
              "fine_steel_warhammer",
              "quarterstaff",
              "staff_of_thorns",
              "elm_flatbow",
              "elm_longbow",
              "elm_reflex_bow",
              "elm_recurve_bow",
              "pinpoint_arrow");

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BLACK MARKET")) {

          if (c.flag("__BLACK_MARKET") != 2) c.sayKey("npc.ttayh.blackmarket.unknown");
          else if (java.time.LocalTime.now().getHour() >= 6
              && java.time.LocalTime.now().getHour() < 18) {

            c.sayKey("npc.ttayh.blackmarket.day");

            c.npcFlag("TTAYH_MARK_BLACK_MARKET", 1 + (int) (Math.random() * 10));

          } else {

            c.sayKey("npc.ttayh.blackmarket.night");

            c.askYesNo("bm");
          }

          return true;
        }

        if (k.equals("BUY")) {

          c.sayKey("npc.ttayh.buy");

          c.askYesNo("browse");

          return true;
        }

        if (k.equals("SELL")) {

          c.openSellShop();

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if (!yes) {

          c.sayKey("npc.ttayh.no");

          return true;
        }

        if ("browse".equals(state)) {

          c.openShop(weapons);

          return true;
        }

        if ("bm".equals(state)) {

          int r = c.npcFlag("TTAYH_MARK_BLACK_MARKET"), l = c.player().getLevel();

          java.util.List<String> i;

          if (l <= 9)
            i =
                r <= 5
                    ? java.util.List.of(
                        "light_healing_potion", "ring_of_light", "ring_of_confidence")
                    : r <= 8
                        ? java.util.List.of(
                            "mana_elixir", "leather_belt_of_survival", "ring_of_the_bear")
                        : java.util.List.of("healing_potion", "ring_of_light", "staff_of_thorns_1");
          else if (l <= 15)
            i =
                r <= 5
                    ? java.util.List.of("ring_of_confidence", "silver_axe")
                    : r <= 8
                        ? java.util.List.of(
                            "light_healing_potion",
                            "healing_potion",
                            "ring_of_accuracy",
                            "staff_of_thorns_1")
                        : java.util.List.of(
                            "mana_elixir",
                            "buckle_of_lockpicks",
                            "ring_of_the_bear",
                            "ring_of_light",
                            "ring_of_confidence");
          else if (l <= 19)
            i =
                r <= 5
                    ? java.util.List.of("ring_of_accuracy", "silver_axe")
                    : r <= 8
                        ? java.util.List.of(
                            "light_healing_potion", "leather_belt_of_survival", "staff_of_thorns_1")
                        : java.util.List.of(
                            "ring_of_the_bear", "buckle_of_lockpicks", "silver_axe");
          else
            i =
                r <= 5
                    ? java.util.List.of("mana_elixir", "ring_of_accuracy")
                    : r <= 8
                        ? java.util.List.of(
                            "healing_potion", "ring_of_light", "ring_of_accuracy", "silver_axe")
                        : java.util.List.of("light_healing_potion", "ring_of_the_bear");

          c.openShop(i);

          return true;
        }

        return false;
      }
    };
  }

  public TtayhMark(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
