package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Read-only projection of the quest state currently committed by the online server. */
public final class OnlineQuestJournal {
  /** Quest IDs present in the authoritative server's runnable content catalog. */
  private static final Set<String> ONLINE_QUEST_IDS = Set.of("renewed_wards");

  public record Entry(String id, String title, boolean completed, int kills, int requiredKills,
      String targetMonster) {}

  private OnlineQuestJournal() {}

  public static List<Entry> entries(CharacterProgress progress) {
    if (progress == null) return List.of();
    var result = new ArrayList<Entry>();
    for (String id : ONLINE_QUEST_IDS) {
      QuestDef quest = QuestRegistry.findById(id);
      if (quest == null) continue;
      int status = QuestRules.status(key -> progress.questFlags().getOrDefault(key, 0), quest);
      if (status != QuestRules.STATUS_ACTIVE && status != QuestRules.STATUS_COMPLETED) continue;
      result.add(
          new Entry(
              id,
              I18n.resolve(quest.getTitle()),
              status == QuestRules.STATUS_COMPLETED,
              QuestRules.kills(key -> progress.questFlags().getOrDefault(key, 0), quest),
              quest.getRequiredKills(),
              I18n.resolve(quest.getTargetMonster())));
    }
    return List.copyOf(result);
  }

  public static List<String> lines(CharacterProgress progress) {
    var lines = new ArrayList<String>();
    lines.add(I18n.key("online.quest.title"));
    if (progress == null) {
      lines.add(I18n.key("online.quest.not_connected"));
    } else {
      List<Entry> entries = entries(progress);
      if (entries.isEmpty()) lines.add(I18n.key("online.quest.empty"));
      for (Entry entry : entries) {
        lines.add(
            I18n.message(
                "online.quest.row",
                entry.title(),
                I18n.key(entry.completed() ? "online.quest.completed" : "online.quest.active")));
        if (entry.completed()) {
          lines.add(I18n.key("online.quest.objectives_complete"));
        } else {
          lines.add(
              I18n.message(
                  "online.quest.kills",
                  entry.targetMonster(),
                  entry.kills(),
                  entry.requiredKills()));
        }
      }
    }
    lines.add(I18n.key("online.quest.close"));
    return List.copyOf(lines);
  }
}
