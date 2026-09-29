package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.AvalonWildsVigil;
import com.perso.T4C.quest.definition.FadingVeilReckoning;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// A conversation-led quest chain: explain, accept, fulfill the objectives, then report.
@Spawn(type = "ElderOphira", x = 4044, y = 1462, z = 0, stationary = true, aggressive = false)
public final class ElderOphira extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "ElderOphira";

  public static final String DISPLAY_NAME = "${npc.elderophira}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.elderophira}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elderophira.0.0}",
                      "${npc.topic_keyword.elderophira.0.1}"),
                  "${npc.topic.elderophira.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elderophira.1.0}",
                      "${npc.topic_keyword.elderophira.1.1}"),
                  "${npc.topic.elderophira.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.2.0}"),
                  "${npc.topic.elderophira.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.3.0}"),
                  "${npc.topic.elderophira.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.4.0}"),
                  "${npc.topic.elderophira.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.elderophira.5.0}",
                      "${npc.topic_keyword.elderophira.5.1}"),
                  "${npc.topic.elderophira.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.6.0}"),
                  "${npc.topic.elderophira.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.7.0}"),
                  "${npc.topic.elderophira.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.elderophira.8.0}"),
                  "${npc.topic.elderophira.8}",
                  List.of())),
          "ElderOphiraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  private static final QuestDef WILDS = AvalonWildsVigil.definition();
  private static final QuestDef VEIL = FadingVeilReckoning.definition();

  // This is an offer within the current conversation, not durable quest progress. Accepted
  // quests use the existing save flags. A new greeting always discards any unaccepted offer.
  private QuestDef selectedQuest;
  private Player conversationPlayer;

  public ElderOphira(NpcContext context) throws GameException {
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
      public void onConversationStart(NpcBehaviorContext context) {
        selectedQuest = null;
        conversationPlayer = context.player();
        QuestDef current = currentQuest(context.player());
        int status = QuestService.statusFor(context.player(), current);
        context.say(
            status == QuestService.STATUS_ACTIVE
                ? "${npc.elderophira.awaiting_report}"
                : status == QuestService.STATUS_COMPLETED
                    ? "${npc.elderophira.chain_completed}"
                    : current == VEIL ? "${npc.elderophira.next_chapter}" : SPEC.welcomeText());
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        Player player = context.player();
        if (conversationPlayer != player) {
          selectedQuest = null;
          conversationPlayer = player;
        }
        QuestService quests = questService();
        if (quests == null) return false;
        // Commands precede lore matching: "accept the wilds" must not merely re-explain it.
        if (matches(SPEC.topics().get(6), keyword)) {
          if (selectedQuest == null) {
            context.say("${npc.topic.elderophira.6}");
          } else if (QuestService.statusFor(player, selectedQuest)
              == QuestService.STATUS_NOT_STARTED) {
            if (selectedQuest == VEIL && !canDiscussVeil(player)) {
              context.say("${npc.elderophira.veil_locked}");
            } else {
              context.say(quests.giveOrReport(selectedQuest.getId(), ID, player));
            }
          } else {
            showProgress(context, quests, selectedQuest);
          }
          return true;
        }
        if (matches(SPEC.topics().get(7), keyword)) {
          QuestDef report = selectedQuest == null ? currentQuest(player) : selectedQuest;
          if (QuestService.statusFor(player, report) == QuestService.STATUS_NOT_STARTED) {
            context.say("${npc.topic.elderophira.7}");
          } else {
            String response = quests.turnInQuest(report.getId(), ID, player);
            if (QuestService.statusFor(player, report) == QuestService.STATUS_COMPLETED) {
              context.say(
                  response
                      + "\n"
                      + I18n.resolve(
                          report == WILDS
                              ? "${npc.elderophira.next_chapter}"
                              : "${npc.elderophira.chain_completed}"));
              selectedQuest = null;
            } else {
              context.say(response + "\n" + I18n.resolve("${npc.elderophira.objectives_pending}"));
            }
          }
          return true;
        }
        if (matches(SPEC.topics().get(8), keyword)) {
          QuestDef route = selectedQuest == null ? currentQuest(player) : selectedQuest;
          context.say(routeText(route));
          return true;
        }
        for (int topic = 0; topic <= 1; topic++) {
          if (!matches(SPEC.topics().get(topic), keyword)) continue;
          if (topic == 1 && !canDiscussVeil(player)) {
            selectedQuest = null;
            context.say("${npc.elderophira.veil_locked}");
            return true;
          }
          selectedQuest = topic == 0 ? WILDS : VEIL;
          if (QuestService.statusFor(player, selectedQuest) == QuestService.STATUS_NOT_STARTED) {
            context.say(
                I18n.resolve(SPEC.topics().get(topic).response())
                    + "\n"
                    + I18n.resolve(selectedQuest.getOfferText())
                    + "\n"
                    + I18n.resolve("${npc.elderophira.offer_accept}"));
          } else {
            showProgress(context, quests, selectedQuest);
          }
          return true;
        }
        return false;
      }
    };
  }

  private static void showProgress(
      NpcBehaviorContext context, QuestService quests, QuestDef quest) {
    String response = quests.giveOrReport(quest.getId(), ID, context.player());
    if (QuestService.statusFor(context.player(), quest) == QuestService.STATUS_ACTIVE) {
      response += "\n" + I18n.resolve("${npc.elderophira.report_instruction}");
    }
    context.say(response);
  }

  private static boolean canDiscussVeil(Player player) {
    // A saved character who already accepted the old independent Veil quest can still finish it.
    return QuestService.statusFor(player, WILDS) == QuestService.STATUS_COMPLETED
        || QuestService.statusFor(player, VEIL) != QuestService.STATUS_NOT_STARTED;
  }

  static String routeText(QuestDef quest) {
    return I18n.resolve(
        quest != null && VEIL.getId().equals(quest.getId())
            ? "${npc.elderophira.veil_route}"
            : SPEC.topics().get(8).response());
  }

  private static QuestDef currentQuest(Player player) {
    if (QuestService.statusFor(player, VEIL) != QuestService.STATUS_NOT_STARTED
        || QuestService.statusFor(player, WILDS) == QuestService.STATUS_COMPLETED) {
      return VEIL;
    }
    return WILDS;
  }
}
