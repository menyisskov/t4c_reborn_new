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

public final class RhodarHeatforge extends ScriptedNpc {

  public static final String ID = "RhodarHeatforge";

  public static final String DISPLAY_NAME = "${npc.rhodarheatforge}";

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
          "${npc.welcome.rhodarheatforge}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.0.0}",
                      "${npc.topic_keyword.rhodarheatforge.0.1}"),
                  "${npc.topic.rhodarheatforge.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.1.0}",
                      "${npc.topic_keyword.rhodarheatforge.1.1}"),
                  "${npc.topic.rhodarheatforge.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.2.0}",
                      "${npc.topic_keyword.rhodarheatforge.2.1}",
                      "${npc.topic_keyword.rhodarheatforge.2.2}"),
                  "${npc.topic.rhodarheatforge.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.3.0}",
                      "${npc.topic_keyword.rhodarheatforge.3.1}"),
                  "${npc.topic.rhodarheatforge.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.4.0}"),
                  "${npc.topic.rhodarheatforge.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.5.0}"),
                  "${npc.topic.rhodarheatforge.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.6.0}"),
                  "${npc.topic.rhodarheatforge.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.7.0}"),
                  "${npc.topic.rhodarheatforge.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.8.0}"),
                  "${npc.topic.rhodarheatforge.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.9.0}"),
                  "${npc.topic.rhodarheatforge.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.10.0}"),
                  "${npc.topic.rhodarheatforge.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.11.0}",
                      "${npc.topic_keyword.rhodarheatforge.11.1}"),
                  "${npc.topic.rhodarheatforge.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.12.0}",
                      "${npc.topic_keyword.rhodarheatforge.12.1}"),
                  "${npc.topic.rhodarheatforge.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.13.0}",
                      "${npc.topic_keyword.rhodarheatforge.13.1}"),
                  "${npc.topic.rhodarheatforge.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rhodarheatforge.14.0}"),
                  "${npc.topic.rhodarheatforge.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rhodarheatforge.15.0}",
                      "${npc.topic_keyword.rhodarheatforge.15.1}",
                      "${npc.topic_keyword.rhodarheatforge.15.2}",
                      "${npc.topic_keyword.rhodarheatforge.15.3}",
                      "${npc.topic_keyword.rhodarheatforge.15.4}"),
                  "${npc.topic.rhodarheatforge.15}",
                  List.of())),
          "ShopKeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final java.util.List<String> weapons =
          java.util.List.of(
              "fine_steel_short_sword",
              "fine_steel_long_sword",
              "fine_steel_broadsword",
              "fine_steel_scimitar",
              "fine_steel_scimitar_dual",
              "high_metal_short_sword",
              "fine_steel_hand_axe",
              "fine_steel_hand_axe_dual",
              "fine_steel_battle_axe",
              "fine_steel_dagger",
              "fine_steel_dagger_dual",
              "high_metal_dagger",
              "hickory_flatbow",
              "hickory_longbow",
              "hickory_reflex_bow",
              "hickory_recurve_bow",
              "hickory_compound_bow",
              "bone_tipped_arrow");

      private boolean closed() {

        int h = java.time.LocalTime.now().getHour();

        return h < 6 || h >= 22;
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        c.sayKey(closed() ? "npc.rhodar.closed" : "npc.rhodar.welcome");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BUY") || k.equals("WEAPON")) {

          c.sayKey("npc.rhodar.buy");

          c.askYesNo("browse");

          return true;
        }

        if (k.equals("SELL")) {

          if (closed()) c.sayKey("npc.rhodar.closed");
          else c.openSellShop();

          return true;
        }

        if (k.equals("HAMMER")) {

          if (c.hasItem("rhodar_hammer")) {

            c.sayKey("npc.rhodar.hammer.ask");

            c.askYesNo("hammer");

          } else c.sayKey("npc.rhodar.hammer.none");

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if (!yes) {

          c.sayKey("npc.rhodar.no");

          return true;
        }

        if ("browse".equals(state)) {

          if (closed()) c.sayKey("npc.rhodar.closed");
          else c.openShop(weapons);

          return true;
        }

        if ("hammer".equals(state)) {

          if (c.hasItem("rhodar_hammer")) {

            c.takeItem("rhodar_hammer");

            c.giveGold(3500);

            c.giveXp(8000);

            c.sayKey("npc.rhodar.hammer.done");

          } else c.sayKey("npc.rhodar.hammer.missing");

          return true;
        }

        return false;
      }
    };
  }

  public RhodarHeatforge(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
