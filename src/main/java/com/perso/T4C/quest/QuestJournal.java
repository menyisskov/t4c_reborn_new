package com.perso.T4C.quest;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.definition.ConcordCampaign;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Read-only journal projection of current quest flags, inventory and dialogue checkpoints. */
public final class QuestJournal {
  public enum Kind {
    MAIN_STORY,
    SIDE_QUEST
  }

  public record Entry(
      String id,
      QuestDef definition,
      Kind kind,
      boolean completed,
      String title,
      String chain,
      String stage,
      String nextStep,
      List<String> objectives,
      String giver,
      String description) {
    public Entry {
      objectives = List.copyOf(objectives);
    }
  }

  private static final Set<String> STORY_CHAINS =
      Set.of(
          "${quest.chain.witness_passage}",
          "${quest.chain.avalon_pact}",
          "${quest.chain.hollow_dawn}",
          "${quest.chain.concord}");
  private static final List<String> CONCORD_SITES =
      List.of(
          "OathwoodArchivist",
          "MoonwakeBellkeeper",
          "EmberglassConfessor",
          "ThrenodyRefugee",
          "BellwardTuner");

  private QuestJournal() {}

  public static List<Entry> entries(Player player) {
    if (player == null) return List.of();
    List<Entry> result = new ArrayList<>();
    for (QuestDef quest : QuestRegistry.load()) {
      if (QuestService.statusFor(player, quest) != QuestService.STATUS_NOT_STARTED)
        result.add(entryFor(player, quest));
    }
    addLegacyEntries(player, result);
    result.sort(Comparator.comparing(Entry::kind));
    return List.copyOf(result);
  }

  public static Kind kind(QuestDef quest) {
    QuestChainInfo.Stage chain = QuestChainInfo.stageFor(quest.getId());
    return chain != null && STORY_CHAINS.contains(chain.chainName())
        ? Kind.MAIN_STORY
        : Kind.SIDE_QUEST;
  }

  public static Entry entryFor(Player player, QuestDef quest) {
    boolean completed = QuestService.statusFor(player, quest) == QuestService.STATUS_COMPLETED;
    QuestChainInfo.Stage chain = QuestChainInfo.stageFor(quest.getId());
    String chainText =
        chain == null
            ? ""
            : I18n.message(
                "quest.chain.stage",
                I18n.resolve(chain.chainName()),
                chain.stageNumber(),
                chain.totalStages());
    List<String> objectives = new ArrayList<>();
    if (completed) objectives.add(t("complete"));
    else {
      if (quest.getRequiredKills() > 0)
        objectives.add(
            I18n.message(
                "quest.objective.kills",
                I18n.resolve(quest.getTargetMonster()),
                kills(player, quest),
                quest.getRequiredKills()));
      if (hasItemObjective(quest))
        objectives.add(
            I18n.message(
                "quest.objective.evidence",
                itemName(quest.getRequiredItemKey()),
                held(player, quest),
                quest.getRequiredItemQty()));
      if (quest.getMinLevel() > player.getLevel())
        objectives.add(
            I18n.message("journal.level_progress", player.getLevel(), quest.getMinLevel()));
    }
    String stage =
        completed
            ? t("complete")
            : !objectivesDone(player, quest)
                ? t("in_progress")
                : player.getLevel() < quest.getMinLevel() ? t("level_required") : t("ready");
    int chapter = concordChapter(quest);
    if (!completed && chapter >= 0 && !objectivesDone(player, quest))
      stage = concordStage(player, chapter);
    else if (!completed && chapter >= 0 && player.getLevel() >= quest.getMinLevel()) {
      if (chapter == 7 && player.getQuestFlag("concord.ending") == 0) stage = t("decision");
      else if (player.getQuestFlag("concord.style") < 1
          || player.getQuestFlag("concord.style") > ConcordCampaign.STYLES.size())
        stage = t("reward_choice");
    }
    return new Entry(
        quest.getId(),
        quest,
        kind(quest),
        completed,
        I18n.resolve(quest.getTitle()),
        chainText,
        stage,
        completed ? afterCompletion(player, quest) : nextStep(player, quest),
        objectives,
        QuestService.giverDisplayName(quest),
        I18n.resolve(completed ? quest.getCompletedText() : quest.getOfferText()));
  }

  private static boolean hasItemObjective(QuestDef q) {
    return q.getRequiredItemKey() != null
        && !q.getRequiredItemKey().isBlank()
        && q.getRequiredItemQty() > 0;
  }

  private static int held(Player p, QuestDef q) {
    return Math.min(
        q.getRequiredItemQty(), Collections.frequency(p.getInventory(), q.getRequiredItemKey()));
  }

  private static int kills(Player p, QuestDef q) {
    return Math.max(0, Math.min(q.getRequiredKills(), p.getQuestFlag(QuestService.killsFlag(q))));
  }

  private static boolean objectivesDone(Player p, QuestDef q) {
    return kills(p, q) >= q.getRequiredKills()
        && (!hasItemObjective(q) || held(p, q) >= q.getRequiredItemQty());
  }

  private static String itemName(String key) {
    var item = ItemRegistry.findByKey(key);
    return item == null ? t("required_item") : I18n.resolve(item.getName());
  }

  private static String npcName(String id) {
    var registration = NpcFactoryRegistry.find(id);
    return registration == null ? t("quest_giver") : I18n.resolve(registration.displayName());
  }

  private static String t(String key, Object... args) {
    return I18n.message("journal." + key, args);
  }

  private static String nextStep(Player p, QuestDef q) {
    int chapter = concordChapter(q);
    if (objectivesDone(p, q)) {
      if (p.getLevel() < q.getMinLevel()) return t("reach_level", q.getMinLevel());
      if (chapter == 7 && p.getQuestFlag("concord.ending") == 0)
        return t("choose_ending", npcName(ConcordCampaign.GIVER));
      if (chapter >= 0
          && ConcordCampaign.choiceReward(chapter)
          && (p.getQuestFlag("concord.style") < 1
              || p.getQuestFlag("concord.style") > ConcordCampaign.STYLES.size())) {
        return t("choose_style", npcName(ConcordCampaign.GIVER));
      }
      return chapter >= 0
          ? t("concord_report", npcName(ConcordCampaign.GIVER))
          : t("return_to", QuestService.giverDisplayName(q));
    }
    if (kills(p, q) < q.getRequiredKills())
      return t("defeat", I18n.resolve(q.getTargetMonster()), q.getRequiredKills() - kills(p, q));
    if (chapter >= 0) return concordNext(p, chapter, q);
    if (hasItemObjective(q))
      return t("collect", q.getRequiredItemQty() - held(p, q), itemName(q.getRequiredItemKey()));
    return t("return_to", QuestService.giverDisplayName(q));
  }

  private static String afterCompletion(Player p, QuestDef q) {
    for (QuestDef next : QuestRegistry.load()) {
      var stage = QuestChainInfo.stageFor(next.getId());
      if (stage == null
          || !stage.prerequisiteQuestIds().contains(q.getId())
          || QuestService.statusFor(p, next) == QuestService.STATUS_COMPLETED) continue;
      boolean available =
          QuestService.statusFor(p, next) == QuestService.STATUS_ACTIVE
              || (stage.anyPrerequisite()
                  ? stage.prerequisiteQuestIds().stream().anyMatch(id -> completed(p, id))
                  : stage.prerequisiteQuestIds().stream().allMatch(id -> completed(p, id)));
      if (available)
        return t(
            "continue_with", QuestService.giverDisplayName(next), I18n.resolve(next.getTitle()));
      for (String id : stage.prerequisiteQuestIds()) {
        QuestDef other = QuestRegistry.findById(id);
        if (other != null && !completed(p, id))
          return t("finish_first", I18n.resolve(other.getTitle()));
      }
    }
    return t("complete");
  }

  private static boolean completed(Player p, String id) {
    return QuestService.statusFor(p, QuestRegistry.findById(id)) == QuestService.STATUS_COMPLETED;
  }

  private static int concordChapter(QuestDef q) {
    var chapters = ConcordCampaign.all();
    for (int i = 0; i < chapters.size(); i++)
      if (chapters.get(i).getId().equals(q.getId())) return i;
    return -1;
  }

  private static int siteState(Player p, int chapter, int site) {
    return p.getQuestFlag(ConcordCampaign.flag(chapter, "site" + site));
  }

  private static int pendingSite(Player p, int chapter) {
    if (chapter != 6) return chapter;
    for (int site : List.of(1, 2, 4)) if ((siteState(p, chapter, site) & 4) == 0) return site;
    return 1; // Revisit a completed witness to recover a missing proof.
  }

  private static String concordStage(Player p, int chapter) {
    if (chapter == 5 || chapter == 7) return t("confrontation");
    int site = pendingSite(p, chapter), state = siteState(p, chapter, site);
    if (chapter == 6) {
      long done =
          List.of(1, 2, 4).stream().filter(s -> (siteState(p, chapter, s) & 4) != 0).count();
      return t("bells_progress", done, 3);
    }
    if ((state & 3) != 3) return t("evidence_progress", Integer.bitCount(state & 3), 2);
    if ((state & 4) != 0) return t("evidence_recorded");
    if (site == 4)
      return t(
          "oath_progress",
          Math.min(3, Math.max(0, p.getQuestFlag(ConcordCampaign.flag(chapter, "sequence")))),
          3);
    return t("decision");
  }

  private static String concordNext(Player p, int chapter, QuestDef q) {
    if (chapter == 5 || chapter == 7) return t("return_to", npcName(ConcordCampaign.GIVER));
    int site = pendingSite(p, chapter), state = siteState(p, chapter, site);
    String witness = npcName(CONCORD_SITES.get(site));
    if ((state & 1) == 0) return t("ask_record", witness);
    if ((state & 2) == 0) return t("ask_echo", witness);
    if ((state & 4) != 0) return t("recover_proof", witness, itemName(q.getRequiredItemKey()));
    if (site == 4 && chapter != 6) return t("next_note", witness);
    return t("answer_witness", witness);
  }

  private static void addLegacyEntries(Player p, List<Entry> entries) {
    int alliance = AllianceQuest.stage(p);
    if (alliance > 0) {
      boolean done = alliance >= AllianceQuest.COMPLETE;
      String next =
          alliance == AllianceQuest.KILL_PIGS
                  && AllianceQuest.pigKills(p) >= AllianceQuest.REQUIRED_PIG_KILLS
              ? t("alliance.report")
              : t("alliance.next." + Math.min(alliance, AllianceQuest.COMPLETE));
      entries.add(
          legacy(
              "alliance",
              Kind.MAIN_STORY,
              done,
              t("alliance.title"),
              t("step", Math.min(alliance, AllianceQuest.COMPLETE), AllianceQuest.COMPLETE),
              next,
              List.of(
                  t(
                      "pig_progress",
                      Math.min(AllianceQuest.REQUIRED_PIG_KILLS, AllianceQuest.pigKills(p)),
                      AllianceQuest.REQUIRED_PIG_KILLS)),
              npcName("BaldricSilverknife"),
              t("alliance.description")));
    }
    int now = AudienceToKingQuest.nowSeconds();
    if (AudienceToKingQuest.isComplete(p)
        || AudienceToKingQuest.isActive(p, now)
        || AudienceToKingQuest.isExpired(p, now)) {
      boolean done = AudienceToKingQuest.isComplete(p),
          expired = AudienceToKingQuest.isExpired(p, now);
      boolean ready = AudienceToKingQuest.kills(p) >= AudienceToKingQuest.REQUIRED_KILLS;
      entries.add(
          legacy(
              "king_audience",
              Kind.MAIN_STORY,
              done,
              t("audience.title"),
              done ? t("complete") : expired ? t("expired") : ready ? t("ready") : t("in_progress"),
              t(
                  done
                      ? "audience.granted"
                      : expired ? "audience.restart" : ready ? "audience.report" : "audience.hunt"),
              List.of(
                  t(
                      "mage_progress",
                      Math.min(AudienceToKingQuest.REQUIRED_KILLS, AudienceToKingQuest.kills(p)),
                      AudienceToKingQuest.REQUIRED_KILLS)),
              npcName("ChamberlainThomar"),
              t("audience.description")));
    }
    int letter = UnsignedLetterQuest.stage(p);
    if (letter > 0)
      entries.add(
          legacy(
              "unsigned_letter",
              Kind.SIDE_QUEST,
              letter >= UnsignedLetterQuest.STAGE_RESOLVED,
              t("letter.title"),
              t("step", Math.min(letter, 2), 2),
              letter >= UnsignedLetterQuest.STAGE_RESOLVED ? t("complete") : t("letter.next"),
              List.of(),
              npcName("MirrorwardenYsmera"),
              t("letter.description")));
    List<String> evidence =
        List.of(
            WitnessStory.ARAKAS_RECORD,
            WitnessStory.GROVE_TESTIMONY,
            WitnessStory.WAR_ACCOUNT,
            WitnessStory.JUDGMENT_ACCOUNT,
            WitnessStory.LIBRARY_COMPARISON,
            WitnessStory.MANIFEST_COMPARED);
    long recorded = evidence.stream().filter(flag -> WitnessStory.has(p, flag)).count();
    if (recorded > 0 || WitnessStory.has(p, WitnessStory.ORACLE_TESTIMONY)) {
      boolean done = WitnessStory.has(p, WitnessStory.MANIFEST_COMPARED);
      String route = WitnessStory.nextRouteKey(p);
      entries.add(
          legacy(
              "witness_investigation",
              Kind.SIDE_QUEST,
              done,
              t("witness.title"),
              t("evidence_progress", recorded, evidence.size()),
              t("witness.next." + route.substring(route.lastIndexOf('.') + 1)),
              List.of(),
              npcName("Kilhiam"),
              t("witness.description")));
    }
    int camp = p.getQuestFlag(MercenaryCampQuest.STAGE);
    if (camp > 0) {
      String next;
      List<String> objectives = new ArrayList<>();
      if (camp >= 2 && camp <= 4) {
        int count =
            Math.min(
                MercenaryCampQuest.REQUIRED_SOLDIER_KILLS,
                p.getQuestFlag("ADDON_MERCENARY_" + (char) ('A' + camp - 2) + "_KILLS"));
        objectives.add(
            t("camp_progress", camp - 1, count, MercenaryCampQuest.REQUIRED_SOLDIER_KILLS));
        next =
            t(
                count < MercenaryCampQuest.REQUIRED_SOLDIER_KILLS
                    ? "camp.soldiers"
                    : "camp.lieutenant",
                camp - 1);
      } else next = t(camp == 1 ? "camp.details" : camp == 5 ? "camp.leader" : "camp.reward");
      entries.add(
          legacy(
              "mercenary_camps",
              Kind.SIDE_QUEST,
              false,
              t("camp.title"),
              t("step", Math.min(camp, 6), 6),
              next,
              objectives,
              npcName("VincentSwiftblade"),
              t("camp.description")));
    }
  }

  private static Entry legacy(
      String id,
      Kind kind,
      boolean completed,
      String title,
      String stage,
      String next,
      List<String> objectives,
      String giver,
      String description) {
    return new Entry(
        id, null, kind, completed, title, "", stage, next, objectives, giver, description);
  }
}
