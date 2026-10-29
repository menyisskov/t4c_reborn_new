package com.perso.T4C.session;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.Player;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Online character source: the server's committed character state, as the client model already
 * holds it, becomes the state the screen builds its real {@link Player} from. Reads the model
 * only; it opens no socket and writes nothing.
 */
final class RemoteCharacterSource implements CharacterSource {
  private final Supplier<OnlineConnection.View> views;
  private final LongSupplier clock;
  private volatile ProgressMirror.Mirrored last;

  RemoteCharacterSource(Supplier<OnlineConnection.View> views) {
    this(views, System::currentTimeMillis);
  }

  RemoteCharacterSource(Supplier<OnlineConnection.View> views, LongSupplier clock) {
    this.views = views;
    this.clock = clock;
  }

  /**
   * Never {@code null} (an online character always exists): a view that is not ready yet fails
   * with {@link IllegalStateException} rather than starting a "new character" at the wrong place.
   */
  @Override
  public PlayerStateDto loadInitialState() {
    last = ProgressMirror.toStateDto(views.get(), clock);
    return last.state();
  }

  /**
   * Marks the player remote first, so no save call can reach the single-player file from the
   * moment it holds this character, then applies the state through the existing mapper.
   */
  @Override
  public void applyTo(PlayerStateDto state, Player player) {
    if (player == null) return;
    player.setRemote(true);
    PlayerStateMapper.applyToPlayer(state, player);
  }

  /** The last mirrored state with the server item identities, or {@code null} before the load. */
  ProgressMirror.Mirrored mirrored() {
    return last;
  }
}
