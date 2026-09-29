package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.PassageToAvalon;
import com.perso.T4C.quest.definition.TidewornShoreScouts;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// Stoneheim's Stonecrest quay (180,740) begins the Witness Isles story. The old quest IDs and
// unlock zone ID remain unchanged so existing character saves retain passage after rebirth.
// Conversation keywords lead through scouts, Ithrak's chart, and an explicit report for each
// objective. A character who completed the original passage is never re-offered the scouts.
@Spawn(type = "HarbormasterRangor", x = 180, y = 740, z = 0, stationary = true, aggressive = false)
public final class HarbormasterRangor extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HarbormasterRangor";

  public static final String DISPLAY_NAME = "${npc.harbormasterrangor}";

  public static final String SPRITE_BASE = null;

  // Declarative fallback/documentation entry for the compendium - real dispatch always happens
  // in javaBehavior() below, which intercepts these same keywords first.
  private static final NpcSpec.DialogueTopic AVALON_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of(
              "${npc.topic_keyword.harbormasterrangor.0.0}",
              "${npc.topic_keyword.harbormasterrangor.0.1}",
              "${npc.topic_keyword.harbormasterrangor.0.2}"),
          "${npc.topic.harbormasterrangor.0}",
          List.of());

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.harbormasterrangor}",
          List.of(
              AVALON_TOPIC,
              new NpcSpec.DialogueTopic(
                  List.of("${npc.harbormasterrangor.keyword.scouts}"),
                  "${npc.harbormasterrangor.scouts}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.harbormasterrangor.keyword.chart}"),
                  "${npc.harbormasterrangor.chart}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.harbormasterrangor.keyword.report}"),
                  "${npc.harbormasterrangor.report}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.harbormasterrangor.keyword.route}"),
                  "${npc.harbormasterrangor.route}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.1.0}"),
                  "${npc.topic.harbormasterrangor.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.2.0}"),
                  "${npc.topic.harbormasterrangor.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.harbormasterrangor.3.0}"),
                  "${npc.topic.harbormasterrangor.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.harbormasterrangor.4.0}",
                      "${npc.topic_keyword.harbormasterrangor.4.1}"),
                  "${npc.topic.harbormasterrangor.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.harbormasterrangor.5.0}",
                      "${npc.topic_keyword.harbormasterrangor.5.1}"),
                  "${npc.topic.harbormasterrangor.5}",
                  List.of())),
          "HarbormasterRangorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public HarbormasterRangor(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected boolean automaticallyTurnInQuests() {
    return false;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        Player player = context.player();
        QuestService quests = context.npc().questService();
        if (quests == null) return false;
        if (ScriptedNpc.matches(AVALON_TOPIC, keyword)) {
          context.say(I18n.resolve("${npc.topic.harbormasterrangor.0}"));
          return true;
        }
        if (ScriptedNpc.matches(SPEC.topics().get(1), keyword)) {
          if (QuestService.statusFor(player, TidewornShoreScouts.definition())
              == QuestService.STATUS_COMPLETED) {
            context.say(I18n.resolve("${npc.harbormasterrangor.scouts_done}"));
          } else {
            context.say(
                quests.giveOrReport("tideworn_shore_scouts", ID, player)
                    + "\n"
                    + I18n.resolve("${npc.harbormasterrangor.report_prompt}"));
          }
          return true;
        }
        if (ScriptedNpc.matches(SPEC.topics().get(2), keyword)) {
          if (QuestService.statusFor(player, TidewornShoreScouts.definition())
                  != QuestService.STATUS_COMPLETED
              && QuestService.statusFor(player, PassageToAvalon.definition())
                  == QuestService.STATUS_NOT_STARTED) {
            context.say(I18n.resolve("${npc.harbormasterrangor.scouts_first}"));
          } else {
            context.say(
                quests.giveOrReport("passage_to_avalon", ID, player)
                    + "\n"
                    + I18n.resolve("${npc.harbormasterrangor.report_prompt}"));
          }
          return true;
        }
        if (ScriptedNpc.matches(SPEC.topics().get(4), keyword)) {
          context.say(I18n.resolve("${npc.harbormasterrangor.route}"));
          return true;
        }
        if (ScriptedNpc.matches(SPEC.topics().get(3), keyword)) {
          String id = nextQuestIdFor(player);
          if (QuestService.statusFor(player, QuestRegistry.findById(id))
              == QuestService.STATUS_NOT_STARTED) {
            context.say(I18n.resolve("${npc.harbormasterrangor.start_first}"));
          } else {
            String response = quests.turnInQuest(id, ID, player);
            if (response != null) {
              if (QuestService.statusFor(player, QuestRegistry.findById(id))
                  == QuestService.STATUS_ACTIVE) {
                response += "\n" + I18n.resolve("${npc.harbormasterrangor.report_prompt}");
              }
              context.say(response);
            }
          }
          return true;
        }
        return false;
      }
    };
  }

  /** Stage two once it's ever been touched (active, or completed - including by a character who
   * finished the original single-stage quest before this chain existed); otherwise stage two
   * once stage one is done, or stage one itself. */
  private static String nextQuestIdFor(Player player) {
    if (QuestService.statusFor(player, PassageToAvalon.definition())
        != QuestService.STATUS_NOT_STARTED) {
      return "passage_to_avalon";
    }
    if (QuestService.statusFor(player, TidewornShoreScouts.definition())
        == QuestService.STATUS_COMPLETED) {
      return "passage_to_avalon";
    }
    return "tideworn_shore_scouts";
  }
}
