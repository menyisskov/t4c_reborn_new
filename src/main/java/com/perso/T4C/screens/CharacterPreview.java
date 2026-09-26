package com.perso.T4C.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The dressed figure shown next to the class picker on the character-creation screen (T4C-0059).
 *
 * <p>It is a real {@link Player} wearing the class's starting kit, drawn through the same {@code
 * PlayerAnimations} pipeline the world uses, so the preview is literally what the character will
 * look like on spawn - no separate portrait art to keep in sync. The player is built once and only
 * re-dressed when the selected class or gender changes, because rebuilding the animation set
 * reloads every body-part sprite.
 */
final class CharacterPreview {
  private static final Logger log = LoggerFactory.getLogger(CharacterPreview.class);

  /** Facing shown in the picker: "000" is the pose that looks back at the player. */
  private static final String PREVIEW_ANGLE = "000";

  private static final float SCALE = 2.2f;

  private final Vector2 origin = new Vector2();
  private final Rectangle bounds = new Rectangle();
  private final Matrix4 transform = new Matrix4();
  private final Matrix4 identity = new Matrix4();
  private Player player;
  private CharacterClass dressedAs;
  private String dressedGender;

  /** Re-dresses the figure if the class or gender changed. Safe to call every frame. */
  void dress(CharacterClass characterClass, String gender) {
    if (characterClass == null || gender == null) return;
    if (characterClass == dressedAs && gender.equals(dressedGender)) return;
    try {
      if (player == null) player = new Player();
      player.setGender(gender);
      player.getEquippedItems().clear();
      characterClass.startingEquipment().forEach((slot, item) -> equip(slot, item));
      PlayerAppearanceDefaults.applyDefaults(player);
      // PlayerAnimations caches every body part's frames and only reloads them when the texture
      // generation changes, not when the part map does - so without this the preview keeps
      // whatever sprites it loaded first and a class whose gear was never loaded draws nothing.
      player.getAnimations().refresh();
      dressedAs = characterClass;
      dressedGender = gender;
    } catch (RuntimeException e) {
      log.warn("Unable to build the character preview for {}", characterClass, e);
      player = null;
      dressedAs = null;
      dressedGender = null;
    }
  }

  private void equip(BodyPart slot, String item) {
    player.getEquippedItems().put(slot, item);
  }

  /**
   * Draws the figure scaled up, horizontally centred on {@code centerX} with its feet on {@code
   * bottomY}. Coordinates are in the caller's (y-down) screen space.
   */
  void render(SpriteBatch batch, float centerX, float bottomY) {
    if (player == null) return;
    origin.set(0f, 0f);
    player.getAnimations().getRenderBounds(origin, PREVIEW_ANGLE, false, false, bounds);
    if (bounds.width <= 0f || bounds.height <= 0f) return;
    float offsetX = centerX - (bounds.x + bounds.width / 2f) * SCALE;
    float offsetY = bottomY - (bounds.y + bounds.height) * SCALE;
    transform.idt().translate(offsetX, offsetY, 0f).scale(SCALE, SCALE, 1f);
    batch.setTransformMatrix(transform);
    player.getAnimations().render(batch, origin, PREVIEW_ANGLE, false, false);
    batch.setTransformMatrix(identity);
  }

  void dispose() {
    if (player != null) {
      player.getAnimations().dispose();
      player = null;
    }
    dressedAs = null;
    dressedGender = null;
  }
}
