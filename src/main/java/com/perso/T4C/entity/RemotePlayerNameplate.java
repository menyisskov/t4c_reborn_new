package com.perso.T4C.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.player.Player;

/**
 * The name drawn above another online player. It uses {@link NameRenderer}, the renderer monsters
 * and NPCs use, with the same anchor convention (the entity's tile anchor), and is shown in the same
 * circumstances as a monster's: while the pointer is over the figure. The health colour is the hover
 * outline {@link Player#render} already draws from the player's hit points, exactly as for monsters.
 */
public final class RemotePlayerNameplate {
  private RemotePlayerNameplate() {}

  /** The text above the figure, or null when there is nothing to show. */
  public static String label(Player other, boolean hovered) {
    if (other == null || !hovered) return null;
    String name = other.getName();
    // A player the server sent no name for still carries the model's placeholder: draw nothing.
    if (name == null || name.isBlank() || name.equals(com.perso.T4C.i18n.I18n.key("player.default_name"))) return null;
    return name;
  }

  /** Draws the name above the figure with the shared name renderer. */
  public static void render(SpriteBatch batch, Player other, boolean hovered) {
    String label = label(other, hovered);
    if (label == null) return;
    var position = other.getPositionVector();
    NameRenderer.renderName(batch, label, position.x, position.y, 0f, 0f);
  }
}
