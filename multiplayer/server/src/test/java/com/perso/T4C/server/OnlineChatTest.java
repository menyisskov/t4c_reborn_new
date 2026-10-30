package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

final class OnlineChatTest {
  @Test
  void pollIsRegionScopedCursorBasedAndRateLimitedPerSender() {
    OnlineChat chat = new OnlineChat();
    var first = chat.send("0:lighthaven", "session-a", "Aria", "Hello", 1_000L);
    var second = chat.send("0:lighthaven", "session-b", "Borin", "Hi", 1_000L);

    assertEquals(1L, first.id());
    assertEquals(2L, second.id());
    assertEquals(List.of(first, second), chat.poll("0:lighthaven", 0L).messages());
    assertEquals(second.id(), chat.poll("0:lighthaven", first.id()).cursor());
    assertEquals(List.of(second), chat.poll("0:lighthaven", first.id()).messages());
    assertEquals(0L, chat.poll("1:forest", 0L).cursor());
    assertEquals(List.of(), chat.poll("1:forest", 0L).messages());

    assertThrows(PreviewWorld.Rejected.class,
        () -> chat.send("0:lighthaven", "session-a", "Aria", "Again", 1_999L));
    assertEquals("Again",
        chat.send("0:lighthaven", "session-a", "Aria", "Again", 2_000L).text());
  }

  @Test
  void rejectsBlankControlAndOversizedMessages() {
    assertEquals(false, OnlineChat.validText("  "));
    assertEquals(false, OnlineChat.validText("hello\nworld"));
    assertEquals(false, OnlineChat.validText("x".repeat(OnlineChat.MAX_TEXT_CODE_POINTS + 1)));
    assertEquals(true, OnlineChat.validText("hello"));
  }

  @Test
  void pollCapsEachBatchAndAdvancesOnlyThroughReturnedMessages() {
    OnlineChat chat = new OnlineChat();
    for (int i = 0; i < OnlineChat.MAX_POLL_MESSAGES + 1; i++) {
      chat.send("0:lighthaven", "session-" + i, "Player" + i, "hello", i * 1_000L);
    }

    var first = chat.poll("0:lighthaven", 0L);
    var second = chat.poll("0:lighthaven", first.cursor());
    assertEquals(OnlineChat.MAX_POLL_MESSAGES, first.messages().size());
    assertEquals(1, second.messages().size());
    assertEquals(first.messages().getLast().id(), first.cursor());
    assertEquals(second.messages().getFirst().id(), second.cursor());
  }
}
