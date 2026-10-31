package com.perso.T4C.npc.core;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.player.Player;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Executes original NPC conversation methods for one participant at a time on one physical NPC. */
public final class NpcConversationSessions {
  private final BaseNPC npc;
  private final Map<String, Entry> sessions = new LinkedHashMap<>();
  private final AtomicBoolean bound = new AtomicBoolean();
  private NpcConversationState ambient;
  private NpcPresentation ambientPresentation;
  private Vector2 heldPatrolTarget;
  private float heldPauseTimer;
  private boolean heldPaused;

  private static final class Entry {
    final Player player;
    final NpcPresentation presentation;
    NpcConversationState state;

    Entry(Player player, NpcPresentation presentation, NpcConversationState state) {
      this.player = player;
      this.presentation = presentation;
      this.state = state;
    }
  }

  public NpcConversationSessions(BaseNPC npc) { this.npc = Objects.requireNonNull(npc, "npc"); }

  public boolean click(String participantId, Player player, NpcPresentation presentation) {
    Objects.requireNonNull(participantId, "participantId");
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(presentation, "presentation");
    return exclusive(() -> {
      Entry entry = sessions.get(participantId);
      if (entry != null && (entry.player != player || entry.presentation != presentation))
        throw new IllegalArgumentException("participant identity or presentation changed");
      if (entry == null) {
        if (sessions.isEmpty()) beginHold();
        entry = new Entry(player, presentation, fresh(ambient));
        sessions.put(participantId, entry);
      }
      Entry selected = entry;
      return invoke(selected, () -> npc.onClick(player));
    });
  }

  public boolean talk(String participantId, Player player, String text) {
    return exclusive(() -> {
      Entry entry = known(participantId, player);
      return entry != null && invoke(entry, () -> npc.talk(text, player));
    });
  }

  public boolean advanceDialog(String participantId, Player player) {
    return exclusive(() -> {
      Entry entry = known(participantId, player);
      return entry != null && invoke(entry, npc::advanceDialog);
    });
  }

  public boolean handleDialogClick(String participantId, Player player, float x, float y) {
    return exclusive(() -> {
      Entry entry = known(participantId, player);
      return entry != null && invoke(entry, () -> npc.handleDialogClick(x, y, player));
    });
  }

  public void endInteraction(String participantId, Player player) {
    exclusive(() -> {
      Entry entry = known(participantId, player);
      if (entry != null) invoke(entry, () -> { npc.endInteraction(); return null; });
      return null;
    });
  }

  public boolean checkPlayerRange(String participantId, Player player) {
    return exclusive(() -> {
      Entry entry = known(participantId, player);
      return entry != null && invoke(entry, () -> {
        npc.checkPlayerRange(player.getPositionVector());
        return npc.isInteracting();
      });
    });
  }

  /** Ticks the physical actor once, facing the earliest still-engaged participant. */
  public void tick(float delta) {
    exclusive(() -> {
      Player engaged = sessions.isEmpty() ? null : sessions.values().iterator().next().player;
      npc.update(delta, engaged == null ? null : engaged.getPositionVector());
      return null;
    });
  }

  public int activeCount() { return exclusive(() -> sessions.size()); }

  public boolean isInteracting(String participantId) {
    return exclusive(() -> {
      Entry entry = sessions.get(participantId);
      return entry != null && entry.state.interacting();
    });
  }

  private Entry known(String participantId, Player player) {
    Entry entry = sessions.get(Objects.requireNonNull(participantId, "participantId"));
    if (entry != null && entry.player != player)
      throw new IllegalArgumentException("participant Player changed");
    return entry;
  }

  private void beginHold() {
    ambient = npc.captureConversationState();
    ambientPresentation = npc.getPresentation();
    heldPatrolTarget = npc.patrolTarget == null ? null : new Vector2(npc.patrolTarget);
    heldPauseTimer = npc.pauseTimer;
    heldPaused = npc.isPaused;
    npc.patrolTarget = null;
    npc.movement.stop();
    npc.isInteracting = true;
  }

  private static NpcConversationState fresh(NpcConversationState original) {
    return new NpcConversationState(false, null, 0f, false, null, 0, false, null,
        java.util.List.of(), java.util.Map.of(), 0L, 0L, original.authoredState());
  }

  private <T> T invoke(Entry entry, Supplier<T> operation) {
    npc.installConversationState(entry.state);
    npc.setPresentation(entry.presentation);
    try {
      return operation.get();
    } finally {
      entry.state = npc.captureConversationState();
      if (!entry.state.interacting()) sessions.values().remove(entry);
      npc.installConversationState(ambient);
      npc.setPresentation(ambientPresentation);
      if (sessions.isEmpty()) {
        npc.patrolTarget = heldPatrolTarget == null ? null : new Vector2(heldPatrolTarget);
        npc.pauseTimer = heldPauseTimer;
        npc.isPaused = heldPaused;
        ambient = null;
        ambientPresentation = null;
      } else {
        npc.isInteracting = true;
        npc.patrolTarget = null;
        npc.movement.stop();
      }
    }
  }

  private <T> T exclusive(Supplier<T> operation) {
    if (!bound.compareAndSet(false, true))
      throw new IllegalStateException("NPC conversation already bound");
    try { return operation.get(); }
    finally { bound.set(false); }
  }
}
