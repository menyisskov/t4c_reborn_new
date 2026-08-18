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
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OmarHald extends ScriptedNpc {

  public static final String ID = "OmarHald";

  public static final String DISPLAY_NAME = "${npc.omarhald}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.omarhald}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.omarhald.0.0}", "${npc.topic_keyword.omarhald.0.1}"),
                  "${npc.topic.omarhald.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.omarhald.1.0}",
                      "${npc.topic_keyword.omarhald.1.1}",
                      "${npc.topic_keyword.omarhald.1.2}",
                      "${npc.topic_keyword.omarhald.1.3}"),
                  "${npc.topic.omarhald.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.omarhald.2.0}"),
                  "${npc.topic.omarhald.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.omarhald.3.0}", "${npc.topic_keyword.omarhald.3.1}"),
                  "${npc.topic.omarhald.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.omarhald.4.0}"),
                  "${npc.topic.omarhald.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.omarhald.5.0}",
                      "${npc.topic_keyword.omarhald.5.1}",
                      "${npc.topic_keyword.omarhald.5.2}",
                      "${npc.topic_keyword.omarhald.5.3}"),
                  "${npc.topic.omarhald.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.omarhald.6.0}",
                      "${npc.topic_keyword.omarhald.6.1}",
                      "${npc.topic_keyword.omarhald.6.2}",
                      "${npc.topic_keyword.omarhald.6.3}"),
                  "${npc.topic.omarhald.6}",
                  List.of())),
          "OmarHaldNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("RAW") && k.contains("CRYSTAL")) {

          if (c.itemCount("raw_crystal") >= 9) {

            c.sayKey("npc.omar.crystal.ask");

            c.askYesNo("omar_crystal");

          } else c.sayKey("npc.omar.crystal.info");

          return true;
        }

        if (k.equals("BUY") || k.equals("SHOP")) {

          c.sayKey("npc.omar.shop.ask");

          c.askYesNo("omar_shop");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("omar_crystal".equals(s)) {

          if (yes && c.itemCount("raw_crystal") >= 9 && c.player().getGold() > 50000) {

            for (int i = 0; i < 9; i++) c.takeItem("raw_crystal");

            c.player().addGold(-50000);

            c.giveItem("gem_of_courage");

            c.sayKey("npc.omar.crystal.done");

          } else if (yes && c.player().getGold() <= 50000) c.sayKey("npc.omar.crystal.gold");
          else if (yes) c.sayKey("npc.omar.crystal.missing");

          return true;
        }

        if ("omar_shop".equals(s)) {

          if (yes)
            c.openShop(
                java.util.List.of(
                    "ring_of_the_adept",
                    "ring_of_the_healer",
                    "ring_of_the_priest",
                    "templar_ring"));

          return true;
        }

        return false;
      }
    };
  }

  public OmarHald(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
