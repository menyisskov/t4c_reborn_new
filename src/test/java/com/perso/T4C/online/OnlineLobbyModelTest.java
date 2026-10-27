package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.OnlineConnection.Credentials;
import com.perso.T4C.online.model.OnlineRoster;
import com.perso.T4C.online.model.OnlineRoster.CharacterSummary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineLobbyModelTest {
  private static final String KEY = "b".repeat(64);

  private static UUID id(long n) {
    return new UUID(0L, n);
  }

  private static CharacterSummary ready(long n) {
    return new CharacterSummary(id(n), "Hero" + n, "Warrior", "male", 5, 0, "ready");
  }

  private static OnlineRoster page(int from, int count, boolean more) {
    var list = new ArrayList<CharacterSummary>();
    for (int i = 0; i < count; i++) list.add(ready(from + i));
    UUID next = more ? list.getLast().id() : null;
    return new OnlineRoster(list, next);
  }

  /** Serves fixed pages keyed by cursor, recording every request. */
  private static final class FakeSource implements OnlineLobbyModel.PageSource {
    final List<UUID> requested = new ArrayList<>();
    final List<OnlineRoster> pages;
    int next;

    FakeSource(List<OnlineRoster> pages) {
      this.pages = pages;
    }

    @Override
    public OnlineRoster page(UUID after) throws IOException {
      requested.add(after);
      if (next >= pages.size()) throw new AssertionError("Requested more pages than served");
      return pages.get(next++);
    }
  }

  @Test
  void loadsAllPagesThroughTheCursorInServerOrder() throws Exception {
    var source = new FakeSource(List.of(page(1, 20, true), page(21, 3, false)));
    var model = new OnlineLobbyModel(KEY, null);
    model.load(source);
    assertEquals(23, model.entries().size());
    assertEquals(id(1), model.entries().getFirst().id());
    assertEquals(id(23), model.entries().getLast().id());
    assertFalse(model.truncated());
    assertNull(source.requested.getFirst(), "First page has no cursor");
    assertEquals(id(20), source.requested.get(1));
    assertEquals(2, source.requested.size());
  }

  @Test
  void boundsPagingAndMarksTheListTruncatedWhenMoreRowsExist() throws Exception {
    var pages = new ArrayList<OnlineRoster>();
    for (int p = 0; p < OnlineLobbyModel.MAX_PAGES + 3; p++) pages.add(page(p * 20 + 1, 20, true));
    var source = new FakeSource(pages);
    var model = new OnlineLobbyModel(KEY, null);
    model.load(source);
    assertEquals(OnlineLobbyModel.MAX_PAGES, source.requested.size(), "Must never request beyond the page bound");
    assertEquals(OnlineLobbyModel.MAX_ENTRIES, model.entries().size());
    assertTrue(model.truncated());
  }

  @Test
  void exactlyFullBoundIsNotTruncated() throws Exception {
    var pages = new ArrayList<OnlineRoster>();
    for (int p = 0; p < OnlineLobbyModel.MAX_PAGES; p++) pages.add(page(p * 20 + 1, 20, p < OnlineLobbyModel.MAX_PAGES - 1));
    pages.set(OnlineLobbyModel.MAX_PAGES - 1, page((OnlineLobbyModel.MAX_PAGES - 1) * 20 + 1, 20, false));
    var model = new OnlineLobbyModel(KEY, null);
    model.load(new FakeSource(pages));
    assertEquals(OnlineLobbyModel.MAX_ENTRIES, model.entries().size());
    assertFalse(model.truncated());
  }

  @Test
  void rejectsCursorsThatDoNotAdvanceAcrossPages() {
    var repeated = page(1, 2, true);
    var model = new OnlineLobbyModel(KEY, null);
    var source = new FakeSource(List.of(repeated, page(1, 2, false)));
    assertThrows(IOException.class, () -> model.load(source));
  }

  @Test
  void rejectsMissingReplies() {
    var model = new OnlineLobbyModel(KEY, null);
    assertThrows(IOException.class, () -> model.load(after -> null));
  }

  @Test
  void defaultsToPreferredCharacterWhenListedAndOtherwiseToFirst() throws Exception {
    var model = new OnlineLobbyModel(KEY, id(22));
    model.load(new FakeSource(List.of(page(1, 20, true), page(21, 3, false))));
    assertEquals(id(22), model.selectedCharacterId().orElseThrow());

    var absent = new OnlineLobbyModel(KEY, id(999));
    absent.load(new FakeSource(List.of(page(1, 3, false))));
    assertEquals(id(1), absent.selectedCharacterId().orElseThrow());

    var none = new OnlineLobbyModel(KEY, null);
    none.load(new FakeSource(List.of(page(1, 3, false))));
    assertEquals(id(1), none.selectedCharacterId().orElseThrow());
    assertEquals(0, none.selectedIndex());
  }

  @Test
  void emptyRosterHasNoSelectionAndConfirmsNothing() throws Exception {
    var model = new OnlineLobbyModel(KEY, id(1));
    model.load(new FakeSource(List.of(new OnlineRoster(List.of(), null))));
    assertTrue(model.entries().isEmpty());
    assertEquals(-1, model.selectedIndex());
    assertTrue(model.selected().isEmpty());
    assertTrue(model.confirm(0L).isEmpty());
    model.selectNext();
    model.selectPrevious();
    assertEquals(-1, model.selectedIndex());
  }

  @Test
  void selectionMovesClampAtBothEndsWithoutWrapping() throws Exception {
    var model = new OnlineLobbyModel(KEY, null);
    model.load(new FakeSource(List.of(page(1, 3, false))));
    model.selectPrevious();
    assertEquals(0, model.selectedIndex());
    model.selectNext();
    model.selectNext();
    assertEquals(2, model.selectedIndex());
    model.selectNext();
    assertEquals(2, model.selectedIndex());
  }

  @Test
  void reloadKeepsAStillListedSelectionAndRejectsUnknownSelections() throws Exception {
    var model = new OnlineLobbyModel(KEY, null);
    model.load(new FakeSource(List.of(page(1, 3, false))));
    assertTrue(model.select(id(2)));
    assertFalse(model.select(id(77)));
    assertFalse(model.select(null));
    model.load(new FakeSource(List.of(page(1, 3, false))));
    assertEquals(id(2), model.selectedCharacterId().orElseThrow());
  }

  @Test
  void confirmHandsOverTheAccountKeyAndSelectedCharacter() throws Exception {
    var model = new OnlineLobbyModel(KEY, null);
    model.load(new FakeSource(List.of(page(1, 3, false))));
    model.select(id(3));
    Credentials credentials = model.confirm(0L).orElseThrow();
    assertEquals(KEY, credentials.secret());
    assertEquals(id(3), credentials.characterId());
  }

  @Test
  void rejoinIsBlockedUntilTheLeaseDeadlineAndIsNeverGuessed() throws Exception {
    var model = new OnlineLobbyModel(KEY, null);
    model.load(new FakeSource(List.of(page(1, 2, false))));
    assertEquals(OptionalLong.of(0L), model.millisUntilRejoin(1_000L));

    model.beginReturnFromGame();
    assertEquals(OnlineLobbyModel.ReturnPhase.LEAVE_PENDING, model.phase(1_000L));
    assertTrue(model.confirm(1_000L).isEmpty(), "Leave outcome unknown: no rejoin");
    assertTrue(model.millisUntilRejoin(1_000L).isEmpty());

    model.finishReturnFromGame(5_000L);
    assertEquals(OnlineLobbyModel.ReturnPhase.LEASE_HELD, model.phase(4_999L));
    assertTrue(model.confirm(4_999L).isEmpty(), "Immediate re-join after leave must be refused");
    assertEquals(OptionalLong.of(1L), model.millisUntilRejoin(4_999L));

    assertEquals(OnlineLobbyModel.ReturnPhase.IDLE, model.phase(5_000L));
    assertTrue(model.confirm(5_000L).isPresent());
    assertEquals(OptionalLong.of(0L), model.millisUntilRejoin(5_000L));
  }

  @Test
  void toStringNeverContainsTheAccountKey() throws Exception {
    var model = new OnlineLobbyModel(KEY, id(1));
    model.load(new FakeSource(List.of(page(1, 1, false))));
    assertFalse(model.toString().contains(KEY));
    assertFalse(model.confirm(0L).orElseThrow().toString().contains(KEY));
  }

  @Test
  void rejectsMalformedAccountKeys() {
    assertThrows(IllegalArgumentException.class, () -> new OnlineLobbyModel(null, null));
    assertThrows(IllegalArgumentException.class, () -> new OnlineLobbyModel("ABCDEF", null));
    assertThrows(IllegalArgumentException.class, () -> new OnlineLobbyModel("g".repeat(64), null));
  }

  @Test
  void keepsLegacyUninitializedRowsSelectableWithoutClassOrGender() throws Exception {
    var legacy = new CharacterSummary(id(1), "Old", null, null, null, 0, "uninitialized");
    var model = new OnlineLobbyModel(KEY, null);
    model.load(new FakeSource(List.of(new OnlineRoster(List.of(legacy), null))));
    assertEquals("uninitialized", model.selected().orElseThrow().availability());
    assertTrue(model.confirm(0L).isPresent(), "Advisory availability does not block the server-revalidated join");
  }
}
