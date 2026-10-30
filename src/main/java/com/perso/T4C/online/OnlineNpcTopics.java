package com.perso.T4C.online;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 * Authored NPC topics. The server sends only a reply key, so the client finds topics by the shipped
 * catalogue keys npc.topic.&lt;npc&gt;.&lt;index&gt; and sends the chosen index back for the server to validate.
 */
final class OnlineNpcTopics {
  /** Number keys 1-9 are the only choices the panel offers. */
  static final int MAX_CHOICES = 9;

  private OnlineNpcTopics() {}

  static String key(String npcId, int topic) {
    return "npc.topic." + Objects.requireNonNull(npcId).toLowerCase(Locale.ROOT) + "." + topic;
  }

  /** Contiguous authored topics from index 0; the first gap ends the list. Capped at the number keys. */
  static List<Integer> available(String npcId, Predicate<String> catalogue) {
    if ("LighthavenSamaritan".equals(npcId))
      return catalogue.test(key(npcId, 37)) ? List.of(37) : List.of();
    var topics = new ArrayList<Integer>();
    for (int topic = 0; topic < MAX_CHOICES && catalogue.test(key(npcId, topic)); topic++) topics.add(topic);
    return List.copyOf(topics);
  }

  /** Number key 1 selects the first listed topic. Out-of-range digits select nothing. */
  static OptionalInt choose(List<Integer> topics, int digit) {
    if (digit < 1 || digit > topics.size() || digit > MAX_CHOICES) return OptionalInt.empty();
    return OptionalInt.of(topics.get(digit - 1));
  }
}
