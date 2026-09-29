package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import java.util.List;

/** Conversation gate shared by the two campaign witnesses. Save state lives in quest flags. */
abstract class CampaignQuestNpc extends ScriptedNpc {
  private final List<QuestDef> stages;
  private final QuestDef prerequisite;

  protected CampaignQuestNpc(
      NpcSpec spec, NpcContext context, List<QuestDef> stages, QuestDef prerequisite)
      throws GameException {
    super(spec, context);
    this.stages = List.copyOf(stages);
    this.prerequisite = prerequisite;
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
        if (HollowDawnCampaign.accountCompleted(context.player())) {
          context.flag(QuestService.zoneUnlockFlag("threnody_reach"), 1);
        }
        String status =
            !unlocked(context.player())
                ? "${npc.hollow_dawn.locked}"
                : choosing(context.player())
                    ? "${npc.hollow_dawn.choose}"
                    : current(context.player()) == null
                        ? finishedText()
                        : "${npc.hollow_dawn.ask_story}";
        context.say(I18n.resolve(specification().welcomeText()) + "\n" + I18n.resolve(status));
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        NpcSpec spec = specification();
        int topic = -1;
        for (int i = 0; i < spec.topics().size(); i++) {
          if (ScriptedNpc.matches(spec.topics().get(i), keyword)) {
            topic = i;
            break;
          }
        }
        if (topic < 0) return false;
        if (!unlocked(context.player())) {
          context.say("${npc.hollow_dawn.locked}");
          return true;
        }
        if (topic >= 4) {
          if (topic == 6 || topic == 7) {
            if (!isChronicler()) {
              context.say("${npc.hollow_dawn.threnody}");
            } else if (!choosing(context.player())) {
              context.say("${npc.hollow_dawn.path_chosen}");
            } else if (questService() != null) {
              QuestDef first = HollowDawnCampaign.avalon().get(topic == 6 ? 0 : 2);
              context.say(
                  questService().giveOrReport(first.getId(), spec.id(), context.player())
                      + "\n"
                      + I18n.resolve("${npc.hollow_dawn.pending}"));
            }
          } else {
            context.say(
                switch (topic) {
                  case 4 -> "${npc.hollow_dawn.witnesses}";
                  case 5 -> "${npc.hollow_dawn.warders}";
                  case 8 -> "${npc.hollow_dawn.hierarchy}";
                  default -> "${npc.hollow_dawn.threnody}";
                });
          }
          return true;
        }
        if (choosing(context.player())) {
          context.say("${npc.hollow_dawn.choose}");
          return true;
        }
        QuestDef quest = current(context.player());
        if (quest == null) {
          context.say(finishedText());
          return true;
        }
        String missingClue = HollowDawnCampaign.missingClue(context.player(), quest);
        if (missingClue != null) {
          context.say("${npc.hollow_dawn.seek_" + missingClue + "}");
          return true;
        }
        QuestService service = questService();
        if (service == null) return false;
        int status = QuestService.statusFor(context.player(), quest);
        String response;
        switch (topic) {
          case 0 ->
              response =
                  I18n.resolve(quest.getOfferText())
                      + "\n"
                      + I18n.resolve("${npc.hollow_dawn.accept_prompt}");
          case 1 -> response = service.giveOrReport(quest.getId(), spec.id(), context.player());
          case 2 -> {
            if (status == QuestService.STATUS_NOT_STARTED) {
              response = I18n.resolve("${npc.hollow_dawn.accept_first}");
            } else {
              response = service.turnInQuest(quest.getId(), spec.id(), context.player());
              if (QuestService.statusFor(context.player(), quest)
                  == QuestService.STATUS_COMPLETED) {
                QuestDef next = current(context.player());
                response +=
                    "\n"
                        + I18n.resolve(
                            next == null
                                ? finishedText()
                                : "${npc.hollow_dawn.next}");
              } else {
                response += "\n" + I18n.resolve("${npc.hollow_dawn.pending}");
              }
            }
          }
          case 3 -> response = I18n.resolve(quest.getWalkthroughText());
          default -> {
            return false;
          }
        }
        if (response != null) context.say(response);
        return true;
      }
    };
  }

  private boolean isChronicler() {
    return HollowDawnCampaign.CHRONICLER.equals(specification().id());
  }

  private String finishedText() {
    return isChronicler() ? "${npc.hollow_dawn.account_finished}" : "${npc.hollow_dawn.finished}";
  }

  private boolean choosing(Player player) {
    return isChronicler() && HollowDawnCampaign.selectedPath(player).isEmpty();
  }

  private boolean unlocked(Player player) {
    // Grandfather active/completed quest saves, even if their earlier prerequisite is absent.
    return stages.stream().anyMatch(q -> QuestService.statusFor(player, q) != 0)
        || (isChronicler()
            ? QuestService.statusFor(player, prerequisite) == QuestService.STATUS_COMPLETED
            : HollowDawnCampaign.accountCompleted(player));
  }

  private QuestDef current(Player player) {
    List<QuestDef> available = isChronicler() ? HollowDawnCampaign.selectedPath(player) : stages;
    for (QuestDef quest : available) {
      if (QuestService.statusFor(player, quest) == QuestService.STATUS_ACTIVE) return quest;
    }
    if (!available.isEmpty()
        && QuestService.statusFor(player, available.getLast()) == QuestService.STATUS_COMPLETED) {
      return null;
    }
    for (QuestDef quest : available) {
      if (QuestService.statusFor(player, quest) != QuestService.STATUS_COMPLETED) return quest;
    }
    return null;
  }
}
