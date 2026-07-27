package com.perso.T4C.movement;

import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for movement logic (direction, facing, flip).
 * Handles angle computation and flip state for NPCs and Monsters.
 */
@Slf4j
@Getter
public abstract class BaseMovement {

    protected String currentAngle = "000";
    protected boolean isMoving = false;
    protected boolean flipX = false;

    /**
     * Set movement direction and update angle/flip.
     * Note: dx, dy are in world pixel coordinates (positive Y = down on screen).
     * We need to invert dy to match the game's internal angle system.
     */
    public void setDirection(float dx, float dy) {
        if (dx == 0 && dy == 0) {
            isMoving = false;
            return;
        }
        isMoving = true;
        float worldDy = -dy;
        computeAngleFlip(dx, worldDy);
    }

    /**
     * Face toward a target position.
     */
    public void faceToward(Vector2 from, Vector2 to) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float worldDy = -dy;
        computeAngleFlip(dx, worldDy);
    }

    /**
     * Stop moving.
     */
    public void stop() {
        isMoving = false;
    }

    /**
     * Computes the angle and horizontal flip state (flipX)
     * based on the movement direction defined by dx and worldDy. Updates
     * the current angle and flipX state, and logs changes if there are any.
     *
     * @param dx The horizontal component of the movement direction in world coordinates.
     *           Positive values indicate movement to the right, and negative values indicate
     *           movement to the left.
     * @param worldDy The vertical component of the movement direction in world coordinates.
     *                Positive values indicate downward movement, and negative values indicate
     *                upward movement. This value is inverted from the game's internal angle system.
     */
    protected void computeAngleFlip(float dx, float worldDy) {
        String oldAngle = currentAngle;
        boolean oldFlip = flipX;

        // Use tolerance for floating point comparison
        final float TOLERANCE = 0.3f;
        boolean dxZero = Math.abs(dx) < TOLERANCE;
        boolean dyZero = Math.abs(worldDy) < TOLERANCE;

        // Pure vertical movements (dx ~= 0)
        if (dxZero && worldDy < 0) { currentAngle = "000"; flipX = false; }
        else if (dxZero && worldDy > 0) { currentAngle = "180"; flipX = false; }
        // Pure horizontal movements (worldDy ~= 0)
        else if (dx < 0 && dyZero) { currentAngle = "090"; flipX = false; }
        else if (dx > 0 && dyZero) { currentAngle = "090"; flipX = true; }
        // Diagonal movements
        else if (dx < 0 && worldDy < 0) { currentAngle = "045"; flipX = false; }
        else if (dx > 0 && worldDy < 0) { currentAngle = "045"; flipX = true; }
        else if (dx < 0 && worldDy > 0) { currentAngle = "135"; flipX = false; }
        else if (dx > 0 && worldDy > 0) { currentAngle = "135"; flipX = true; }

        // Log only when angle or flip changes
        if (!oldAngle.equals(currentAngle) || oldFlip != flipX) {
            logDirectionChange(dx, worldDy);
        }
    }

    /**
     * Log direction change. Override to customize log message.
     */
    protected void logDirectionChange(float dx, float worldDy) {
        log.debug("Direction: dx={}, worldDy={} angle={}, flipX={}",
            String.format("%.2f", dx), String.format("%.2f", worldDy), currentAngle, flipX);
    }
}

