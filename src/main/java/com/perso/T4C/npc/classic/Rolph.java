package com.perso.T4C.npc.classic;

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

public final class Rolph extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Rolph";

  public static final String DISPLAY_NAME = "${npc.rolph}";

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
          "${npc.welcome.rolph}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.0.0}", "${npc.topic_keyword.rolph.0.1}"),
                  "${npc.topic.rolph.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rolph.1.0}",
                      "${npc.topic_keyword.rolph.1.1}",
                      "${npc.topic_keyword.rolph.1.2}"),
                  "${npc.topic.rolph.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.2.0}"), "${npc.topic.rolph.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rolph.3.0}",
                      "${npc.topic_keyword.rolph.3.1}",
                      "${npc.topic_keyword.rolph.3.2}"),
                  "${npc.topic.rolph.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.4.0}"), "${npc.topic.rolph.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.5.0}"), "${npc.topic.rolph.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.6.0}"), "${npc.topic.rolph.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.7.0}"), "${npc.topic.rolph.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.8.0}", "${npc.topic_keyword.rolph.8.1}"),
                  "${npc.topic.rolph.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.9.0}"), "${npc.topic.rolph.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rolph.10.0}",
                      "${npc.topic_keyword.rolph.10.1}",
                      "${npc.topic_keyword.rolph.10.2}"),
                  "${npc.topic.rolph.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.11.0}"), "${npc.topic.rolph.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.rolph.12.0}"), "${npc.topic.rolph.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.rolph.13.0}",
                      "${npc.topic_keyword.rolph.13.1}",
                      "${npc.topic_keyword.rolph.13.2}",
                      "${npc.topic_keyword.rolph.13.3}",
                      "${npc.topic_keyword.rolph.13.4}"),
                  "${npc.topic.rolph.13}",
                  List.of())),
          "ShopKeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private final List<String> goods =
            List.of(
                "cloth_pants",
                "cloth_vest",
                "leather_belt",
                "leather_gloves",
                "leather_helmet",
                "leather_pants",
                "leather_boots",
                "leather_armor",
                "red_cape",
                "studded_leather_belt",
                "studded_leather_gloves",
                "studded_leather_helmet",
                "studded_leather_pants",
                "studded_leather_boots",
                "studded_leather_armor",
                "wooden_shield");

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (!c.hasItem("merchant_letter")) c.flag("__QUEST_ROLPH_REPORT", 0);

          c.sayKey("npc.rolph.welcome");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("BUY") || k.contains("SHOP") || k.contains("ARMOR")) {

            c.openShop(goods);

            return true;
          }

          if (k.contains("REGISTRATION") || k.equals("LETTER")) {

            if (!c.hasItem("merchant_letter")) {

              c.sayKey("npc.rolph.need.registration");

              return true;
            }

            long now = System.currentTimeMillis() / 1000L,
                until = Integer.toUnsignedLong(c.globalFlag("GLOBAL_QUEST_ROLPH_REPORT"));

            if (until > now) {

              c.sayKey("npc.rolph.cooldown");

              return true;
            }

            c.takeItem("merchant_letter");

            c.flag("__QUEST_MERCHANT", 0);

            if (c.player().getLevel() <= 4) {

              c.sayKey("npc.rolph.too.young");

              return true;
            }

            int expiry = (int) (now + 3600);

            c.globalFlag("GLOBAL_QUEST_ROLPH_REPORT", expiry);

            c.flag("__QUEST_ROLPH_REPORT", expiry);

            c.giveItem("rolph_report");

            c.sayKey("npc.rolph.report.given");

            return true;
          }

          return false;
        }
      };

  public Rolph(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
