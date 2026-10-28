package com.perso.T4C.input;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.PlayerCommands;
import java.util.List;

/** Test seam: builds a {@link GameInputHandler} whose movement calls land in a recorded list. */
final class MoveCallRecording {
  private MoveCallRecording() {}

  /** One recorded movement call. */
  record Call(float dx, float dy, float delta) {}

  /** Records the movement intent the handler emits instead of moving anything. */
  private static final class RecordingCommands implements PlayerCommands {
    private final List<Call> calls;

    RecordingCommands(List<Call> calls) {
      this.calls = calls;
    }

    @Override
    public void attack(BaseMonster target) {
      throw new AssertionError("the input handler never attacks");
    }

    @Override
    public void move(float dx, float dy, float delta) {
      calls.add(new Call(dx, dy, delta));
    }
  }

  static GameInputHandler handler(List<Call> calls) {
    return new GameInputHandler(null, null, new Player(), new RecordingCommands(calls));
  }

  /** A handler whose commands move this real player, to observe the rule's own gates. */
  static GameInputHandler handlerFor(Player player) {
    return new GameInputHandler(
        null,
        null,
        player,
        new PlayerCommands() {
          @Override
          public void attack(BaseMonster target) {
            throw new AssertionError("the input handler never attacks");
          }

          @Override
          public void move(float dx, float dy, float delta) {
            player.move(dx, dy, delta);
          }
        });
  }

  static void frame(GameInputHandler handler, float delta) {
    handler.handleInput(delta, handler.getPlayer());
  }
}
