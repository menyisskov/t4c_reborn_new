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

public final class Kiadus extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Kiadus";

  public static final String DISPLAY_NAME = "${npc.kiadus}";

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
          "${npc.welcome.kiadus}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.0.0}"), "${npc.topic.kiadus.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.1.0}"), "${npc.topic.kiadus.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.2.0}"), "${npc.topic.kiadus.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.3.0}", "${npc.topic_keyword.kiadus.3.1}"),
                  "${npc.topic.kiadus.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.4.0}"), "${npc.topic.kiadus.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.5.0}"), "${npc.topic.kiadus.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.6.0}"), "${npc.topic.kiadus.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.kiadus.7.0}"), "${npc.topic.kiadus.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.kiadus.8.0}",
                      "${npc.topic_keyword.kiadus.8.1}",
                      "${npc.topic_keyword.kiadus.8.2}"),
                  "${npc.topic.kiadus.8}",
                  List.of())),
          "ShopKeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final java.util.List<String> weapons =
          java.util.List.of(
              "fine_steel_mace",
              "fine_steel_mace_dual",
              "high_metal_mace",
              "high_metal_flail",
              "bo",
              "rang_kwan",
              "tetsubo");

      private boolean closed() {

        int h = java.time.LocalTime.now().getHour();

        return h < 6 || h >= 22;
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        int h = java.time.LocalTime.now().getHour();

        c.sayKey(
            h >= 6 && h < 18
                ? "npc.kiadus.day"
                : h >= 18 && h < 22 ? "npc.kiadus.evening" : "npc.kiadus.night");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BUY") || k.equals("WEAPON")) {

          c.askYesNo("browse");

          return true;
        }

        if (k.equals("SELL")) {

          if (closed()) c.sayKey("npc.kiadus.closed");
          else c.openSellShop();

          return true;
        }

        if (k.equals("VISITOR")) {

          int q = c.flag("__QUEST_VISITOR_SPOTTED");

          if (q < 4) c.sayKey("npc.kiadus.visitor.before");
          else if (q == 4) {

            c.sayKey("npc.kiadus.visitor.found");

            c.flag("__QUEST_VISITOR_SPOTTED", 5);

            c.flag("__FLAG_ASKED_ABOUT_SWORD", 1);

          } else c.sayKey("npc.kiadus.visitor.after");

          return true;
        }

        if (k.equals("SISTER")) {

          int q = c.flag("__FLAG_PAID_KIADUS_BRIBE");

          if (q == 2) {

            c.sayKey("npc.kiadus.sister.again");

            c.askYesNo("bribe");

          } else if (q == 1) {

            c.sayKey("npc.kiadus.sister.name");

            c.flag("__FLAG_PAID_KIADUS_BRIBE", 2);

          } else {

            c.sayKey("npc.kiadus.sister.ask");

            c.askYesNo("bribe");
          }

          return true;
        }

        return false;
      }

      @Override
      public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

        if (!yes) {

          c.sayKey("npc.kiadus.no");

          return true;
        }

        if ("browse".equals(state)) {

          if (closed()) c.sayKey("npc.kiadus.closed");
          else c.openShop(weapons);

          return true;
        }

        if ("bribe".equals(state)) {

          if (c.player().getGold() >= 2000) {

            c.player().addGold(-2000);

            c.flag("__FLAG_PAID_KIADUS_BRIBE", 1);

            c.sayKey("npc.kiadus.paid");

          } else if (c.player().getGold() >= 1000) {

            int g = c.player().getGold();

            c.player().addGold(-g);

            c.sayKey("npc.kiadus.partial");

          } else c.sayKey("npc.kiadus.poor");

          return true;
        }

        return false;
      }
    };
  }

  public Kiadus(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
