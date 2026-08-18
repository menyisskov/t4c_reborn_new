package com.perso.T4C.npc.arakas;

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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "LanceSilversmith", x = 2580, y = 690, z = 0, stationary = false, aggressive = false)
public final class LanceSilversmith extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "LanceSilversmith";

  public static final String DISPLAY_NAME = "${npc.lancesilversmith}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.lancesilversmith}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.0.0}",
                      "${npc.topic_keyword.lancesilversmith.0.1}",
                      "${npc.topic_keyword.lancesilversmith.0.2}"),
                  "${npc.topic.lancesilversmith.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.1.0}",
                      "${npc.topic_keyword.lancesilversmith.1.1}"),
                  "${npc.topic.lancesilversmith.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.2.0}",
                      "${npc.topic_keyword.lancesilversmith.2.1}"),
                  "${npc.topic.lancesilversmith.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.3.0}"),
                  "${npc.topic.lancesilversmith.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.4.0}"),
                  "${npc.topic.lancesilversmith.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.5.0}"),
                  "${npc.topic.lancesilversmith.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.6.0}"),
                  "${npc.topic.lancesilversmith.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.7.0}"),
                  "${npc.topic.lancesilversmith.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.8.0}"),
                  "${npc.topic.lancesilversmith.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.9.0}",
                      "${npc.topic_keyword.lancesilversmith.9.1}",
                      "${npc.topic_keyword.lancesilversmith.9.2}"),
                  "${npc.topic.lancesilversmith.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.10.0}",
                      "${npc.topic_keyword.lancesilversmith.10.1}"),
                  "${npc.topic.lancesilversmith.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.11.0}",
                      "${npc.topic_keyword.lancesilversmith.11.1}"),
                  "${npc.topic.lancesilversmith.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.12.0}",
                      "${npc.topic_keyword.lancesilversmith.12.1}"),
                  "${npc.topic.lancesilversmith.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.13.0}"),
                  "${npc.topic.lancesilversmith.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.14.0}"),
                  "${npc.topic.lancesilversmith.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lancesilversmith.15.0}"),
                  "${npc.topic.lancesilversmith.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.16.0}",
                      "${npc.topic_keyword.lancesilversmith.16.1}",
                      "${npc.topic_keyword.lancesilversmith.16.2}",
                      "${npc.topic_keyword.lancesilversmith.16.3}"),
                  "${npc.topic.lancesilversmith.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lancesilversmith.17.0}",
                      "${npc.topic_keyword.lancesilversmith.17.1}",
                      "${npc.topic_keyword.lancesilversmith.17.2}",
                      "${npc.topic_keyword.lancesilversmith.17.3}",
                      "${npc.topic_keyword.lancesilversmith.17.4}"),
                  "${npc.topic.lancesilversmith.17}",
                  List.of())),
          "NakedTorso_Smith",
          new NpcSpec.CombatProfile(100, 1000000, 65, 65, 65, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST") == 12 && c.hasItem("silversmith_letter")) {

          c.takeItem("silversmith_letter");

          c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 13);

          c.sayKey("npc.lancesilversmith.letter");

        } else if (c.player().getLevel() <= 15) c.sayKey("npc.lancesilversmith.lowlevel");
        else c.sayKey("npc.lancesilversmith.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("MAKE") && k.contains("GLOOMBLADE")) {

          if (has(c, "scroll_of_enchantment", "nightsword", "ring_of_darkness")
              && c.player().getGold() >= 10000) {

            take(c, "scroll_of_enchantment", "nightsword", "ring_of_darkness");

            c.player().addGold(-10000);

            c.giveItem("gloomblade");

            c.sayKey("npc.lancesilversmith.make.gloomblade.ok");

          } else c.sayKey("npc.lancesilversmith.make.gloomblade.missing");

          return true;
        }

        if (k.contains("MAKE") && k.contains("BLESSED") && k.contains("CHAINMAIL")) {

          if (has(c, "scroll_of_enchantment", "chainmail") && c.player().getGold() >= 5500) {

            take(c, "scroll_of_enchantment", "chainmail");

            c.player().addGold(-5500);

            c.giveItem("blessed_chainmail_armor");

            c.sayKey("npc.lancesilversmith.make.chainmail.ok");

          } else c.sayKey("npc.lancesilversmith.make.chainmail.missing");

          return true;
        }

        if (k.contains("MAKE") && k.contains("DEMONBLADE")) {

          if (c.karma() > -100) {

            c.sayKey("npc.lancesilversmith.make.demonblade.good");

            return true;
          }

          if (has(
                  c,
                  "scroll_of_enchantment",
                  "chaos_sword",
                  "demon_skull",
                  "necklace_of_the_black_heart")
              && c.player().getGold() >= 8000) {

            take(
                c,
                "scroll_of_enchantment",
                "chaos_sword",
                "demon_skull",
                "necklace_of_the_black_heart");

            c.player().addGold(-8000);

            c.giveItem("demonblade");

            c.sayKey("npc.lancesilversmith.make.demonblade.ok");

          } else c.sayKey("npc.lancesilversmith.make.demonblade.missing");

          return true;
        }

        if (k.contains("MAKE") && k.contains("HAMMER") && k.contains("FINALITY")) {

          int q = c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST"),
              count = c.flag("__FLAG_COUNTER_HAMMER_OF_FINALITY");

          if (q >= 14) c.sayKey("npc.lancesilversmith.hammer.done");
          else if (q != 13) c.sayKey("npc.lancesilversmith.hammer.unavailable");
          else if (has(c, "fine_steel_warhammer", "scroll_of_enchantment")
              && c.player().getGold() >= 4000) {

            take(c, "fine_steel_warhammer", "scroll_of_enchantment");

            c.player().addGold(-4000);

            c.giveItem("hammer_of_finality");

            count++;

            c.flag("__FLAG_COUNTER_HAMMER_OF_FINALITY", count);

            if (count == 3) {

              c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 14);

              c.summon("MOBSHADOWSTALKER", c.npcTileX(), c.npcTileY(), 0);
            }

            c.sayKey("npc.lancesilversmith.hammer.ok");

          } else c.sayKey("npc.lancesilversmith.hammer.missing");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      private boolean has(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String... items) {

        for (String i : items) if (!c.hasItem(i)) return false;

        return true;
      }

      private void take(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String... items) {

        for (String i : items) c.takeItem(i);
      }
    };
  }

  public LanceSilversmith(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
