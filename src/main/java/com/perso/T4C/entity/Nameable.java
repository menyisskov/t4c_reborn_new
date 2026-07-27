package com.perso.T4C.entity;

/**
 * Interface for entities that can display their name on right-click.
 * Provides common functionality for name display timing.
 */
public interface Nameable {

    /**
     * Default display duration for entity names (4 seconds).
     */
    long NAME_DISPLAY_MS = 4000L;

    /**
     * Get the entity's name.
     */
    String getName();

    /**
     * Show the entity's name for the given duration (milliseconds).
     */
    void showNameFor(long ms);

    /**
     * Check if the entity's name should currently be visible.
     */
    boolean isNameVisible();

    /**
     * Check if the mouse cursor is over this entity.
     */
    boolean isMouseOver(float mouseX, float mouseY);

    /**
     * Show the entity's name for the default duration.
     */
    default void showName() {
        showNameFor(NAME_DISPLAY_MS);
    }
}

