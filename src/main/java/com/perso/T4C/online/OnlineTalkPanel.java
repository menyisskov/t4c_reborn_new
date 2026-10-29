package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 * The topic choices offered after an NPC greets the player online. Single-player lets the player
 * type one of the NPC's highlighted keywords; this lists the same authored keywords (the first one
 * of each topic) and sends the chosen topic's index, which the server validates.
 */
public final class OnlineTalkPanel {
  private String npc;
  private String name;
  private List<Integer> topics = List.of();

  /** Opens the panel for this NPC; stays closed when the NPC has no authored topics. */
  public void open(String npcId, String displayName, Predicate<String> catalogue) {
    Objects.requireNonNull(npcId);
    var found = OnlineNpcTopics.available(npcId, catalogue);
    if (found.isEmpty()) {
      close();
      return;
    }
    npc = npcId;
    name = displayName == null || displayName.isBlank() ? npcId : displayName;
    topics = found;
  }

  public void open(String npcId, String displayName) {
    open(npcId, displayName, I18n::has);
  }

  public void close() {
    npc = null;
    name = null;
    topics = List.of();
  }

  public boolean isOpen() {
    return npc != null;
  }

  public String npc() {
    return npc;
  }

  /** The topic index behind number key {@code digit} (1-9), or empty. */
  public OptionalInt choose(int digit) {
    return isOpen() ? OnlineNpcTopics.choose(topics, digit) : OptionalInt.empty();
  }

  /** Title, one numbered row per topic, then the close hint; empty when closed. */
  public List<String> lines() {
    var lines = new ArrayList<String>();
    if (!isOpen()) return lines;
    lines.add(I18n.message("online.talk.header", name));
    for (int i = 0; i < topics.size(); i++)
      lines.add(I18n.message("online.talk.topic_row", i + 1, label(topics.get(i))));
    lines.add(I18n.key("online.talk.panel_hint"));
    return lines;
  }

  private String label(int topic) {
    String key = "npc.topic_keyword." + npc.toLowerCase(Locale.ROOT) + "." + topic + ".0";
    if (!I18n.has(key)) return I18n.message("online.talk.topic_fallback", topic + 1);
    String keyword = I18n.key(key);
    return keyword.isEmpty() ? keyword : Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1);
  }
}
