package com.perso.T4C.world;

import com.badlogic.gdx.graphics.Color;

/**
 * In-game clock and screen-darkness curve, matching the original T4C server's
 * TFCTime::IsNight() rule: night is [0h-6h[ and [18h-24h[ on a 24h day, with
 * a real-time pace (no compression). Torches/the Light spell are represented
 * by {@link #forceDaylight(boolean)}, which pins the ambient level to full day.
 *
 * <p>The clock only drives the light of the surface world (Z=0). Worlds without a sky
 * are flagged with {@link #setUnlitWorld(boolean)} and stay dark whatever the hour.
 */
public class DayNightCycle {

    private static final float DAY_SECONDS = 24f * 3600f;
    private static final float NIGHT_START_HOUR = 18f;
    private static final float NIGHT_END_HOUR = 6f;
    private static final float MIN_AMBIENT = 5f / 30f;
    private static final float TRANSITION_HOURS = 1f;

    private float elapsedSeconds;
    private boolean forceDaylight;
    private boolean unlit;

    public DayNightCycle() {
        this(7f * 3600f);
    }

    public DayNightCycle(float initialHour) {
        this.elapsedSeconds = initialHour * 3600f;
    }

    public void update(float delta) {
        elapsedSeconds = (elapsedSeconds + delta) % DAY_SECONDS;
    }

    public void forceDaylight(boolean force) {
        this.forceDaylight = force;
    }

    /**
     * Marks the current world as having no daylight of its own (any world but Z=0:
     * caves, dungeons, indoors). The time of day stops mattering there — only the
     * Light spell, via {@link #forceDaylight(boolean)}, lights the scene.
     */
    public void setUnlitWorld(boolean unlit) {
        this.unlit = unlit;
    }

    public float getHour() {
        return elapsedSeconds / 3600f;
    }

    public void setHour(float hour) {
        elapsedSeconds = ((hour % 24f) + 24f) % 24f * 3600f;
    }

    public boolean isNight() {
        float hour = getHour();
        return hour < NIGHT_END_HOUR || hour >= NIGHT_START_HOUR;
    }

    /** Ambient brightness in [MIN_AMBIENT, 1.0], smoothed across dawn/dusk. */
    public float getAmbientLevel() {
        if (forceDaylight) {
            return 1f;
        }
        if (unlit) {
            // No sky overhead: dark round the clock unless the Light spell is up.
            return MIN_AMBIENT;
        }
        float hour = getHour();
        if (hour >= NIGHT_END_HOUR + TRANSITION_HOURS && hour < NIGHT_START_HOUR - TRANSITION_HOURS) {
            return 1f;
        }
        if (hour >= NIGHT_START_HOUR + TRANSITION_HOURS || hour < NIGHT_END_HOUR - TRANSITION_HOURS) {
            return MIN_AMBIENT;
        }
        if (hour >= NIGHT_START_HOUR - TRANSITION_HOURS && hour < NIGHT_START_HOUR + TRANSITION_HOURS) {
            float t = (hour - (NIGHT_START_HOUR - TRANSITION_HOURS)) / (2f * TRANSITION_HOURS);
            return 1f - t * (1f - MIN_AMBIENT);
        }
        float wrappedHour = hour < NIGHT_END_HOUR ? hour + 24f : hour;
        float t = (wrappedHour - (NIGHT_END_HOUR + 24f - TRANSITION_HOURS)) / (2f * TRANSITION_HOURS);
        return MIN_AMBIENT + t * (1f - MIN_AMBIENT);
    }

    /** Overlay color to multiply/blend over the rendered scene (black at increasing alpha as it darkens). */
    public Color getOverlayColor() {
        float ambient = getAmbientLevel();
        return new Color(0f, 0f, 0f, 1f - ambient);
    }
}
