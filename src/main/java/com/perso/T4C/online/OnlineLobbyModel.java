package com.perso.T4C.online;

import com.perso.T4C.online.OnlineConnection.Credentials;
import com.perso.T4C.online.model.OnlineRoster;
import com.perso.T4C.online.model.OnlineRoster.CharacterSummary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Pure, render-free state for the online-only character lobby.
 *
 * <p>Loads owned roster summaries through a bounded page source, keeps a stable selection, and hands
 * the (account key, character UUID) pair to {@link OnlineConnection} only when a return from game has
 * finished its leave/lease window. The account key is never included in {@link #toString()}.
 * Times are passed in as epoch milliseconds so callers and tests control the clock.
 */
public final class OnlineLobbyModel {
  /** Upper bound on pages read per load; the cursor is never followed past this many requests. */
  public static final int MAX_PAGES = 5;
  /** Upper bound on listed characters per load. */
  public static final int MAX_ENTRIES = OnlineRoster.PAGE_SIZE * MAX_PAGES;

  /** One explicit account-roster page read; implemented by {@link OnlineAccountClient#page(UUID)}. */
  @FunctionalInterface
  public interface PageSource {
    OnlineRoster page(UUID after) throws IOException, InterruptedException;
  }

  /** Return-from-game phases. A rejoin is allowed only in {@link ReturnPhase#IDLE}. */
  public enum ReturnPhase {
    /** No return in progress; confirming a selection is allowed. */
    IDLE,
    /** The leave request was sent but its outcome and lease expiry are not known yet. */
    LEAVE_PENDING,
    /** The leave finished but the server may still hold the character lease until a deadline. */
    LEASE_HELD
  }

  private final String accountKey;
  private final UUID preferredCharacterId;
  private List<CharacterSummary> entries = List.of();
  private boolean truncated;
  private UUID selectedId;
  private ReturnPhase returnPhase = ReturnPhase.IDLE;
  private long leaseDeadlineMillis;

  /**
   * @param accountKey 64 lowercase hex characters from the private profile; validated, never printed
   * @param preferredCharacterId preferred character from the private profile, or null for none
   */
  public OnlineLobbyModel(String accountKey, UUID preferredCharacterId) {
    if (accountKey == null || !accountKey.matches("[0-9a-f]{64}"))
      throw new IllegalArgumentException("Invalid account key");
    this.accountKey = accountKey;
    this.preferredCharacterId = preferredCharacterId;
  }

  /**
   * Reads at most {@link #MAX_PAGES} pages, following only the server-provided cursor. Entries must
   * strictly increase across pages. The previous selection is kept when that character is still
   * listed; otherwise the preferred character is selected, and failing that the first entry.
   */
  public void load(PageSource source) throws IOException, InterruptedException {
    Objects.requireNonNull(source, "source");
    var loaded = new ArrayList<CharacterSummary>();
    UUID after = null;
    boolean more = false;
    for (int page = 0; page < MAX_PAGES; page++) {
      OnlineRoster roster = source.page(after);
      if (roster == null || roster.characters() == null) throw new IOException("Invalid roster page");
      for (CharacterSummary character : roster.characters()) {
        if (!loaded.isEmpty() && character.id().toString().compareTo(loaded.getLast().id().toString()) <= 0)
          throw new IOException("Roster order regressed");
        loaded.add(character);
      }
      if (roster.nextAfter() == null) {
        more = false;
        break;
      }
      if (loaded.isEmpty() || !roster.nextAfter().equals(loaded.getLast().id()))
        throw new IOException("Invalid roster cursor");
      after = roster.nextAfter();
      more = true;
    }
    entries = List.copyOf(loaded);
    truncated = more;
    UUID previous = selectedId;
    selectedId = null;
    if (previous != null && contains(previous)) selectedId = previous;
    else if (preferredCharacterId != null && contains(preferredCharacterId)) selectedId = preferredCharacterId;
    else if (!entries.isEmpty()) selectedId = entries.getFirst().id();
  }

  /** Stable, bounded list in server (PostgreSQL UUID byte) order. */
  public List<CharacterSummary> entries() {
    return entries;
  }

  /** True when more roster pages existed beyond {@link #MAX_PAGES}; the remainder is not listed. */
  public boolean truncated() {
    return truncated;
  }

  public Optional<UUID> selectedCharacterId() {
    return Optional.ofNullable(selectedId);
  }

  public Optional<CharacterSummary> selected() {
    return entries.stream().filter(c -> c.id().equals(selectedId)).findFirst();
  }

  /** Index of the selection, or -1 when nothing is listed. */
  public int selectedIndex() {
    for (int i = 0; i < entries.size(); i++) if (entries.get(i).id().equals(selectedId)) return i;
    return -1;
  }

  /** Moves the selection down, clamping at the last entry (no wrap). */
  public void selectNext() {
    int index = selectedIndex();
    if (index >= 0 && index < entries.size() - 1) selectedId = entries.get(index + 1).id();
  }

  /** Moves the selection up, clamping at the first entry (no wrap). */
  public void selectPrevious() {
    int index = selectedIndex();
    if (index > 0) selectedId = entries.get(index - 1).id();
  }

  /** Selects a listed character; returns false and leaves the selection unchanged otherwise. */
  public boolean select(UUID characterId) {
    if (characterId == null || !contains(characterId)) return false;
    selectedId = characterId;
    return true;
  }

  /** Current return phase at {@code nowMillis}; an expired lease deadline ends the hold. */
  public ReturnPhase phase(long nowMillis) {
    if (returnPhase == ReturnPhase.LEASE_HELD && nowMillis >= leaseDeadlineMillis) returnPhase = ReturnPhase.IDLE;
    return returnPhase;
  }

  /** Marks that the player left a game session and the leave request is in flight. */
  public void beginReturnFromGame() {
    returnPhase = ReturnPhase.LEAVE_PENDING;
  }

  /**
   * Records the leave outcome. The lease is treated as held until {@code leaseExpiresAtMillis}, even
   * if the leave was acknowledged, unless the caller passes the current time because the release is
   * confirmed. Rejoining stays blocked until that deadline.
   */
  public void finishReturnFromGame(long leaseExpiresAtMillis) {
    returnPhase = ReturnPhase.LEASE_HELD;
    leaseDeadlineMillis = leaseExpiresAtMillis;
  }

  /**
   * Milliseconds until a rejoin may be attempted: 0 when allowed now, empty while the leave outcome is
   * still unknown (LEAVE_PENDING), and the remaining hold while LEASE_HELD.
   */
  public OptionalLong millisUntilRejoin(long nowMillis) {
    return switch (phase(nowMillis)) {
      case IDLE -> OptionalLong.of(0L);
      case LEAVE_PENDING -> OptionalLong.empty();
      case LEASE_HELD -> OptionalLong.of(leaseDeadlineMillis - nowMillis);
    };
  }

  /**
   * Returns the credentials for the selected character, or empty when nothing is selected or a return
   * from game still blocks a rejoin. The server revalidates the character on join; the roster
   * availability value is advisory.
   */
  public Optional<Credentials> confirm(long nowMillis) {
    if (phase(nowMillis) != ReturnPhase.IDLE || selectedId == null) return Optional.empty();
    return Optional.of(new Credentials(accountKey, selectedId));
  }

  private boolean contains(UUID characterId) {
    for (CharacterSummary entry : entries) if (entry.id().equals(characterId)) return true;
    return false;
  }

  /** Never includes the account key. */
  @Override
  public String toString() {
    return "OnlineLobbyModel[entries=" + entries.size() + ", truncated=" + truncated
        + ", selected=" + selectedId + ", phase=" + returnPhase + ", accountKey=REDACTED]";
  }
}
