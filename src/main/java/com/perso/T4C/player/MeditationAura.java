package com.perso.T4C.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.GuiSprites;

/**
 * The slowly turning arcane circle drawn under a meditating player.
 *
 * <p>Frames come from the {@code MeditationAura-*} family seeded by
 * {@link com.perso.T4C.tools.MeditationSpriteSeed}; each is the same diagram rotated a
 * little further, so cycling through them reads as one continuous rotation. The whole
 * loop lasts {@link #LOOP_SECONDS}, which is deliberately slow: meditation should feel
 * calm rather than busy.
 */
public final class MeditationAura {

    private static final String SPRITE_BASE = "MeditationAura";
    private static final int FRAME_COUNT = 16;
    /** Seconds for one full turn of the circle. */
    private static final float LOOP_SECONDS = 6f;
    /** On-screen size, matching the seeded frame size. */
    private static final float WIDTH = 96f;
    private static final float HEIGHT = 56f;
    /** Pushes the circle down so it lies at the player's feet rather than on his chest. */
    private static final float GROUND_OFFSET_Y = 6f;

    private final TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
    private float elapsedSeconds;

    public MeditationAura() {
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = GuiSprites.load(SPRITE_BASE + "-" + (char) ('a' + i));
        }
    }

    /** Advances the loop. Called every frame while the player meditates. */
    public void update(float delta) {
        if (delta > 0f) {
            elapsedSeconds = (elapsedSeconds + delta) % LOOP_SECONDS;
        }
    }

    /** Resets the loop so each meditation starts from the same frame. */
    public void reset() {
        elapsedSeconds = 0f;
    }

    /** Draws the aura centred on the player's feet. */
    public void render(SpriteBatch batch, float playerX, float playerY) {
        int index = (int) (elapsedSeconds / LOOP_SECONDS * FRAME_COUNT) % FRAME_COUNT;
        TextureRegion frame = frames[index];
        if (frame == null) {
            return;
        }
        batch.draw(frame, playerX - WIDTH / 2f, playerY - HEIGHT / 2f + GROUND_OFFSET_Y,
                WIDTH, HEIGHT);
    }
}
