package com.perso.T4C.server;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded, ephemeral player chat separated by the server's current hosted region. */
final class OnlineChat {
  static final int MAX_TEXT_CODE_POINTS = 160;
  static final int MAX_POLL_MESSAGES = 50;
  private static final int MAX_MESSAGES_PER_REGION = 200;
  private static final int MAX_REGIONS = 256;
  private static final long SEND_INTERVAL_MILLIS = 1_000L;

  record Message(long id, String speaker, String text) {}
  record Poll(long cursor, List<Message> messages) {
    Poll { messages = List.copyOf(messages); }
  }

  private final Map<String, ArrayDeque<Message>> regions = new LinkedHashMap<>(16, .75f, true);
  private final Map<String, Long> lastSentAt = new LinkedHashMap<>();
  private long nextId = 1L;

  synchronized Message send(
      String region, String sender, String speaker, String text, long nowMillis) {
    if (region == null || region.isBlank() || sender == null || sender.isBlank()
        || speaker == null || speaker.isBlank() || !validText(text)) {
      throw new PreviewWorld.Rejected(400, "chat_input");
    }
    Long previous = lastSentAt.get(sender);
    if (previous != null && nowMillis - previous < SEND_INTERVAL_MILLIS) {
      throw new PreviewWorld.Rejected(429, "chat_rate_limited");
    }
    lastSentAt.put(sender, nowMillis);
    Message message = new Message(nextId++, speaker, text.strip());
    ArrayDeque<Message> messages = regions.computeIfAbsent(region, ignored -> new ArrayDeque<>());
    messages.addLast(message);
    while (messages.size() > MAX_MESSAGES_PER_REGION) messages.removeFirst();
    while (regions.size() > MAX_REGIONS) regions.remove(regions.keySet().iterator().next());
    return message;
  }

  synchronized Poll poll(String region, long cursor) {
    if (region == null || region.isBlank() || cursor < 0) {
      throw new PreviewWorld.Rejected(400, "chat_input");
    }
    ArrayDeque<Message> messages = regions.get(region);
    if (messages == null || messages.isEmpty()) return new Poll(cursor, List.of());
    List<Message> result = new ArrayList<>(Math.min(MAX_POLL_MESSAGES, messages.size()));
    long nextCursor = cursor;
    for (Message message : messages) {
      if (message.id() <= cursor) continue;
      result.add(message);
      nextCursor = message.id();
      if (result.size() == MAX_POLL_MESSAGES) break;
    }
    return new Poll(nextCursor, result);
  }

  synchronized void forget(String sender) { lastSentAt.remove(sender); }

  static boolean validText(String text) {
    if (text == null || text.isBlank() || text.codePointCount(0, text.length()) > MAX_TEXT_CODE_POINTS)
      return false;
    return text.codePoints().noneMatch(Character::isISOControl);
  }
}
