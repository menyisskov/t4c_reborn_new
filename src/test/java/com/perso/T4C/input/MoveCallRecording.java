package com.perso.T4C.input;

import com.perso.T4C.player.Player;
import java.util.List;

/** Test seam: builds a {@link GameInputHandler} whose movement calls land in a recorded list. */
final class MoveCallRecording {
  private MoveCallRecording() {}

  /** One recorded movement call. */
  record Call(float dx, float dy, float delta) {}

  /** Records the movement the handler emits instead of moving anything. */
  private static final class RecordingPlayer extends Player {
    private final List<Call> calls;

    RecordingPlayer(List<Call> calls) {
      this.calls = calls;
    }

    @Override
    public void move(float dx, float dy, float delta) {
      calls.add(new Call(dx, dy, delta));
    }
  }

  static GameInputHandler handler(List<Call> calls) {
    return new GameInputHandler(null, null, new RecordingPlayer(calls));
  }

  /** A handler that moves this real player, to observe the rule's own gates. */
  static GameInputHandler handlerFor(Player player) {
    return new GameInputHandler(null, null, player);
  }

  static void frame(GameInputHandler handler, float delta) {
    handler.handleInput(delta, handler.getPlayer());
  }
}
