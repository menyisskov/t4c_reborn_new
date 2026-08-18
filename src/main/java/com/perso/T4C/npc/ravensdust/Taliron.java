package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Taliron extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Taliron";

  public static final String DISPLAY_NAME = "${npc.taliron}";

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
          "${npc.welcome.taliron}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.0.0}"), "${npc.topic.taliron.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.1.0}"), "${npc.topic.taliron.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.2.0}"), "${npc.topic.taliron.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.3.0}", "${npc.topic_keyword.taliron.3.1}"),
                  "${npc.topic.taliron.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.4.0}"), "${npc.topic.taliron.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.5.0}"), "${npc.topic.taliron.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.6.0}"), "${npc.topic.taliron.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.taliron.7.0}"), "${npc.topic.taliron.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.taliron.8.0}",
                      "${npc.topic_keyword.taliron.8.1}",
                      "${npc.topic_keyword.taliron.8.2}"),
                  "${npc.topic.taliron.8}",
                  List.of())),
          "ShopKeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      private final List<String> armor =
          List.of(
              "red_cape",
              "studded_leather_belt",
              "studded_leather_gloves",
              "studded_leather_helmet",
              "studded_leather_pants",
              "studded_leather_boots",
              "studded_leather_armor",
              "ringmail_girdle",
              "ringmail_armor",
              "ringmail_gauntlets",
              "ringmail_helmet",
              "ringmail_leggings",
              "ringmail_boots",
              "chainmail_girdle",
              "chainmail",
              "chainmail_gloves",
              "chainmail_coif",
              "chainmail_leggings",
              "chainmail_boots",
              "round_shield",
              "large_shield");

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int h = java.time.LocalTime.now().getHour();

        c.sayKey(
            h >= 6 && h < 18
                ? "npc.taliron.day"
                : h >= 22 || h < 6 ? "npc.taliron.sleep" : "npc.taliron.evening");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String keyword) {

        String k = keyword == null ? "" : keyword.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SELL")) {

          if (closed()) c.sayKey("npc.taliron.closed");
          else c.openSellShop();

          return true;
        }

        if (k.contains("ARMOR") || k.equals("BUY")) {

          if (closed()) c.sayKey("npc.taliron.closed");
          else c.openShop(armor);

          return true;
        }

        return false;
      }

      private boolean closed() {

        int h = java.time.LocalTime.now().getHour();

        return h >= 22 || h < 6;
      }
    };
  }

  public Taliron(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
