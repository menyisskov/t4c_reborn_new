package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.ConcordCampaign;
import java.util.ArrayList;
import java.util.List;

/** Player-owned, saved evidence: shared NPC instances never hold another player's puzzle state. */
abstract class ConcordStoryNpc extends ScriptedNpc {
  private final int site;

  protected ConcordStoryNpc(NpcSpec spec, NpcContext context, int site) throws GameException {
    super(spec, context);
    this.site = site;
  }

  static NpcSpec storySpec(String id) {
    List<String> words =
        new ArrayList<>(
            List.of(
                "story",
                "accept",
                "report",
                "route",
                "rewards",
                "record",
                "echo",
                "names",
                "shelter",
                "truth",
                "mercy",
                "reveal",
                "choice",
                "ring",
                "remember",
                "passage",
                "return",
                "restore",
                "release"));
    words.addAll(ConcordCampaign.STYLES);
    return new NpcSpec(
        id,
        "${npc.concord." + id + ".name}",
        null,
        List.of(
            new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
            new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
        0,
        List.of(),
        "${npc.concord." + id + ".welcome}",
        words.stream()
            .map(
                w ->
                    new NpcSpec.DialogueTopic(
                        List.of("${npc.concord.keyword." + w + "}"),
                        "${npc.concord.help}",
                        List.of()))
            .toList(),
        id + "NPC");
  }

  @Override
  protected boolean automaticallyTurnInQuests() {
    return false;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public void onConversationStart(NpcBehaviorContext c) {
        c.say(specification().welcomeText());
        if (site < 0) guide(c, "story");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String keyword) {
        // Match resolved catalogue keywords, including case and quote normalization.
        for (var topic : specification().topics()) {
          if (!ScriptedNpc.matches(topic, keyword)) continue;
          String word = topic.keywords().getFirst();
          word = word.substring("${npc.concord.keyword.".length(), word.length() - 1);
          if (site < 0) guide(c, word);
          else investigate(c, word);
          return true;
        }
        return false;
      }
    };
  }

  private void guide(NpcBehaviorContext c, String word) {
    int chapter = ConcordCampaign.chapter(c.player());
    if (word.equals("return")) {
      c.teleport(4040, 1477, 0);
      return;
    }
    if (word.equals("passage")) {
      if (c.flag(QuestService.zoneUnlockFlag("threnody_reach")) != 0) c.teleport(5505, 1150, 0);
      else c.sayKey("npc.concord.passage_locked");
      return;
    }
    if (chapter == ConcordCampaign.all().size()) {
      c.say(
          c.flag("concord.ending") == 1
              ? "${npc.concord.ending_restore}"
              : "${npc.concord.ending_release}");
      c.say(
          c.flag("concord.refuge") == 1
              ? "${npc.concord.refuge_mercy}"
              : "${npc.concord.refuge_reveal}");
      return;
    }
    var quest = ConcordCampaign.all().get(chapter);
    if (ConcordCampaign.STYLES.contains(word)) {
      c.flag("concord.style", ConcordCampaign.STYLES.indexOf(word) + 1);
      questService().saveProgress();
      c.sayKey("npc.concord.style_selected");
    } else if (word.equals("restore") || word.equals("release")) {
      if (chapter != 7 || c.flag(QuestService.killsFlag(quest)) < 1) {
        c.sayKey("npc.concord.ending_locked");
        return;
      }
      c.flag("concord.ending", word.equals("restore") ? 1 : 2);
      questService().saveProgress();
      c.sayKey("npc.concord.ending_selected");
    } else if (word.equals("accept")) {
      c.say(questService().giveOrReport(quest.getId(), ConcordCampaign.GIVER, c.player()));
    } else if (word.equals("report")) {
      if (QuestService.statusFor(c.player(), quest) != QuestService.STATUS_ACTIVE) {
        c.sayKey("npc.concord.accept_first");
        return;
      }
      if (c.player().getLevel() < quest.getMinLevel()) {
        c.sayKey("message.quest_progress_dialog_needs_level", quest.getMinLevel());
        return;
      }
      if (chapter == 7 && c.flag("concord.ending") == 0) {
        c.sayKey("npc.concord.choose_ending");
        return;
      }
      if (ConcordCampaign.choiceReward(chapter)) {
        int selection = c.flag("concord.style");
        if (selection < 1 || selection > ConcordCampaign.STYLES.size()) {
          c.sayKey("npc.concord.rewards");
          return;
        }
        String reward = ConcordCampaign.reward(chapter, ConcordCampaign.STYLES.get(selection - 1));
        if (!InventoryService.canAdd(c.player(), reward)) {
          c.sayKey("npc.concord.capacity");
          return;
        }
        String response =
            questService()
                .completeWithAlternateReward(
                    quest.getId(),
                    c.player(),
                    () -> {
                      InventoryService.add(c.player(), reward);
                      c.giveGold(quest.getRewardGold());
                      c.giveXp(quest.getRewardXp());
                    });
        c.say(response == null ? quest.getWalkthroughText() : response);
      } else c.say(questService().turnInQuest(quest.getId(), ConcordCampaign.GIVER, c.player()));
      if (QuestService.statusFor(c.player(), quest) == QuestService.STATUS_COMPLETED) {
        c.sayKey("npc.concord.next");
        if (chapter == 7) guide(c, "story");
      }
    } else if (word.equals("rewards")) c.sayKey("npc.concord.rewards");
    else c.say(quest.getWalkthroughText());
  }

  private void investigate(NpcBehaviorContext c, String word) {
    if (c.player().getCoordinates().getZ() != 0 || !c.isInRange(12)) {
      c.sayKey("npc.concord.approach");
      return;
    }
    if (word.equals("return") && site == 3) {
      c.teleport(4040, 1477, 0);
      return;
    }
    int chapter = ConcordCampaign.chapter(c.player());
    boolean ritual = chapter == 6 && (site == 1 || site == 2 || site == 4);
    if ((!ritual && chapter != site)
        || chapter >= ConcordCampaign.all().size()
        || QuestService.statusFor(c.player(), ConcordCampaign.all().get(chapter))
            != QuestService.STATUS_ACTIVE) {
      c.sayKey("npc.concord.site_locked");
      return;
    }
    String progress = ConcordCampaign.flag(chapter, "site" + site);
    String prefix = "${npc.concord.site." + site + (ritual ? ".ritual" : "");
    int state = c.flag(progress);
    if (word.equals("record") || word.equals("echo")) {
      c.flag(progress, state | (word.equals("record") ? 1 : 2));
      questService().saveProgress();
      c.say(prefix + "." + word + "}");
      return;
    }
    if ((state & 3) != 3) {
      c.say(prefix + ".welcome}");
      return;
    }
    if (site == 4 && !ritual && (state & 4) == 0) {
      String sequence = ConcordCampaign.flag(chapter, "sequence");
      int note = c.flag(sequence);
      if (note < 0 || note > 2) note = 0;
      if (!word.equals(List.of("remember", "shelter", "choice").get(note))) {
        c.flag(sequence, 0);
        questService().saveProgress();
        c.say(prefix + ".question}");
        return;
      }
      c.flag(sequence, note + 1);
      questService().saveProgress();
      if (note < 2) {
        c.sayKey("npc.concord.next_note");
        return;
      }
    }
    String answer =
        ritual
            ? (site == 1 ? "shelter" : site == 2 ? "truth" : "choice")
            : switch (site) {
              case 0 -> "names";
              case 1 -> "shelter";
              case 2 -> "truth";
              case 3 -> "mercy";
              default -> "choice";
            };
    if (!word.equals(answer) && !(site == 3 && word.equals("reveal"))) {
      c.say(prefix + ".question}");
      return;
    }
    if (site == 3) {
      // A recovered proof never lets the player rewrite an already recorded moral choice.
      if (c.flag("concord.refuge") == 0) c.flag("concord.refuge", word.equals("mercy") ? 1 : 2);
    }
    c.flag(progress, state | 4);
    questService().saveProgress();
    if (ritual && (c.flag(ConcordCampaign.flag(6, "site1")) & 4) == 0
        || ritual && (c.flag(ConcordCampaign.flag(6, "site2")) & 4) == 0
        || ritual && (c.flag(ConcordCampaign.flag(6, "site4")) & 4) == 0) {
      c.sayKey("npc.concord.bell_remaining");
      return;
    }
    String proof = ConcordCampaign.all().get(chapter).getRequiredItemKey();
    if (!c.hasItem(proof) && !InventoryService.add(c.player(), proof).success()) {
      c.sayKey("npc.concord.capacity");
      return;
    }
    questService().saveProgress();
    c.say(prefix + ".success}");
  }
}
